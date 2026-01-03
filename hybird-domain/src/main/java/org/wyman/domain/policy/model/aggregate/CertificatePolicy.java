package org.wyman.domain.policy.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wyman.domain.policy.valobj.CryptographicRule;
import org.wyman.domain.policy.valobj.SubjectDNRule;
import org.wyman.domain.policy.valobj.ValidityPeriodRule;
import org.wyman.types.enums.CertificateType;

import java.time.LocalDateTime;

/**
 * 证书策略聚合根
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificatePolicy {
    /**
     * 策略ID
     */
    private String policyId;

    /**
     * 证书类型
     */
    private CertificateType certificateType;

    /**
     * 策略名称
     */
    private String policyName;

    /**
     * 密码学规则
     */
    private CryptographicRule cryptographicRule;

    /**
     * 有效期规则
     */
    private ValidityPeriodRule validityPeriodRule;

    /**
     * 主题DN规则
     */
    private SubjectDNRule subjectDNRule;

    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 策略版本
     */
    private String version;

    public CertificatePolicy(String policyId, CertificateType certificateType, String policyName) {
        this.policyId = policyId;
        this.certificateType = certificateType;
        this.policyName = policyName;
        this.enabled = true;
        this.createTime = LocalDateTime.now();
        this.version = "1.0";
    }

    /**
     * 验证密码学参数
     */
    public boolean validateCryptographicParameters(String signatureAlg, String kemAlg) {
        if (cryptographicRule == null || !enabled) {
            return false;
        }
        return cryptographicRule.validateAlgorithmCombination(signatureAlg, kemAlg);
    }

    /**
     * 验证有效期
     */
    public boolean validateValidityPeriod(LocalDateTime notBefore, LocalDateTime notAfter) {
        if (validityPeriodRule == null || !enabled) {
            return false;
        }
        return validityPeriodRule.validateValidityPeriod(notBefore, notAfter);
    }

    /**
     * 验证主题DN
     */
    public boolean validateSubjectDN(String subjectDN) {
        if (subjectDNRule == null || !enabled) {
            return false;
        }
        return subjectDNRule.validateSubjectDN(subjectDN);
    }

    /**
     * 检查是否需要混合签名
     */
    public boolean requiresHybridSignature() {
        return enabled && cryptographicRule != null &&
               cryptographicRule.isRequireHybridSignature();
    }

    /**
     * 启用策略
     */
    public void enable() {
        this.enabled = true;
    }

    /**
     * 禁用策略
     */
    public void disable() {
        this.enabled = false;
    }
}
