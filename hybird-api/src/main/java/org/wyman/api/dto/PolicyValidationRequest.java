package org.wyman.api.dto;

import lombok.Data;

/**
 * 策略验证请求
 */
@Data
public class PolicyValidationRequest {
    /**
     * 策略ID
     */
    private String policyId;

    /**
     * CSR PEM数据
     */
    private String csrPemData;
}
