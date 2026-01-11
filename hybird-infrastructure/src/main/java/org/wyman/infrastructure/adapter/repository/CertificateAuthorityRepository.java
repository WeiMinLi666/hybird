package org.wyman.infrastructure.adapter.repository;

import org.springframework.stereotype.Repository;
import org.wyman.domain.signing.adapter.port.ICertificateAuthorityRepository;
import org.wyman.domain.signing.model.aggregate.CertificateAuthority;
import org.wyman.infrastructure.dao.mapper.CertificateAuthorityMapper;
import org.wyman.infrastructure.dao.po.CertificateAuthorityPO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 证书颁发机构仓储MyBatis实现
 */
@Repository
public class CertificateAuthorityRepository implements ICertificateAuthorityRepository {

    private final CertificateAuthorityMapper certificateAuthorityMapper;

    public CertificateAuthorityRepository(CertificateAuthorityMapper certificateAuthorityMapper) {
        this.certificateAuthorityMapper = certificateAuthorityMapper;
    }

    @Override
    public void save(CertificateAuthority ca) {
        CertificateAuthorityPO po = toPO(ca);

        // 先按名称查询是否存在
        CertificateAuthorityPO existingByName = certificateAuthorityMapper.selectAll()
            .stream()
            .filter(p -> p.getCaName().equals(ca.getCaName()))
            .findFirst()
            .orElse(null);

        if (existingByName != null) {
            // 如果按名称找到记录,使用该记录的ID进行更新
            po.setCaId(existingByName.getCaId());
            certificateAuthorityMapper.update(po);
        } else {
            // 按ID查询
            CertificateAuthorityPO existingById = certificateAuthorityMapper.selectById(ca.getCaId());
            if (existingById == null) {
                certificateAuthorityMapper.insert(po);
            } else {
                certificateAuthorityMapper.update(po);
            }
        }
    }

    @Override
    public CertificateAuthority findByName(String caName) {
        // 简化实现:实际应该在Mapper中添加按名称查询的方法
        List<CertificateAuthorityPO> poList = certificateAuthorityMapper.selectAll();
        return poList.stream()
                .filter(po -> po.getCaName().equals(caName))
                .findFirst()
                .map(this::toDomain)
                .orElse(null);
    }

    @Override
    public CertificateAuthority findById(String caId) {
        CertificateAuthorityPO po = certificateAuthorityMapper.selectById(caId);
        return toDomain(po);
    }

    @Override
    public List<CertificateAuthority> findAll() {
        List<CertificateAuthorityPO> poList = certificateAuthorityMapper.selectAll();
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    private CertificateAuthorityPO toPO(CertificateAuthority ca) {
        return CertificateAuthorityPO.builder()
                .caId(ca.getCaId())
                .caName(ca.getCaName())
                .caDn(ca.getCaCertificate() != null ? ca.getCaCertificate().getSubjectDN() : null)
                .certificatePem(ca.getCaCertificate() != null ? ca.getCaCertificate().getPemEncoded() : null)
                .privateKeyAlias(null)
                .keyType(null)
                .keySize(null)
                .enabled(true)
                .createTime(ca.getCreateTime() != null ? ca.getCreateTime() : java.time.LocalDateTime.now())
                .build();
    }

    private CertificateAuthorityPO po; // 用于临时存储

    private CertificateAuthority toDomain(CertificateAuthorityPO po) {
        if (po == null) {
            return null;
        }

        this.po = po;

        CertificateAuthority ca = new CertificateAuthority(
                po.getCaId(),
                po.getCaName(),
                po.getCertificatePem() != null ? createMockCertificate(po.getCertificatePem()) : null
        );
        ca.setCreateTime(po.getCreateTime());

        return ca;
    }

    private org.wyman.domain.signing.valobj.Certificate createMockCertificate(String pem) {
        org.wyman.domain.signing.valobj.Certificate cert = new org.wyman.domain.signing.valobj.Certificate();
        if (po != null) {
            cert.setSubjectDN(po.getCaDn());
            cert.setIssuerDN(po.getCaDn());
        }
        cert.setPemEncoded(pem);
        return cert;
    }
}
