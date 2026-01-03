package org.wyman.api.dto;

import lombok.Data;

/**
 * 证书策略查询响应
 */
@Data
public class PolicyQueryResponse {
    /**
     * 策略ID
     */
    private String policyId;

    /**
     * 策略名称
     */
    private String policyName;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 是否激活
     */
    private Boolean isActive;

    /**
     * 描述
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

    /**
     * 创建时间
     */
    private String createTime;
}
