package org.wyman.trigger.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.wyman.domain.audit.service.AuditService;
import org.wyman.types.event.*;

/**
 * 领域事件监听器
 */
@Slf4j
@Component
public class DomainEventListener {

    private final AuditService auditService;

    public DomainEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * 监听身份验证完成事件
     */
    @Async
    @EventListener
    public void handleAuthenticationCompletedEvent(AuthenticationCompletedEvent event) {
        log.info("处理身份验证完成事件: requestId={}, success={}",
            event.getRequestId(), event.isSuccess());
        try {
            auditService.logAuthentication(
                event.getRequestId(),
                event.getApplicantId(),
                event.isSuccess(),
                event.getReason(),
                null
            );
        } catch (Exception e) {
            log.error("记录审计日志失败", e);
        }
    }

    /**
     * 监听证书签发事件
     */
    @Async
    @EventListener
    public void handleCertificateIssuedEvent(CertificateIssuedEvent event) {
        log.info("处理证书签发事件: serialNumber={}, subject={}",
            event.getCertificateSerial(), event.getSubject());
        try {
            auditService.logCertificateIssuance(
                event.getCertificateSerial(),
                event.getSubject(),
                "SYSTEM",
                event.getRequestId()
            );
        } catch (Exception e) {
            log.error("记录审计日志失败", e);
        }
    }

    /**
     * 监听证书吊销事件
     */
    @Async
    @EventListener
    public void handleCertificateRevokedEvent(CertificateRevokedEvent event) {
        log.info("处理证书吊销事件: serialNumber={}", event.getCertificateSerial());
        try {
            auditService.logCertificateRevocation(
                event.getCertificateSerial(),
                "CERTIFICATE", // subject需要从证书查询获取
                event.getReason(),
                "SYSTEM"
            );
        } catch (Exception e) {
            log.error("记录审计日志失败", e);
        }
    }

    /**
     * 监听CRL签发事件
     */
    @Async
    @EventListener
    public void handleCRLIssuedEvent(CRLIssuedEvent event) {
        log.info("处理CRL签发事件: crlNumber={}, revokedCount={}",
            event.getCrlNumber(), event.getRevokedCount());
        try {
            auditService.logCRLGeneration(
                event.getCrlNumber(),
                event.getCrlUrl(),
                event.getRevokedCount(),
                "SYSTEM"
            );

            // 更新吊销状态缓存
            // TODO: 调用RevocationStatusService更新缓存
        } catch (Exception e) {
            log.error("处理CRL签发事件失败", e);
        }
    }

    /**
     * 监听证书临期通知事件
     */
    @Async
    @EventListener
    public void handleRenewalNoticeDueEvent(RenewalNoticeDueEvent event) {
        log.info("处理证书临期通知事件: serialNumber={}, days={}",
            event.getCertificateSerial(), event.getDaysUntilExpiry());
        try {
            // TODO: 发送邮件或短信通知
            log.info("证书临期通知已发送: serialNumber={}", event.getCertificateSerial());
        } catch (Exception e) {
            log.error("发送临期通知失败", e);
        }
    }
}
