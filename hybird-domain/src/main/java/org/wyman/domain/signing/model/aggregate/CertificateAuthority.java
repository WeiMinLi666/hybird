package org.wyman.domain.signing.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wyman.domain.signing.adapter.port.ICertificateGenerator;
import org.wyman.domain.signing.adapter.port.IPrivateKeyProvider;
import org.wyman.domain.signing.valobj.CRL;
import org.wyman.domain.signing.valobj.Certificate;
import org.wyman.domain.signing.valobj.RevokedCertificate;
import org.wyman.types.event.CRLIssuedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 证书颁发机构聚合根
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateAuthority {
    /**
     * CA ID
     */
    private String caId;

    /**
     * CA名称
     */
    private String caName;

    /**
     * CA证书
     */
    private Certificate caCertificate;

    /**
     * 下一次CRL编号
     */
    private long nextCrlNumber;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 证书生成器(通过依赖注入)
     */
    private transient ICertificateGenerator certificateGenerator;

    /**
     * 领域事件集合
     */
    private transient List<Object> domainEvents = new ArrayList<>();

    public CertificateAuthority(String caId, String caName, Certificate caCertificate) {
        this.caId = caId;
        this.caName = caName;
        this.caCertificate = caCertificate;
        this.nextCrlNumber = 1;
        this.createTime = LocalDateTime.now();
    }

    public void setCertificateGenerator(ICertificateGenerator certificateGenerator) {
        this.certificateGenerator = certificateGenerator;
    }

    /**
     * 签发证书(通过依赖注入的CertificateGenerator)
     */
    public Certificate issueCertificate(String subjectDN,
                                       java.security.PublicKey publicKey,
                                       LocalDateTime notBefore,
                                       LocalDateTime notAfter,
                                       String signatureAlgorithm,
                                       String kemAlgorithm,
                                       IPrivateKeyProvider keyProvider,
                                       org.wyman.domain.signing.valobj.HybridCertificateRequestContext hybridContext) {
        try {
            // 获取CA私钥
            java.security.PrivateKey caPrivateKey = keyProvider.getSigningPrivateKey(signatureAlgorithm);
            if (hybridContext != null && hybridContext.isAltSignatureRequired()) {
                if (hybridContext.getAltSignatureJcaName() == null) {
                    hybridContext.setAltSignatureJcaName(signatureAlgorithm);
                }
                if (hybridContext.getAltSignaturePrivateKey() == null) {
                    String altAlg = hybridContext.getAltSignatureJcaName();
                    hybridContext.setAltSignaturePrivateKey(keyProvider.getAltSigningPrivateKey(altAlg));
                }
            }

            // 解析DN
            org.bouncycastle.asn1.x500.X500Name issuerX500Name =
                new org.bouncycastle.asn1.x500.X500Name(caCertificate.getSubjectDN());
            org.bouncycastle.asn1.x500.X500Name subjectX500Name =
                new org.bouncycastle.asn1.x500.X500Name(subjectDN);

            // 转换日期
            java.util.Date notBeforeDate = java.util.Date.from(
                notBefore.atZone(java.time.ZoneId.systemDefault()).toInstant());
            java.util.Date notAfterDate = java.util.Date.from(
                notAfter.atZone(java.time.ZoneId.systemDefault()).toInstant());

            // 生成序列号
            java.math.BigInteger serialNumber = java.math.BigInteger.valueOf(System.currentTimeMillis());

            // 使用依赖注入的CertificateGenerator
            java.security.cert.X509Certificate x509Cert = certificateGenerator.generateCertificate(
                issuerX500Name,
                subjectX500Name,
                publicKey,
                caPrivateKey,
                notBeforeDate,
                notAfterDate,
                serialNumber,
                signatureAlgorithm,
                "http://crl.example.com/ca.crl",
                hybridContext
            );

            // 转换为领域模型
            Certificate cert = new Certificate();
            cert.setSerialNumber(serialNumber.toString(16));
            cert.setIssuerDN(x509Cert.getIssuerX500Principal().getName());
            cert.setSubjectDN(x509Cert.getSubjectX500Principal().getName());
            cert.setPublicKey(publicKey);
            cert.setNotBefore(notBefore);
            cert.setNotAfter(notAfter);
            cert.setSignatureAlgorithm(signatureAlgorithm);
            cert.setCrlDistributionPoint("http://crl.example.com/ca.crl");
            cert.setPemEncoded(certificateGenerator.toPEM(x509Cert));

            if (hybridContext != null && hybridContext.isHybridEnabled()) {
                cert.setPostQuantumPublicKeyPem(hybridContext.getPqSignaturePublicKeyPem());
                cert.setPostQuantumKekPublicKeyPem(hybridContext.getPqKekPublicKeyPem());
            }

            return cert;
        } catch (Exception e) {
            throw new RuntimeException("签发证书失败: " + e.getMessage(), e);
        }
    }



    /**
     * 生成CRL(通过依赖注入的CertificateGenerator)
     */
    public CRL generateCRL(List<RevokedCertificate> revokedCerts,
                          IPrivateKeyProvider keyProvider,
                          String signatureAlgorithm) {
        try {
            // 获取CA私钥
            java.security.PrivateKey caPrivateKey = keyProvider.getSigningPrivateKey(signatureAlgorithm);

            // 解析发行者DN
            org.bouncycastle.asn1.x500.X500Name issuerX500Name =
                new org.bouncycastle.asn1.x500.X500Name(caCertificate.getSubjectDN());

            // 转换日期
            java.util.Date thisUpdate = new java.util.Date();
            java.util.Date nextUpdate = new java.util.Date(
                System.currentTimeMillis() + 24 * 60 * 60 * 1000);

            // 使用依赖注入的CertificateGenerator
            java.security.cert.X509CRL x509CRL = certificateGenerator.generateCRL(
                issuerX500Name,
                caPrivateKey,
                thisUpdate,
                nextUpdate,
                java.math.BigInteger.valueOf(nextCrlNumber++),
                signatureAlgorithm,
                revokedCerts
            );

            // 获取CRL编号
            java.math.BigInteger crlNum = certificateGenerator.getCRLNumber(x509CRL);

            // 转换为领域模型
            CRL crl = new CRL();
            crl.setCrlNumber(crlNum.toString());
            crl.setIssuerDN(x509CRL.getIssuerX500Principal().getName());
            crl.setThisUpdate(LocalDateTime.ofInstant(
                x509CRL.getThisUpdate().toInstant(), java.time.ZoneId.systemDefault()));
            crl.setNextUpdate(LocalDateTime.ofInstant(
                x509CRL.getNextUpdate().toInstant(), java.time.ZoneId.systemDefault()));
            crl.setRevokedCertificates(revokedCerts);
            crl.setSignatureAlgorithm(signatureAlgorithm);
            crl.setPemEncoded(certificateGenerator.toPEM(x509CRL));

            // 发布CRL事件
            addDomainEvent(new CRLIssuedEvent(
                crl.getCrlNumber(),
                "http://crl.example.com/crl-" + crl.getCrlNumber() + ".crl",
                revokedCerts != null ? revokedCerts.size() : 0
            ));

            return crl;
        } catch (Exception e) {
            throw new RuntimeException("生成CRL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加领域事件
     */
    private void addDomainEvent(Object event) {
        if (this.domainEvents == null) {
            this.domainEvents = new ArrayList<>();
        }
        this.domainEvents.add(event);
    }

    /**
     * 获取并清空领域事件
     */
    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }
}
