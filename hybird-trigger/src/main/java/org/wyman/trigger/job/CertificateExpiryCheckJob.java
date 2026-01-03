package org.wyman.trigger.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wyman.domain.lifecycle.service.CertificateLifecycleService;

/**
 * 证书过期检查定时任务
 */
@Slf4j
@Component
public class CertificateExpiryCheckJob {

    private final CertificateLifecycleService lifecycleService;

    public CertificateExpiryCheckJob(CertificateLifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    /**
     * 每天凌晨1点检查过期证书
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkExpiredCertificates() {
        log.info("开始检查过期证书");
        try {
            lifecycleService.checkExpiredCertificates();
            log.info("过期证书检查完成");
        } catch (Exception e) {
            log.error("检查过期证书失败", e);
        }
    }

    /**
     * 每天早上8点扫描并通知临期证书
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void scanExpiringCertificates() {
        log.info("开始扫描临期证书");
        try {
            var expiringCerts = lifecycleService.scanAndNotifyExpiringCertificates();
            log.info("扫描临期证书完成,共{}张证书临期", expiringCerts.size());
        } catch (Exception e) {
            log.error("扫描临期证书失败", e);
        }
    }
}
