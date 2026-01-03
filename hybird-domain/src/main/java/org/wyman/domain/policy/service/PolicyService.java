package org.wyman.domain.policy.service;

import org.springframework.stereotype.Service;
import org.wyman.domain.policy.adapter.port.ICertificatePolicyRepository;
import org.wyman.domain.policy.model.aggregate.CertificatePolicy;
import org.wyman.types.enums.CertificateType;

import java.time.LocalDateTime;

/**
 * 证书策略领域服务
 */
@Service
public class PolicyService {

    private final ICertificatePolicyRepository policyRepository;

    public PolicyService(ICertificatePolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    /**
     * 获取证书类型的策略
     */
    public CertificatePolicy getPolicyForCertificateType(CertificateType certificateType) {
        CertificatePolicy policy = policyRepository.findEnabledByType(certificateType);
        if (policy == null) {
            throw new RuntimeException("未找到适用于" + certificateType.getDesc() + "的策略");
        }
        return policy;
    }

    /**
     * 验证CSR是否符合策略
     */
    public boolean validateCSRAgainstPolicy(CertificateType certificateType,
                                            String signatureAlg,
                                            String kemAlg,
                                            String subjectDN,
                                            LocalDateTime notBefore,
                                            LocalDateTime notAfter) {
        CertificatePolicy policy = getPolicyForCertificateType(certificateType);

        if (!policy.validateCryptographicParameters(signatureAlg, kemAlg)) {
            return false;
        }

        if (!policy.validateSubjectDN(subjectDN)) {
            return false;
        }

        if (!policy.validateValidityPeriod(notBefore, notAfter)) {
            return false;
        }

        return true;
    }

    /**
     * 创建默认策略
     */
    public CertificatePolicy createDefaultPolicy(CertificateType certificateType,
                                                  String policyName) {
        CertificatePolicy policy = new CertificatePolicy(
            java.util.UUID.randomUUID().toString(),
            certificateType,
            policyName
        );

        // 设置默认密码学规则
        org.wyman.domain.policy.valobj.CryptographicRule cryptoRule =
            new org.wyman.domain.policy.valobj.CryptographicRule();
        cryptoRule.setAllowedSignatureAlgorithms(java.util.List.of("SM2", "ML-DSA", "RSA2048", "ECDSA_P256"));
        cryptoRule.setAllowedKeyEncapsulationAlgorithms(java.util.List.of("ML-KEM", "RSA-KEM"));
        cryptoRule.setMinKeyLength(2048);
        cryptoRule.setRequireHybridSignature(true);
        policy.setCryptographicRule(cryptoRule);

        // 设置默认有效期规则
        org.wyman.domain.policy.valobj.ValidityPeriodRule validityRule =
            new org.wyman.domain.policy.valobj.ValidityPeriodRule();
        validityRule.setMinDays(1);
        validityRule.setMaxDays(365);
        policy.setValidityPeriodRule(validityRule);

        // 设置默认主题DN规则
        org.wyman.domain.policy.valobj.SubjectDNRule subjectRule =
            new org.wyman.domain.policy.valobj.SubjectDNRule();
        subjectRule.setRequiredAttributes(java.util.List.of("CN", "O"));
        policy.setSubjectDNRule(subjectRule);

        policyRepository.save(policy);
        return policy;
    }

    /**
     * 更新策略
     */
    public void updatePolicy(String policyId, CertificatePolicy updatedPolicy) {
        CertificatePolicy existing = policyRepository.findById(policyId);
        if (existing == null) {
            throw new RuntimeException("策略不存在: " + policyId);
        }

        existing.setPolicyName(updatedPolicy.getPolicyName());
        existing.setCryptographicRule(updatedPolicy.getCryptographicRule());
        existing.setValidityPeriodRule(updatedPolicy.getValidityPeriodRule());
        existing.setSubjectDNRule(updatedPolicy.getSubjectDNRule());
        existing.setEnabled(updatedPolicy.isEnabled());

        policyRepository.save(existing);
    }

    /**
     * 启用策略
     */
    public void enablePolicy(String policyId) {
        CertificatePolicy policy = policyRepository.findById(policyId);
        if (policy == null) {
            throw new RuntimeException("策略不存在: " + policyId);
        }
        policy.enable();
        policyRepository.save(policy);
    }

    /**
     * 禁用策略
     */
    public void disablePolicy(String policyId) {
        CertificatePolicy policy = policyRepository.findById(policyId);
        if (policy == null) {
            throw new RuntimeException("策略不存在: " + policyId);
        }
        policy.disable();
        policyRepository.save(policy);
    }
}
