package org.wyman.trigger.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.wyman.api.dto.*;
import org.wyman.api.response.Response;
import org.wyman.domain.authentication.adapter.port.ICSRParser;
import org.wyman.domain.authentication.model.aggregate.AuthenticationRequest;
import org.wyman.domain.authentication.model.entity.Applicant;
import org.wyman.domain.authentication.valobj.CertificateSigningRequest;
import org.wyman.domain.authentication.service.AuthenticationService;
import org.wyman.domain.chain.service.CertificateChainService;
import org.wyman.domain.lifecycle.model.aggregate.Certificate;
import org.wyman.domain.lifecycle.service.CertificateLifecycleService;
import org.wyman.domain.policy.service.PolicyService;
import org.wyman.domain.signing.service.SigningService;
import org.wyman.domain.signing.valobj.HybridCertificateRequestContext;
import org.wyman.domain.status.service.RevocationStatusService;
import org.wyman.types.constants.HybridCertificateOids;
import org.wyman.types.enums.CertificateType;
import org.wyman.types.enums.RevocationReason;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 证书管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/certificate")
@CrossOrigin("*")
public class CertificateController {

    private final AuthenticationService authenticationService;
    private final PolicyService policyService;
    private final SigningService signingService;
    private final CertificateLifecycleService lifecycleService;
    private final RevocationStatusService revocationStatusService;
    private final CertificateChainService certificateChainService;
    private final ICSRParser csrParser;

    public CertificateController(AuthenticationService authenticationService,
                                 PolicyService policyService,
                                 SigningService signingService,
                                 CertificateLifecycleService lifecycleService,
                                 RevocationStatusService revocationStatusService,
                                 CertificateChainService certificateChainService,
                                 ICSRParser csrParser) {
        this.authenticationService = authenticationService;
        this.policyService = policyService;
        this.signingService = signingService;
        this.lifecycleService = lifecycleService;
        this.revocationStatusService = revocationStatusService;
        this.certificateChainService = certificateChainService;
        this.csrParser = csrParser;
    }

