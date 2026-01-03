package org.wyman.api.dto;

import lombok.Data;

/**
 * 证书策略创建请求
 */
@Data
public class PolicyCreateRequest {
    /**
     * 策略名称
     */
    private String policyName;

    /**
     * 策略描述
     */
    private String description;

    /**
     * 允许的算法列表
     */
    private String[] allowedAlgorithms;

    /**
     * 有效期(天)
     */
    private Integer validityPeriod;

    /**
     * 密钥大小
     */
    private Integer keySize;
}
