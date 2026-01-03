package org.wyman.trigger.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * CRL更新定时任务
 */
@Slf4j
@Component
public class CRLUpdateJob {

    /**
     * 每天凌晨2点更新CRL
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateCRL() {
        log.info("开始更新CRL");
        try {
            // TODO: 调用signing服务生成最新CRL
            log.info("CRL更新完成");
        } catch (Exception e) {
            log.error("更新CRL失败", e);
        }
    }
}
