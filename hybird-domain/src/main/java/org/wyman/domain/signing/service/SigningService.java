package org.wyman.domain.signing.service;

import org.springframework.stereotype.Service;
import org.wyman.domain.signing.adapter.port.ICertificateAuthorityRepository;
import org.wyman.domain.signing.adapter.port.ICertificateGenerator;
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
    private final ICertificateGenerator certificateGenerator;

    public SigningService(ICertificateAuthorityRepository caRepository,
                          IObjectStorageGateway objectStorageGateway,
                          IPrivateKeyProvider keyProvider,
                          ICertificateGenerator certificateGenerator) {
        this.caRepository = caRepository;
        this.objectStorageGateway = objectStorageGateway;
        this.keyProvider = keyProvider;
        this.certificateGenerator = certificateGenerator;
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

        // 设置certificateGenerator到聚合根
        ca.setCertificateGenerator(certificateGenerator);

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

        // 设置certificateGenerator到聚合根
        ca.setCertificateGenerator(certificateGenerator);

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
     * 创建CA(通过依赖注入的CertificateGenerator)
     */
    public CertificateAuthority createCertificateAuthority(String caName,
                                                             String subjectDN,
                                                             String signatureAlgorithm,
                                                             Integer validityDays) {
        try {
            String caId = java.util.UUID.randomUUID().toString();

            // 使用依赖注入的CertificateGenerator
            // 生成CA密钥对
            java.security.KeyPair keyPair = certificateGenerator.generateKeyPair(signatureAlgorithm);

            // 计算有效期
            java.time.LocalDateTime notBefore = java.time.LocalDateTime.now();
            java.time.LocalDateTime notAfter = notBefore.plusDays(validityDays);
            java.util.Date notBeforeDate = java.util.Date.from(
                notBefore.atZone(java.time.ZoneId.systemDefault()).toInstant());
            java.util.Date notAfterDate = java.util.Date.from(
                notAfter.atZone(java.time.ZoneId.systemDefault()).toInstant());

            // 解析DN
            org.bouncycastle.asn1.x500.X500Name x500Name =
                new org.bouncycastle.asn1.x500.X500Name(subjectDN);

            // 生成CA自签名证书
            java.security.cert.X509Certificate x509CACert = certificateGenerator.generateCACertificate(
                x500Name,
                keyPair,
                notBeforeDate,
                notAfterDate,
                java.math.BigInteger.valueOf(System.currentTimeMillis()),
                signatureAlgorithm,
                "http://crl.example.com/ca.crl"
            );

            // 转换为领域模型
            org.wyman.domain.signing.valobj.Certificate caCert =
                new org.wyman.domain.signing.valobj.Certificate();
            caCert.setSerialNumber(x509CACert.getSerialNumber().toString(16));
            caCert.setSubjectDN(x509CACert.getSubjectX500Principal().getName());
            caCert.setIssuerDN(x509CACert.getIssuerX500Principal().getName());
            caCert.setSignatureAlgorithm(signatureAlgorithm);
            caCert.setPemEncoded(certificateGenerator.toPEM(x509CACert));

            CertificateAuthority ca = new CertificateAuthority(caId, caName, caCert);
            // 设置certificateGenerator到聚合根
            ca.setCertificateGenerator(certificateGenerator);
            caRepository.save(ca);
            return ca;
        } catch (Exception e) {
            throw new RuntimeException("创建CA失败: " + e.getMessage(), e);
        }
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
