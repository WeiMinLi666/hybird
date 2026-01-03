package org.wyman.domain.signing.adapter.port;

import org.wyman.domain.signing.model.aggregate.CertificateAuthority;

/**
 * 证书颁发机构仓储接口
 */
public interface ICertificateAuthorityRepository {
    /**
     * 保存CA
     */
    void save(CertificateAuthority ca);

    /**
     * 根据CA名称查询
     */
    CertificateAuthority findByName(String caName);

    /**
     * 根据CA ID查询
     */
    CertificateAuthority findById(String caId);

    /**
     * 查询所有CA
     */
    java.util.List<CertificateAuthority> findAll();
}