    /**
     * 申请证书
     */
    @PostMapping("/apply")
    public Response<CertificateIssuanceResponse> applyCertificate(@RequestBody CertificateApplyRequest request) {
        try {
            // 1. 解析CSR,提取公钥和主题DN
            CertificateSigningRequest csr = csrParser.parsePEM(request.getCsrPemData());

            // 验证CSR签名
            if (!csr.isSignatureValid()) {
                return Response.fail("CSR签名验证失败");
            }

            // 2. 创建申请者
            Applicant applicant = new Applicant(
                request.getApplicantId(),
                request.getApplicantName()
            );
            applicant.setEmail(request.getApplicantEmail());

            // 3. 执行身份验证
            AuthenticationRequest authRequest = authenticationService.processAuthentication(
                java.util.UUID.randomUUID().toString(),
                request.getApplicantId(),
                request.getIdToken(),
                request.getCsrPemData()
            );

            if (authRequest.getStatus() != org.wyman.types.enums.AuthRequestStatus.VALIDATION_SUCCESSFUL) {
                return Response.fail("身份验证失败: " + authRequest.getFailureReason());
            }

            // 4. 获取策略
            CertificateType certType;
            try {
                certType = CertificateType.from(request.getCertificateType());
            } catch (IllegalArgumentException ex) {
                return Response.fail(ex.getMessage());
            }
            var policy = policyService.getPolicyForCertificateType(certType);
            if (policy == null) {
                return Response.fail("未找到可用的证书策略");
            }

            // 5. 验证CSR符合策略
            String signatureAlgorithm = csr.getPublicKeyAlgorithm();
            if (!policyService.validateCSRAgainstPolicy(
                certType,
                signatureAlgorithm,
                null,
                csr.getSubjectDN(),
                LocalDateTime.now(),
                request.getNotAfter()
            )) {
                return Response.fail("CSR不符合策略要求");
            }

            // 6. 签发证书,使用从CSR中提取的公钥
            org.wyman.domain.signing.valobj.Certificate cert = signingService.issueCertificate(
                request.getCaName(),
                csr.getSubjectDN(),
                csr.getPublicKey(),  // 从CSR中提取的公钥
                LocalDateTime.now(),
                request.getNotAfter(),
                signatureAlgorithm,
                null,
                null
            );

            // 7. 保存证书
            Certificate certificate = new Certificate(
                cert.getSerialNumber(),
                certType,
                cert.getSubjectDN(),
                cert.getIssuerDN(),
                cert.getNotBefore(),
                cert.getNotAfter(),
                request.getApplicantId()
            );
            certificate.setPemEncoded(cert.getPemEncoded());
            certificate.setIssuanceRequestId(authRequest.getRequestId());
            certificate.activate();

            lifecycleService.saveCertificate(certificate);

            // 8. 构建响应
            CertificateIssuanceResponse response = new CertificateIssuanceResponse();
            response.setSerialNumber(cert.getSerialNumber());
            response.setCertificatePem(cert.getPemEncoded());
            response.setSubjectDN(cert.getSubjectDN());
            response.setIssuerDN(cert.getIssuerDN());
            response.setNotBefore(cert.getNotBefore().toString());
            response.setNotAfter(cert.getNotAfter().toString());
            response.setSignatureAlgorithm(cert.getSignatureAlgorithm());
            response.setCrlDistributionPoint(cert.getCrlDistributionPoint());

            return Response.success(response);
        } catch (Exception e) {
            log.error("申请证书失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 申请混合证书
     */
    @PostMapping("/apply/hybrid")
    public Response<CertificateIssuanceResponse> applyHybridCertificate(@RequestBody HybridCertificateApplyRequest request) {
        try {
            // 1. 解析并校验两个CSR
            CertificateSigningRequest classicalCsr = csrParser.parsePEM(request.getClassicalCsrPemData());
            if (!classicalCsr.isSignatureValid()) {
                return Response.fail("传统算法CSR签名验证失败");
            }
            CertificateSigningRequest pqCsr = csrParser.parsePEM(request.getPostQuantumCsrPemData());
            if (!pqCsr.isSignatureValid()) {
                return Response.fail("后量子算法CSR签名验证失败");
            }

            if (!classicalCsr.getSubjectDN().equals(pqCsr.getSubjectDN())) {
                return Response.fail("两份CSR的Subject DN不一致，无法签发混合证书");
            }

            // 2. 申请者与身份验证
            Applicant applicant = new Applicant(
                request.getApplicantId(),
                request.getApplicantName()
            );
            applicant.setEmail(request.getApplicantEmail());

            AuthenticationRequest authRequest = authenticationService.processAuthentication(
                java.util.UUID.randomUUID().toString(),
                request.getApplicantId(),
                request.getIdToken(),
                request.getClassicalCsrPemData()
            );

            if (authRequest.getStatus() != org.wyman.types.enums.AuthRequestStatus.VALIDATION_SUCCESSFUL) {
                return Response.fail("身份验证失败: " + authRequest.getFailureReason());
            }

            // 3. 策略校验（要求混合签名）
            CertificateType certType = CertificateType.DEVICE_CERT;
            var policy = policyService.getPolicyForCertificateType(certType);
            if (policy == null) {
                return Response.fail("未找到可用的证书策略");
            }
            if (!policy.requiresHybridSignature()) {
                return Response.fail("当前策略未开启混合签名要求");
            }

            String signatureAlgorithm = request.getSignatureAlgorithm() != null
                ? request.getSignatureAlgorithm()
                : classicalCsr.getPublicKeyAlgorithm();

            if (!policy.validateCryptographicParameters(signatureAlgorithm, request.getKemAlgorithm())) {
                return Response.fail("签名/密钥封装算法不符合策略要求");
            }

            if (!policy.validateSubjectDN(classicalCsr.getSubjectDN())) {
                return Response.fail("Subject DN 不符合策略要求");
            }

            if (!policy.validateValidityPeriod(LocalDateTime.now(), request.getNotAfter())) {
                return Response.fail("证书有效期不符合策略要求");
            }

            String pqSignaturePublicKeyPem = resolvePqPublicKeyPem(pqCsr);
            String pqKekPublicKeyPem = resolvePqKekPublicKeyPem(pqCsr, pqSignaturePublicKeyPem);

            HybridCertificateRequestContext hybridContext = HybridCertificateRequestContext.builder()
                .hybridEnabled(true)
                .pqSignaturePublicKeyPem(pqSignaturePublicKeyPem)
                .pqKekPublicKeyPem(pqKekPublicKeyPem)
                .applicantPqSignatureValue(pqCsr.getPqSignatureValue())
                .applicantKekProof(pqCsr.getPqKekProofValue())
                .altSignatureAlgorithmOid(HybridCertificateOids.EXT_ALT_SIGNATURE_ALGORITHM)
                .altSignatureJcaName(signatureAlgorithm)
                .altSignatureRequired(true)
                .build();

            org.wyman.domain.signing.valobj.Certificate cert = signingService.issueCertificate(
                request.getCaName(),
                classicalCsr.getSubjectDN(),
                classicalCsr.getPublicKey(),
                LocalDateTime.now(),
                request.getNotAfter(),
                signatureAlgorithm,
                request.getKemAlgorithm(),
                hybridContext
            );

            Certificate certificate = new Certificate(
                cert.getSerialNumber(),
                certType,
                cert.getSubjectDN(),
                cert.getIssuerDN(),
                cert.getNotBefore(),
                cert.getNotAfter(),
                request.getApplicantId()
            );
            certificate.setPemEncoded(cert.getPemEncoded());
            certificate.setPostQuantumCsrPem(request.getPostQuantumCsrPemData());
            certificate.setPostQuantumPublicKeyPem(pqSignaturePublicKeyPem);
            certificate.setPostQuantumKekPublicKeyPem(pqKekPublicKeyPem);
            certificate.setIssuanceRequestId(authRequest.getRequestId());
            certificate.activate();

            lifecycleService.saveCertificate(certificate);

            // 5. 响应
            CertificateIssuanceResponse response = new CertificateIssuanceResponse();
            response.setSerialNumber(cert.getSerialNumber());
            response.setCertificatePem(cert.getPemEncoded());
            response.setSubjectDN(cert.getSubjectDN());
            response.setIssuerDN(cert.getIssuerDN());
            response.setNotBefore(cert.getNotBefore().toString());
            response.setNotAfter(cert.getNotAfter().toString());
            response.setSignatureAlgorithm(cert.getSignatureAlgorithm());
            response.setCrlDistributionPoint(cert.getCrlDistributionPoint());

            // 可选：在响应中附带后量子公钥以便客户端存档
            // response.setPostQuantumPublicKeyPem(toPublicKeyPem(pqCsr.getPublicKey()));

            return Response.success(response);
        } catch (Exception e) {
            log.error("申请混合证书失败", e);
            return Response.fail(e.getMessage());
        }
    }

    private String resolvePqPublicKeyPem(CertificateSigningRequest pqCsr) {
        if (pqCsr.getPqSignaturePublicKeyPem() != null && !pqCsr.getPqSignaturePublicKeyPem().isBlank()) {
            return pqCsr.getPqSignaturePublicKeyPem();
        }
        return toPublicKeyPem(pqCsr.getPublicKey());
    }

    private String resolvePqKekPublicKeyPem(CertificateSigningRequest pqCsr, String fallback) {
        if (pqCsr.getPqKekPublicKeyPem() != null && !pqCsr.getPqKekPublicKeyPem().isBlank()) {
            return pqCsr.getPqKekPublicKeyPem();
        }
        return fallback;
    }

    private String toPublicKeyPem(java.security.PublicKey publicKey) {
        try {
            String base64 = java.util.Base64.getMimeEncoder(64, "\n".getBytes())
                .encodeToString(publicKey.getEncoded());
            return "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----";
        } catch (Exception e) {
            throw new RuntimeException("公钥转PEM失败", e);
        }
    }

    /**
     * 查询证书
     */
    @GetMapping("/query")
    public Response<CertificateQueryResponse> queryCertificate(@RequestParam String serialNumber) {
        try {
            Certificate certificate = lifecycleService.getCertificateBySerialNumber(serialNumber);
            if (certificate == null) {
                return Response.fail("证书不存在");
            }

            CertificateQueryResponse response = new CertificateQueryResponse();
            response.setSerialNumber(certificate.getSerialNumber());
            response.setCertificateType(certificate.getCertificateType().name());
            response.setStatus(certificate.getStatus().name());
            response.setSubjectDN(certificate.getSubjectDN());
            response.setIssuerDN(certificate.getIssuerDN());
            response.setNotBefore(certificate.getNotBefore().toString());
            response.setNotAfter(certificate.getNotAfter().toString());
            response.setApplicantId(certificate.getApplicantId());
            response.setCertificatePem(certificate.getPemEncoded());

            return Response.success(response);
        } catch (Exception e) {
            log.error("查询证书失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询证书链
     */
    @GetMapping("/chain")
    public Response<CertificateChainResponse> queryCertificateChain(@RequestParam String serialNumber) {
        try {
            var chainInfoList = certificateChainService.getCertificateChain(serialNumber);
            if (chainInfoList.isEmpty()) {
                return Response.fail("证书不存在或证书链为空");
            }

            List<CertificateChainResponse.CertificateInfo> chain = new ArrayList<>();
            for (var chainInfo : chainInfoList) {
                chain.add(new CertificateChainResponse.CertificateInfo(
                    chainInfo.getSerialNumber(),
                    chainInfo.getSubjectDN(),
                    chainInfo.getIssuerDN(),
                    chainInfo.getCertificatePem(),
                    chainInfo.getLevel()
                ));
            }

            CertificateChainResponse response = new CertificateChainResponse();
            response.setSerialNumber(serialNumber);
            response.setChain(chain);

            return Response.success(response);
        } catch (Exception e) {
            log.error("查询证书链失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询设备证书
     */
    @GetMapping("/device")
    public Response<List<CertificateQueryResponse>> queryDeviceCertificates(
            @RequestParam String applicantId) {
        try {
            List<Certificate> certificates = lifecycleService
                .getCertificatesByApplicantId(applicantId);

            List<CertificateQueryResponse> responses = certificates.stream()
                .map(cert -> {
                    CertificateQueryResponse response = new CertificateQueryResponse();
                    response.setSerialNumber(cert.getSerialNumber());
                    response.setCertificateType(cert.getCertificateType().name());
                    response.setStatus(cert.getStatus().name());
                    response.setSubjectDN(cert.getSubjectDN());
                    response.setIssuerDN(cert.getIssuerDN());
                    response.setNotBefore(cert.getNotBefore().toString());
                    response.setNotAfter(cert.getNotAfter().toString());
                    response.setApplicantId(cert.getApplicantId());
                    response.setCertificatePem(cert.getPemEncoded());
                    return response;
                })
                .collect(Collectors.toList());

            return Response.success(responses);
        } catch (Exception e) {
            log.error("查询设备证书失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 吊销证书
     */
    @PostMapping("/revoke")
    public Response<Void> revokeCertificate(@RequestBody CertificateRevocationRequest request) {
        try {
            RevocationReason reason = RevocationReason.values()[request.getReasonCode()];
            lifecycleService.revokeCertificate(
                request.getSerialNumber(),
                reason,
                request.getOperator(),
                request.getComments()
            );
            return Response.success();
        } catch (Exception e) {
            log.error("吊销证书失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 续期证书
     */
    @PostMapping("/renew")
    public Response<CertificateIssuanceResponse> renewCertificate(@RequestBody CertificateRenewalRequest request) {
        try {
            // 1. 查询原证书
            Certificate oldCert = lifecycleService.getCertificateBySerialNumber(request.getOldSerialNumber());
            if (oldCert == null) {
                return Response.fail("原证书不存在");
            }

            // 2. 标记原证书为待续期
            lifecycleService.markCertificateForRenewal(request.getOldSerialNumber());

            // 3. 解析CSR并校验签名
            CertificateSigningRequest csr = csrParser.parsePEM(request.getCsrPemData());
            if (!csr.isSignatureValid()) {
                return Response.fail("CSR签名验证失败");
            }

            String signatureAlgorithm = csr.getPublicKeyAlgorithm();

            // 4. 签发新证书
            org.wyman.domain.signing.valobj.Certificate newCert = signingService.issueCertificate(
                oldCert.getIssuerDN(),
                csr.getSubjectDN(),
                csr.getPublicKey(),
                LocalDateTime.now(),
                request.getNotAfter(),
                signatureAlgorithm,
                null,
                null
            );

            // 5. 保存新证书
            Certificate certificate = new Certificate(
                newCert.getSerialNumber(),
                oldCert.getCertificateType(),
                newCert.getSubjectDN(),
                newCert.getIssuerDN(),
                newCert.getNotBefore(),
                newCert.getNotAfter(),
                oldCert.getApplicantId()
            );
            certificate.setPemEncoded(newCert.getPemEncoded());
            certificate.activate();

            lifecycleService.saveCertificate(certificate);

            // 6. 构建响应
            CertificateIssuanceResponse response = new CertificateIssuanceResponse();
            response.setSerialNumber(newCert.getSerialNumber());
            response.setCertificatePem(newCert.getPemEncoded());
            response.setSubjectDN(newCert.getSubjectDN());
            response.setIssuerDN(newCert.getIssuerDN());
            response.setNotBefore(newCert.getNotBefore().toString());
            response.setNotAfter(newCert.getNotAfter().toString());
            response.setSignatureAlgorithm(newCert.getSignatureAlgorithm());
            response.setCrlDistributionPoint(newCert.getCrlDistributionPoint());

            return Response.success(response);
        } catch (Exception e) {
            log.error("续期证书失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 签发证书(保留旧接口)
     */
    @PostMapping("/issue")
    public Response<CertificateIssuanceResponse> issueCertificate(@RequestBody CertificateIssuanceRequest request) {
        try {
            CertificateSigningRequest csr = csrParser.parsePEM(request.getCsrPemData());
            if (!csr.isSignatureValid()) {
                return Response.fail("CSR签名验证失败");
            }

            String signatureAlgorithm = request.getSignatureAlgorithm() != null
                ? request.getSignatureAlgorithm()
                : csr.getPublicKeyAlgorithm();

            org.wyman.domain.signing.valobj.Certificate certificate = signingService.issueCertificate(
                request.getCaName(),
                csr.getSubjectDN(),
                csr.getPublicKey(),
                request.getNotBefore() != null ? request.getNotBefore() : LocalDateTime.now(),
                request.getNotAfter(),
                signatureAlgorithm,
                request.getKemAlgorithm(),
                null
            );

            CertificateIssuanceResponse response = new CertificateIssuanceResponse();
            response.setSerialNumber(certificate.getSerialNumber());
            response.setCertificatePem(certificate.getPemEncoded());
            response.setSubjectDN(certificate.getSubjectDN());
            response.setIssuerDN(certificate.getIssuerDN());
            response.setNotBefore(certificate.getNotBefore().toString());
            response.setNotAfter(certificate.getNotAfter().toString());
            response.setSignatureAlgorithm(certificate.getSignatureAlgorithm());
            response.setCrlDistributionPoint(certificate.getCrlDistributionPoint());

            return Response.success(response);
        } catch (Exception e) {
            log.error("签发证书失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询证书吊销状态
     */
    @PostMapping("/status")
    public Response<CertificateStatusCheckResponse> checkCertificateStatus(
        @RequestBody CertificateStatusCheckRequest request) {
        try {
            CertificateStatusCheckResponse response = new CertificateStatusCheckResponse();

            if ("batch".equals(request.getQueryType()) && request.getSerialNumbers() != null) {
                // 批量查询
                Map<String, Boolean> results = revocationStatusService.batchCheckRevocationStatus(
                    request.getSerialNumbers()
                );
                response.setBatchResults(results);
            } else {
                // 单个查询
                String serialNumber = request.getSerialNumber();
                boolean revoked = revocationStatusService.checkRevocationStatus(serialNumber);
                response.setSerialNumber(serialNumber);
                response.setRevoked(revoked);

                if (revoked) {
                    var detail = revocationStatusService.getRevocationDetail(serialNumber);
                    if (detail != null) {
                        response.setRevocationDate(detail.getRevocationDate());
                        response.setRevocationReason(detail.getRevocationReason());
                    }
                }
            }

            return Response.success(response);
        } catch (Exception e) {
            log.error("查询证书状态失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 生成CRL
     */
    @PostMapping("/crl/generate")
    public Response<CRLGenerateResponse> generateCRL(@RequestBody CRLGenerateRequest request) {
        try {
            // 获取所有吊销证书
            List<org.wyman.domain.lifecycle.model.aggregate.Certificate> revokedCerts =
                lifecycleService.getRevokedCertificates();

            // 转换为RevokedCertificate列表
            List<org.wyman.domain.signing.valobj.RevokedCertificate> revokedList = revokedCerts.stream()
                .map(cert -> {
                    org.wyman.domain.signing.valobj.RevokedCertificate revoked =
                        new org.wyman.domain.signing.valobj.RevokedCertificate();
                    revoked.setSerialNumber(cert.getSerialNumber());
                    revoked.setRevocationDate(cert.getRevocationInfo().getRevocationTime());
                    // 将String原因转换为RevocationReason枚举
                    String reasonStr = cert.getRevocationInfo().getRevocationReason();
                    org.wyman.types.enums.RevocationReason reasonEnum = null;
                    if (reasonStr != null) {
                        for (org.wyman.types.enums.RevocationReason r : org.wyman.types.enums.RevocationReason.values()) {
                            if (r.getDesc().equals(reasonStr)) {
                                reasonEnum = r;
                                break;
                            }
                        }
                    }
                    revoked.setReason(reasonEnum != null ? reasonEnum : org.wyman.types.enums.RevocationReason.KEY_COMPROMISE);
                    return revoked;
                })
                .toList();

            org.wyman.domain.signing.valobj.CRL crl = signingService.generateCRL(
                request.getCaName(),
                revokedList,
                request.getSignatureAlgorithm()
            );

            // 同步更新吊销状态缓存
            String issuerDN = revokedCerts.isEmpty() ? request.getCaName() : revokedCerts.get(0).getIssuerDN();
            java.util.List<org.wyman.domain.status.model.aggregate.RevocationStatusCache.RevocationDetail> revokedDetails = revokedCerts.stream()
                .filter(cert -> cert.getRevocationInfo() != null)
                .map(cert -> new org.wyman.domain.status.model.aggregate.RevocationStatusCache.RevocationDetail(
                    cert.getSerialNumber(),
                    cert.getRevocationInfo().getRevocationTime(),
                    cert.getRevocationInfo().getRevocationReason()
                ))
                .toList();

            org.wyman.domain.status.valobj.CRLMetadata metadata = new org.wyman.domain.status.valobj.CRLMetadata(
                String.valueOf(crl.getCrlNumber()),
                issuerDN,
                crl.getThisUpdate(),
                crl.getNextUpdate(),
                "http://crl.example.com/crl-" + crl.getCrlNumber() + ".crl",
                revokedCerts.size()
            );
            revocationStatusService.updateCacheFromCRL(metadata, revokedDetails);

            CRLGenerateResponse response = new CRLGenerateResponse();
            response.setCrlNumber(crl.getCrlNumber());
            response.setCrlPem(crl.getPemEncoded());
            response.setCrlUrl("http://crl.example.com/crl-" + crl.getCrlNumber() + ".crl");
            response.setThisUpdate(crl.getThisUpdate());
            response.setNextUpdate(crl.getNextUpdate());
            response.setRevokedCount(revokedCerts.size());

            return Response.success(response);
        } catch (Exception e) {
            log.error("生成CRL失败", e);
            return Response.fail(e.getMessage());
        }
    }
}
