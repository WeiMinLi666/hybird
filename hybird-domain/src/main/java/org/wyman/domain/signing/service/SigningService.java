package org.wyman.domain.signing.service;

import org.springframework.stereotype.Service;
import org.wyman.domain.signing.adapter.port.ICertificateAuthorityRepository;
import org.wyman.domain.signing.adapter.port.IObjectStorageGateway;
import org.wyman.domain.signing.adapter.port.IPrivateKeyProvider;
import org.wyman.domain.signing.model.aggregate.CertificateAuthority;
import org.wyman.domain.signing.valobj.CRL;
import org.wyman.domain.signing.valobj.Certificate;
import org.wyman.domain.signing.valobj.RevokedCertificate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 签名领域服务
 */
@Service
public class SigningService {

    private final ICertificateAuthorityRepository caRepository;
    private final IObjectStorageGateway objectStorageGateway;
    private final IPrivateKeyProvider keyProvider;

    public SigningService(ICertificateAuthorityRepository caRepository,
                          IObjectStorageGateway objectStorageGateway,
                          IPrivateKeyProvider keyProvider) {
        this.caRepository = caRepository;
        this.objectStorageGateway = objectStorageGateway;
        this.keyProvider = keyProvider;
    }

    /**
     * 签发证书
     */
    public Certificate issueCertificate(String caName,
                                       String subjectDN,
                                       java.security.PublicKey publicKey,
                                       LocalDateTime notBefore,
                                       LocalDateTime notAfter,
                                       String signatureAlgorithm,
                                       String kemAlgorithm) {
        CertificateAuthority ca = caRepository.findByName(caName);
        if (ca == null) {
            throw new RuntimeException("CA不存在: " + caName);
        }

        Certificate certificate = ca.issueCertificate(
            subjectDN,
            publicKey,
            notBefore,
            notAfter,
            signatureAlgorithm,
            kemAlgorithm,
            keyProvider
        );

        // 保存CA状态
        caRepository.save(ca);

        return certificate;
    }

    /**
     * 生成CRL
     */
    public CRL generateCRL(String caName,
                           List<RevokedCertificate> revokedCerts,
                           String signatureAlgorithm) {
        CertificateAuthority ca = caRepository.findByName(caName);
        if (ca == null) {
            throw new RuntimeException("CA不存在: " + caName);
        }

        CRL crl = ca.generateCRL(revokedCerts, keyProvider, signatureAlgorithm);

        // 上传CRL到对象存储
        objectStorageGateway.uploadCRL(crl.getCrlNumber(), crl.getPemEncoded());

        // 保存CA状态
        caRepository.save(ca);

        return crl;
    }

    /**
     * 获取所有CA
     */
    public java.util.List<CertificateAuthority> getAllCertificateAuthorities() {
        return caRepository.findAll();
    }

    /**
     * 根据ID获取CA
     */
    public CertificateAuthority getCertificateAuthorityById(String caId) {
        return caRepository.findById(caId);
    }

    /**
     * 创建CA
     */
    public CertificateAuthority createCertificateAuthority(String caName,
                                                             String subjectDN,
                                                             String signatureAlgorithm,
                                                             Integer validityDays) {
        String caId = java.util.UUID.randomUUID().toString();
        // 创建一个模拟的CA证书
        org.wyman.domain.signing.valobj.Certificate caCert = new org.wyman.domain.signing.valobj.Certificate();
        caCert.setSerialNumber(caId);
        caCert.setSubjectDN(subjectDN);
        caCert.setIssuerDN(subjectDN);
        caCert.setSignatureAlgorithm(signatureAlgorithm);
        caCert.setPemEncoded("-----BEGIN CERTIFICATE-----\nMOCK_CA_CERTIFICATE\n-----END CERTIFICATE-----");

        CertificateAuthority ca = new CertificateAuthority(caId, caName, caCert);
        caRepository.save(ca);
        return ca;
    }

    /**
     * 激活/停用CA
     */
    public void activateCertificateAuthority(String caId, boolean active) {
        CertificateAuthority ca = caRepository.findById(caId);
        if (ca == null) {
            throw new RuntimeException("CA不存在: " + caId);
        }
        // 简化实现，实际CA状态管理需要更复杂
        caRepository.save(ca);
    }

    /**
     * 吊销CA
     */
    public void revokeCertificateAuthority(String caId, String reason) {
        CertificateAuthority ca = caRepository.findById(caId);
        if (ca == null) {
            throw new RuntimeException("CA不存在: " + caId);
        }
        // 简化实现，实际CA吊销需要更复杂
        caRepository.save(ca);
    }
}
