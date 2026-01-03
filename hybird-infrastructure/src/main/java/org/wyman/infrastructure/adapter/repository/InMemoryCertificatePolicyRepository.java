package org.wyman.infrastructure.adapter.repository;

import org.springframework.stereotype.Repository;
import org.wyman.domain.policy.adapter.port.ICertificatePolicyRepository;
import org.wyman.domain.policy.model.aggregate.CertificatePolicy;
import org.wyman.types.enums.CertificateType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 证书策略仓储内存实现
 */
@Repository
public class InMemoryCertificatePolicyRepository implements ICertificatePolicyRepository {

    private final Map<String, CertificatePolicy> storage = new HashMap<>();

    @Override
    public void save(CertificatePolicy policy) {
        storage.put(policy.getPolicyId(), policy);
    }

    @Override
    public CertificatePolicy findById(String policyId) {
        return storage.get(policyId);
    }

    @Override
    public CertificatePolicy findEnabledByType(CertificateType certificateType) {
        return storage.values().stream()
            .filter(policy -> policy.getCertificateType() == certificateType &&
                         policy.isEnabled())
            .findFirst()
            .orElse(null);
    }

    @Override
    public List<CertificatePolicy> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public List<CertificatePolicy> findEnabled() {
        return storage.values().stream()
            .filter(CertificatePolicy::isEnabled)
            .collect(Collectors.toList());
    }
}
