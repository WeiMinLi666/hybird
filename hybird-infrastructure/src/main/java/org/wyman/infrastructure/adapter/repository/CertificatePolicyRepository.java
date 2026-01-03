package org.wyman.infrastructure.adapter.repository;

import org.springframework.stereotype.Repository;
import org.wyman.domain.policy.adapter.port.ICertificatePolicyRepository;
import org.wyman.domain.policy.model.aggregate.CertificatePolicy;
import org.wyman.domain.policy.valobj.CryptographicRule;
import org.wyman.domain.policy.valobj.SubjectDNRule;
import org.wyman.domain.policy.valobj.ValidityPeriodRule;
import org.wyman.infrastructure.dao.mapper.CertificatePolicyMapper;
import org.wyman.infrastructure.dao.po.CertificatePolicyPO;
import org.wyman.types.enums.CertificateType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 证书策略仓储MyBatis实现
 */
@Repository
public class CertificatePolicyRepository implements ICertificatePolicyRepository {

    private final CertificatePolicyMapper certificatePolicyMapper;

    public CertificatePolicyRepository(CertificatePolicyMapper certificatePolicyMapper) {
        this.certificatePolicyMapper = certificatePolicyMapper;
    }

    @Override
    public void save(CertificatePolicy policy) {
        CertificatePolicyPO po = toPO(policy);
        CertificatePolicyPO existing = certificatePolicyMapper.selectById(policy.getPolicyId());
        if (existing == null) {
            certificatePolicyMapper.insert(po);
        } else {
            certificatePolicyMapper.update(po);
        }
    }

    @Override
    public CertificatePolicy findById(String policyId) {
        CertificatePolicyPO po = certificatePolicyMapper.selectById(policyId);
        return toDomain(po);
    }

    @Override
    public CertificatePolicy findEnabledByType(CertificateType certificateType) {
        CertificatePolicyPO po = certificatePolicyMapper.selectEnabledByType(certificateType.name());
        return toDomain(po);
    }

    @Override
    public List<CertificatePolicy> findAll() {
        List<CertificatePolicyPO> poList = certificatePolicyMapper.selectAll();
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<CertificatePolicy> findEnabled() {
        List<CertificatePolicyPO> poList = certificatePolicyMapper.selectEnabled();
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    private CertificatePolicyPO toPO(CertificatePolicy policy) {
        return CertificatePolicyPO.builder()
                .policyId(policy.getPolicyId())
                .certificateType(policy.getCertificateType().name())
                .policyName(policy.getPolicyName())
                .signatureAlgorithms(policy.getCryptographicRule() != null ?
                        String.join(",", policy.getCryptographicRule().getAllowedSignatureAlgorithms()) : null)
                .kemAlgorithms(policy.getCryptographicRule() != null ?
                        String.join(",", policy.getCryptographicRule().getAllowedKeyEncapsulationAlgorithms()) : null)
                .requireHybridSignature(policy.getCryptographicRule() != null ?
                        policy.getCryptographicRule().isRequireHybridSignature() : false)
                .minValidityDays(policy.getValidityPeriodRule() != null ?
                        policy.getValidityPeriodRule().getMinDays() : null)
                .maxValidityDays(policy.getValidityPeriodRule() != null ?
                        policy.getValidityPeriodRule().getMaxDays() : null)
                .subjectDnPattern(policy.getSubjectDNRule() != null ?
                        policy.getSubjectDNRule().getCommonNamePattern() != null ?
                                policy.getSubjectDNRule().getCommonNamePattern().pattern() : null : null)
                .subjectDnRequiredFields(policy.getSubjectDNRule() != null ?
                        String.join(",", policy.getSubjectDNRule().getRequiredAttributes()) : null)
                .enabled(policy.isEnabled())
                .version(policy.getVersion())
                .createTime(policy.getCreateTime() != null ? policy.getCreateTime() : java.time.LocalDateTime.now())
                .build();
    }

    private CertificatePolicy toDomain(CertificatePolicyPO po) {
        if (po == null) {
            return null;
        }

        CertificatePolicy policy = new CertificatePolicy(
                po.getPolicyId(),
                org.wyman.types.enums.CertificateType.valueOf(po.getCertificateType()),
                po.getPolicyName()
        );

        if (po.getSignatureAlgorithms() != null || po.getKemAlgorithms() != null) {
            CryptographicRule cryptoRule = new CryptographicRule();
            if (po.getSignatureAlgorithms() != null) {
                cryptoRule.setAllowedSignatureAlgorithms(
                        java.util.Arrays.asList(po.getSignatureAlgorithms().split(",")));
            }
            if (po.getKemAlgorithms() != null) {
                cryptoRule.setAllowedKeyEncapsulationAlgorithms(
                        java.util.Arrays.asList(po.getKemAlgorithms().split(",")));
            }
            cryptoRule.setRequireHybridSignature(po.getRequireHybridSignature() != null ?
                    po.getRequireHybridSignature() : false);
            policy.setCryptographicRule(cryptoRule);
        }

        if (po.getMinValidityDays() != null || po.getMaxValidityDays() != null) {
            ValidityPeriodRule validityRule = new ValidityPeriodRule();
            validityRule.setMinDays(po.getMinValidityDays());
            validityRule.setMaxDays(po.getMaxValidityDays());
            policy.setValidityPeriodRule(validityRule);
        }

        if (po.getSubjectDnPattern() != null || po.getSubjectDnRequiredFields() != null) {
            SubjectDNRule subjectRule = new SubjectDNRule();
            if (po.getSubjectDnPattern() != null) {
                subjectRule.setCommonNamePattern(java.util.regex.Pattern.compile(po.getSubjectDnPattern()));
            }
            if (po.getSubjectDnRequiredFields() != null) {
                subjectRule.setRequiredAttributes(
                        java.util.Arrays.asList(po.getSubjectDnRequiredFields().split(",")));
            }
            policy.setSubjectDNRule(subjectRule);
        }

        policy.setEnabled(po.getEnabled() != null ? po.getEnabled() : true);
        policy.setVersion(po.getVersion());
        policy.setCreateTime(po.getCreateTime());

        return policy;
    }
}
