package org.wyman.trigger.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wyman.domain.lifecycle.service.CertificateLifecycleService;
import org.wyman.domain.signing.adapter.port.ICertificateAuthorityRepository;
import org.wyman.domain.signing.model.aggregate.CertificateAuthority;
import org.wyman.domain.signing.service.SigningService;
import org.wyman.domain.signing.valobj.RevokedCertificate;

import java.util.List;

/**
 * CRL更新定时任务
 */
@Slf4j
@Component
public class CRLUpdateJob {

    private final SigningService signingService;
    private final CertificateLifecycleService lifecycleService;
    private final ICertificateAuthorityRepository caRepository;

    public CRLUpdateJob(SigningService signingService,
                         CertificateLifecycleService lifecycleService,
                         ICertificateAuthorityRepository caRepository) {
        this.signingService = signingService;
        this.lifecycleService = lifecycleService;
        this.caRepository = caRepository;
    }

    /**
     * 每天凌晨2点更新CRL
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateCRL() {
        log.info("开始更新CRL");
        try {
            // 查询所有启用的CA
            List<CertificateAuthority> caList = caRepository.findAll().stream()
                .filter(CertificateAuthority::isEnabled)
                .toList();

            log.info("找到{}个启用的CA", caList.size());

            for (CertificateAuthority ca : caList) {
                try {
                    // 查询已吊销证书
                    List<org.wyman.domain.lifecycle.model.aggregate.Certificate> revokedCerts =
                        lifecycleService.getRevokedCertificates();

                    // 转换为RevokedCertificate列表
                    List<RevokedCertificate> revokedList = revokedCerts.stream()
                        .filter(cert -> cert.getIssuerDN().equals(ca.getCaCertificate().getSubjectDN()))
                        .map(cert -> {
                            RevokedCertificate revoked = new RevokedCertificate();
                            revoked.setSerialNumber(cert.getSerialNumber());
                            revoked.setRevocationDate(
                                cert.getRevocationInfo() != null ? cert.getRevocationInfo().getRevocationTime() : null
                            );
                            if (cert.getRevocationInfo() != null && cert.getRevocationInfo().getRevocationReason() != null) {
                                // 将String原因转换为RevocationReason枚举
                                String reasonStr = cert.getRevocationInfo().getRevocationReason();
                                org.wyman.types.enums.RevocationReason reasonEnum = null;
                                for (org.wyman.types.enums.RevocationReason r : org.wyman.types.enums.RevocationReason.values()) {
                                    if (r.getDesc().equals(reasonStr)) {
                                        reasonEnum = r;
                                        break;
                                    }
                                }
                                revoked.setReason(reasonEnum != null ? reasonEnum : org.wyman.types.enums.RevocationReason.KEY_COMPROMISE);
                            }
                            return revoked;
                        })
                        .toList();

                    log.info("CA {} 有 {} 个已吊销证书", ca.getCaName(), revokedList.size());

                    if (!revokedList.isEmpty()) {
                        // 生成CRL,使用SM2签名算法
                        signingService.generateCRL(
                            ca.getCaName(),
                            revokedList,
                            "SM2"
                        );
                        log.info("CA {} 的CRL生成完成,吊销证书数: {}", ca.getCaName(), revokedList.size());
                    } else {
                        log.info("CA {} 无已吊销证书,跳过CRL生成", ca.getCaName());
                    }
                } catch (Exception e) {
                    log.error("更新CA {} 的CRL失败", ca.getCaName(), e);
                }
            }

            log.info("CRL更新完成");
        } catch (Exception e) {
            log.error("更新CRL失败", e);
        }
    }
}
