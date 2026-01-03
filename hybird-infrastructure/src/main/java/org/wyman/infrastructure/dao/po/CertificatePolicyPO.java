package org.wyman.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 证书策略PO实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificatePolicyPO {
    /**
     * 策略ID
     */
    private String policyId;

    /**
     * 证书类型
     */
    private String certificateType;

    /**
     * 策略名称
     */
    private String policyName;

    /**
     * 允许的签名算法,逗号分隔
     */
    private String signatureAlgorithms;

    /**
     * 允许的KEM算法,逗号分隔
     */
    private String kemAlgorithms;

    /**
     * 是否需要混合签名
     */
    private Boolean requireHybridSignature;

    /**
     * 最小有效期(天)
     */
    private Integer minValidityDays;

    /**
     * 最大有效期(天)
     */
    private Integer maxValidityDays;

    /**
     * 主题DN正则表达式
     */
    private String subjectDnPattern;

    /**
     * 主题DN必填字段,逗号分隔
     */
    private String subjectDnRequiredFields;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 策略版本
     */
    private String version;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
