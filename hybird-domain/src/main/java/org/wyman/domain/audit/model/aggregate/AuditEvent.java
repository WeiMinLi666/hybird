package org.wyman.domain.audit.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wyman.domain.audit.valobj.TimestampValueObject;

import java.util.Map;

/**
 * 审计事件聚合根
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事件源
     */
    private String eventSource;

    /**
     * 事件发生时间
     */
    private TimestampValueObject timestamp;

    /**
     * 操作者
     */
    private String operator;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 目标资源
     */
    private String targetResource;

    /**
     * 事件负载(JSON格式)
     */
    private String payload;

    /**
     * 负载哈希值(用于完整性校验)
     */
    private String payloadHash;

    /**
     * 结果状态
     */
    private String resultStatus;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 额外属性
     */
    private Map<String, Object> additionalAttributes;

    /**
     * 创建时间
     */
    private TimestampValueObject createTime;

    public AuditEvent(String eventType, String operationType, String operator) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.eventType = eventType;
        this.operationType = operationType;
        this.operator = operator;
        this.timestamp = TimestampValueObject.now();
        this.createTime = TimestampValueObject.now();
    }

    /**
     * 计算负载哈希值
     */
    public void computePayloadHash() {
        if (payload != null && !payload.isEmpty()) {
            try {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(payload.getBytes());
                this.payloadHash = java.util.Base64.getEncoder().encodeToString(hash);
            } catch (Exception e) {
                this.payloadHash = null;
            }
        }
    }

    /**
     * 验证负载完整性
     */
    public boolean verifyPayloadIntegrity() {
        if (payload == null || payloadHash == null) {
            return false;
        }
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(payload.getBytes());
            String computedHash = java.util.Base64.getEncoder().encodeToString(hash);
            return computedHash.equals(payloadHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置负载并计算哈希
     */
    public void setPayloadWithHash(String payload) {
        this.payload = payload;
        computePayloadHash();
    }

    /**
     * 添加额外属性
     */
    public void addAdditionalAttribute(String key, Object value) {
        if (this.additionalAttributes == null) {
            this.additionalAttributes = new java.util.HashMap<>();
        }
        this.additionalAttributes.put(key, value);
    }
}
