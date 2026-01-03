package org.wyman.api.dto;

import lombok.Data;

/**
 * 策略验证响应
 */
@Data
public class PolicyValidationResponse {
    /**
     * 是否有效
     */
    private Boolean isValid;

    /**
     * 策略ID
     */
    private String policyId;

    /**
     * 消息
     */
    private String message;
}
