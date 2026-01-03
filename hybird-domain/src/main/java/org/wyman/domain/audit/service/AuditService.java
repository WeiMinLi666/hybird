package org.wyman.domain.audit.service;

import com.alibaba.fastjson2.JSON;
import org.springframework.stereotype.Service;
import org.wyman.domain.audit.adapter.port.IAuditEventRepository;
import org.wyman.domain.audit.model.aggregate.AuditEvent;
import org.wyman.types.event.IDomainEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审计日志领域服务
 */
@Service
public class AuditService {

    private final IAuditEventRepository auditEventRepository;

    public AuditService(IAuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    /**
     * 记录领域事件为审计日志
     */
    public void logDomainEvent(IDomainEvent domainEvent, String operator, String clientIp) {
        AuditEvent auditEvent = new AuditEvent(
            domainEvent.getEventType(),
            "DOMAIN_EVENT",
            operator
        );

        auditEvent.setEventSource("HYBRID_CERTIFICATE_SYSTEM");
        auditEvent.setClientIp(clientIp);
        auditEvent.setResultStatus("SUCCESS");

        // 将领域事件转换为JSON负载
        Map<String, Object> payload = Map.of(
            "eventId", domainEvent.getEventId(),
            "eventType", domainEvent.getEventType(),
            "occurredOn", domainEvent.getOccurredOn().toString()
        );
        auditEvent.setPayloadWithHash(JSON.toJSONString(payload));

        auditEventRepository.save(auditEvent);
    }

    /**
     * 记录自定义审计事件
     */
    public void logCustomEvent(String eventType,
                               String operationType,
                               String operator,
                               String targetResource,
                               Map<String, Object> payload,
                               String resultStatus,
                               String clientIp) {
        AuditEvent auditEvent = new AuditEvent(eventType, operationType, operator);
        auditEvent.setEventSource("HYBRID_CERTIFICATE_SYSTEM");
        auditEvent.setTargetResource(targetResource);
        auditEvent.setResultStatus(resultStatus);
        auditEvent.setClientIp(clientIp);

        if (payload != null) {
            auditEvent.setPayloadWithHash(JSON.toJSONString(payload));
        }

        auditEventRepository.save(auditEvent);
    }

    /**
     * 记录证书签发操作
     */
    public void logCertificateIssuance(String certificateSerial,
                                       String subject,
                                       String operator,
                                       String requestId) {
        Map<String, Object> payload = Map.of(
            "certificateSerial", certificateSerial,
            "subject", subject,
            "requestId", requestId,
            "action", "CERTIFICATE_ISSUED"
        );
        logCustomEvent(
            "CertificateIssued",
            "ISSUE_CERTIFICATE",
            operator,
            "CERTIFICATE:" + certificateSerial,
            payload,
            "SUCCESS",
            null
        );
    }

    /**
     * 记录证书吊销操作
     */
    public void logCertificateRevocation(String certificateSerial,
                                        String subject,
                                        String reason,
                                        String operator) {
        Map<String, Object> payload = Map.of(
            "certificateSerial", certificateSerial,
            "subject", subject,
            "reason", reason,
            "action", "CERTIFICATE_REVOKED"
        );
        logCustomEvent(
            "CertificateRevoked",
            "REVOKE_CERTIFICATE",
            operator,
            "CERTIFICATE:" + certificateSerial,
            payload,
            "SUCCESS",
            null
        );
    }

    /**
     * 记录CRL生成操作
     */
    public void logCRLGeneration(String crlNumber,
                                  String crlUrl,
                                  int revokedCount,
                                  String operator) {
        Map<String, Object> payload = Map.of(
            "crlNumber", crlNumber,
            "crlUrl", crlUrl,
            "revokedCount", revokedCount,
            "action", "CRL_GENERATED"
        );
        logCustomEvent(
            "CRLGenerated",
            "GENERATE_CRL",
            operator,
            "CRL:" + crlNumber,
            payload,
            "SUCCESS",
            null
        );
    }

    /**
     * 记录身份验证操作
     */
    public void logAuthentication(String requestId,
                                 String applicantId,
                                 boolean success,
                                 String failureReason,
                                 String clientIp) {
        Map<String, Object> payload = Map.of(
            "requestId", requestId,
            "applicantId", applicantId,
            "success", success,
            "failureReason", failureReason != null ? failureReason : "",
            "action", "AUTHENTICATION"
        );
        logCustomEvent(
            "Authentication",
            "AUTHENTICATE",
            "SYSTEM",
            "REQUEST:" + requestId,
            payload,
            success ? "SUCCESS" : "FAILURE",
            clientIp
        );
    }

    /**
     * 验证审计日志完整性
     */
    public boolean verifyAuditLogIntegrity(String eventId) {
        return auditEventRepository.verifyEventIntegrity(eventId);
    }

    /**
     * 批量验证审计日志完整性
     */
    public List<String> findTamperedEvents(LocalDateTime startTime,
                                           LocalDateTime endTime) {
        List<AuditEvent> events = auditEventRepository.findByTimeRange(startTime, endTime);
        return events.stream()
            .filter(event -> !event.verifyPayloadIntegrity())
            .map(AuditEvent::getEventId)
            .toList();
    }

    /**
     * 查询审计日志
     */
    public List<AuditEvent> queryAuditLogs(String eventType,
                                           String operator,
                                           LocalDateTime startTime,
                                           LocalDateTime endTime) {
        if (eventType != null && !eventType.isEmpty()) {
            return auditEventRepository.findByEventType(eventType);
        }
        if (operator != null && !operator.isEmpty()) {
            return auditEventRepository.findByOperator(operator);
        }
        if (startTime != null && endTime != null) {
            return auditEventRepository.findByTimeRange(startTime, endTime);
        }
        return List.of();
    }
}
