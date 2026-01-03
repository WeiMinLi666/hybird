package org.wyman.infrastructure.adapter.repository;

import org.springframework.stereotype.Repository;
import org.wyman.domain.signing.adapter.port.ICertificateAuthorityRepository;
import org.wyman.domain.signing.model.aggregate.CertificateAuthority;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 证书颁发机构仓储内存实现
 */
@Repository
public class InMemoryCertificateAuthorityRepository implements ICertificateAuthorityRepository {

    private final Map<String, CertificateAuthority> storage = new HashMap<>();

    @Override
    public void save(CertificateAuthority ca) {
        storage.put(ca.getCaId(), ca);
    }

    @Override
    public CertificateAuthority findByName(String caName) {
        return storage.values().stream()
            .filter(ca -> caName.equals(ca.getCaName()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public CertificateAuthority findById(String caId) {
        return storage.get(caId);
    }

    @Override
    public List<CertificateAuthority> findAll() {
        return List.copyOf(storage.values());
    }
}
