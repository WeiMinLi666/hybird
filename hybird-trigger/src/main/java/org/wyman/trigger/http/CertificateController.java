package org.wyman.trigger.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.wyman.api.dto.*;
import org.wyman.api.response.Response;
import org.wyman.domain.lifecycle.service.CertificateLifecycleService;
import org.wyman.domain.signing.service.SigningService;
import org.wyman.domain.status.service.RevocationStatusService;
import org.wyman.types.enums.RevocationReason;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 证书管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/certificate")
@CrossOrigin("*")
public class CertificateController {

    private final SigningService signingService;
    private final CertificateLifecycleService lifecycleService;
    private final RevocationStatusService revocationStatusService;

    public CertificateController(SigningService signingService,
                                 CertificateLifecycleService lifecycleService,
                                 RevocationStatusService revocationStatusService) {
        this.signingService = signingService;
        this.lifecycleService = lifecycleService;
        this.revocationStatusService = revocationStatusService;
    }

    /**
     * 签发证书
     */
    @PostMapping("/issue")
    public Response<CertificateIssuanceResponse> issueCertificate(@RequestBody CertificateIssuanceRequest request) {
        try {
            // 简化实现,实际需要从请求中提取公钥等参数
            org.wyman.domain.signing.valobj.Certificate certificate = signingService.issueCertificate(
                request.getCaName(),
                "CN=" + request.getApplicantId(), // 简化:直接用申请者ID作为CN
                null, // 公钥需要从CSR中提取
                request.getNotBefore() != null ? request.getNotBefore() : LocalDateTime.now(),
                request.getNotAfter(),
                request.getSignatureAlgorithm(),
                request.getKemAlgorithm()
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
                .map(cert -> new org.wyman.domain.signing.valobj.RevokedCertificate(
                    cert.getSerialNumber(),
                    cert.getRevocationInfo().getRevocationTime(),
                    cert.getRevocationInfo().getRevocationReason()
                ))
                .toList();

            org.wyman.domain.signing.valobj.CRL crl = signingService.generateCRL(
                request.getCaName(),
                revokedList,
                request.getSignatureAlgorithm()
            );

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
