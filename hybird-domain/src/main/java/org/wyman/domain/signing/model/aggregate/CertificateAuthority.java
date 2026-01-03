package org.wyman.domain.signing.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    /**
     * 签发证书(使用Catalyst模型破解签名循环依赖)
     */
    public Certificate issueCertificate(String subjectDN,
                                       java.security.PublicKey publicKey,
                                       LocalDateTime notBefore,
                                       LocalDateTime notAfter,
                                       String signatureAlgorithm,
                                       String kemAlgorithm,
                                       IPrivateKeyProvider keyProvider) {
        // 创建预签名证书结构(不包含签名)
        Certificate cert = new Certificate();
        cert.setSerialNumber(java.math.BigInteger.valueOf(System.currentTimeMillis()).toString(16));
        cert.setIssuerDN(caCertificate.getSubjectDN());
        cert.setSubjectDN(subjectDN);
        cert.setPublicKey(publicKey);
        cert.setNotBefore(notBefore);
        cert.setNotAfter(notAfter);
        cert.setSignatureAlgorithm(signatureAlgorithm);
        cert.setCrlDistributionPoint("http://crl.example.com/ca.crl");

        // 执行签名操作
        byte[] signature = performSignature(cert, keyProvider, signatureAlgorithm, kemAlgorithm);

        // 这里应该使用Bouncy Castle生成最终的证书编码
        // 简化实现,实际需要调用Bouncy Castle API生成完整的X.509证书
        cert.setPemEncoded(generatePemEncoded(cert, signature));

        return cert;
    }

    /**
     * 执行签名(支持混合签名)
     */
    private byte[] performSignature(Certificate cert,
                                    IPrivateKeyProvider keyProvider,
                                    String signatureAlgorithm,
                                    String kemAlgorithm) {
        // 获取签名私钥
        java.security.PrivateKey privateKey = keyProvider.getSigningPrivateKey(signatureAlgorithm);

        try {
            // 使用Bouncy Castle进行签名
            // 简化实现,实际需要根据不同算法调用相应的签名方法
            java.security.Signature signer = java.security.Signature.getInstance(
                signatureAlgorithm.equals("SM2") ? "SM3withSM2" : signatureAlgorithm,
                org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME
            );
            signer.initSign(privateKey);
            signer.update(getTbsData(cert));
            return signer.sign();
        } catch (Exception e) {
            throw new RuntimeException("签名失败", e);
        }
    }

    /**
     * 获取待签名数据(TBS - To Be Signed)
     */
    private byte[] getTbsData(Certificate cert) {
        // 生成证书TBS数据(不包含签名的部分)
        String tbsString = cert.getSubjectDN() + cert.getIssuerDN() +
                          cert.getNotBefore() + cert.getNotAfter();
        return tbsString.getBytes();
    }

    /**
     * 生成PEM编码
     */
    private String generatePemEncoded(Certificate cert, byte[] signature) {
        // 简化实现,实际需要使用Bouncy Castle生成完整的X.509证书
        return "-----BEGIN CERTIFICATE-----\n" +
               java.util.Base64.getEncoder().encodeToString(signature) +
               "\n-----END CERTIFICATE-----";
    }

    /**
     * 生成CRL
     */
    public CRL generateCRL(List<RevokedCertificate> revokedCerts,
                          IPrivateKeyProvider keyProvider,
                          String signatureAlgorithm) {
        CRL crl = new CRL();
        crl.setCrlNumber(String.valueOf(nextCrlNumber++));
        crl.setIssuerDN(caCertificate.getSubjectDN());
        crl.setThisUpdate(LocalDateTime.now());
        crl.setNextUpdate(LocalDateTime.now().plusDays(1));
        crl.setRevokedCertificates(revokedCerts);
        crl.setSignatureAlgorithm(signatureAlgorithm);

        // 执行CRL签名
        byte[] signature = signCRL(crl, keyProvider, signatureAlgorithm);
        crl.setPemEncoded(generateCRLPemEncoded(crl, signature));

        // 发布CRL事件
        addDomainEvent(new CRLIssuedEvent(
            crl.getCrlNumber(),
            "http://crl.example.com/crl-" + crl.getCrlNumber() + ".crl",
            revokedCerts != null ? revokedCerts.size() : 0
        ));

        return crl;
    }

    /**
     * 签名CRL
     */
    private byte[] signCRL(CRL crl, IPrivateKeyProvider keyProvider, String signatureAlgorithm) {
        java.security.PrivateKey privateKey = keyProvider.getSigningPrivateKey(signatureAlgorithm);

        try {
            java.security.Signature signer = java.security.Signature.getInstance(
                signatureAlgorithm.equals("SM2") ? "SM3withSM2" : signatureAlgorithm,
                org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME
            );
            signer.initSign(privateKey);
            signer.update(getCRLTbsData(crl));
            return signer.sign();
        } catch (Exception e) {
            throw new RuntimeException("CRL签名失败", e);
        }
    }

    /**
     * 获取CRL待签名数据
     */
    private byte[] getCRLTbsData(CRL crl) {
        String tbsString = crl.getIssuerDN() + crl.getThisUpdate() + crl.getNextUpdate();
        return tbsString.getBytes();
    }

    /**
     * 生成CRL PEM编码
     */
    private String generateCRLPemEncoded(CRL crl, byte[] signature) {
        return "-----BEGIN X509 CRL-----\n" +
               java.util.Base64.getEncoder().encodeToString(signature) +
               "\n-----END X509 CRL-----";
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
