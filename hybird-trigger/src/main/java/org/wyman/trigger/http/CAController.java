package org.wyman.trigger.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.wyman.api.dto.CACreateRequest;
import org.wyman.api.dto.CAQueryResponse;
import org.wyman.api.response.Response;
import org.wyman.domain.signing.service.SigningService;
import org.wyman.domain.signing.model.aggregate.CertificateAuthority;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 证书颁发机构(CA)控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ca")
@CrossOrigin("*")
public class CAController {

    private final SigningService signingService;

    public CAController(SigningService signingService) {
        this.signingService = signingService;
    }

    /**
     * 创建CA
     */
    @PostMapping("/create")
    public Response<CAQueryResponse> createCA(@RequestBody CACreateRequest request) {
        try {
            CertificateAuthority ca = signingService.createCertificateAuthority(
                request.getCaName(),
                request.getSubjectDN(),
                request.getSignatureAlgorithm(),
                request.getValidityDays()
            );

            CAQueryResponse response = new CAQueryResponse();
            response.setCaId(ca.getCaId());
            response.setCaName(ca.getCaName());
            response.setSubjectDN(ca.getCaCertificate().getSubjectDN());
            response.setSignatureAlgorithm(ca.getCaCertificate().getSignatureAlgorithm());
            response.setStatus("ACTIVE");
            response.setCertificatePem(ca.getCaCertificate().getPemEncoded());
            response.setPublicKey(""); // 需要从证书中提取
            response.setCreateTime(ca.getCreateTime().toString());

            return Response.success(response);
        } catch (Exception e) {
            log.error("创建CA失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询所有CA
     */
    @GetMapping("/list")
    public Response<List<CAQueryResponse>> listCAs() {
        try {
            List<CertificateAuthority> cas = signingService.getAllCertificateAuthorities();

            List<CAQueryResponse> responses = cas.stream()
                .map(ca -> {
                    CAQueryResponse response = new CAQueryResponse();
                    response.setCaId(ca.getCaId());
                    response.setCaName(ca.getCaName());
                    response.setSubjectDN(ca.getCaCertificate() != null ? ca.getCaCertificate().getSubjectDN() : "");
                    response.setSignatureAlgorithm(ca.getCaCertificate() != null ? ca.getCaCertificate().getSignatureAlgorithm() : "");
                    response.setStatus("ACTIVE");
                    response.setCertificatePem(ca.getCaCertificate() != null ? ca.getCaCertificate().getPemEncoded() : "");
                    response.setPublicKey("");
                    response.setCreateTime(ca.getCreateTime().toString());
                    return response;
                })
                .collect(Collectors.toList());

            return Response.success(responses);
        } catch (Exception e) {
            log.error("查询CA列表失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询CA详情
     */
    @GetMapping("/{caId}")
    public Response<CAQueryResponse> getCA(@PathVariable String caId) {
        try {
            CertificateAuthority ca = signingService.getCertificateAuthorityById(caId);
            if (ca == null) {
                return Response.fail("CA不存在");
            }

            CAQueryResponse response = new CAQueryResponse();
            response.setCaId(ca.getCaId());
            response.setCaName(ca.getCaName());
            response.setSubjectDN(ca.getCaCertificate().getSubjectDN());
            response.setSignatureAlgorithm(ca.getCaCertificate().getSignatureAlgorithm());
            response.setStatus("ACTIVE");
            response.setCertificatePem(ca.getCaCertificate().getPemEncoded());
            response.setPublicKey(""); // 需要从证书中提取
            response.setCreateTime(ca.getCreateTime().toString());

            return Response.success(response);
        } catch (Exception e) {
            log.error("查询CA详情失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 激活/停用CA
     */
    @PostMapping("/{caId}/activate")
    public Response<Void> activateCA(
            @PathVariable String caId,
            @RequestParam boolean active) {
        try {
            signingService.activateCertificateAuthority(caId, active);
            return Response.success();
        } catch (Exception e) {
            log.error("更新CA状态失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 吊销CA
     */
    @PostMapping("/{caId}/revoke")
    public Response<Void> revokeCA(
            @PathVariable String caId,
            @RequestParam String reason) {
        try {
            signingService.revokeCertificateAuthority(caId, reason);
            return Response.success();
        } catch (Exception e) {
            log.error("吊销CA失败", e);
            return Response.fail(e.getMessage());
        }
    }
}
