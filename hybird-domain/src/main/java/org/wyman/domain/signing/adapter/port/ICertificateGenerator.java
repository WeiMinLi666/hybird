package org.wyman.domain.signing.adapter.port;

import org.bouncycastle.asn1.x500.X500Name;
import org.wyman.domain.signing.valobj.HybridCertificateRequestContext;
import org.wyman.domain.signing.valobj.RevokedCertificate;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

/**
 * 证书生成器接口(Domain层定义)
 */
public interface ICertificateGenerator {

    /**
     * 生成密钥对
     */
    KeyPair generateKeyPair(String algorithm) throws Exception;

    /**
     * 生成X.509证书
     */
    X509Certificate generateCertificate(
            X500Name issuer,
            X500Name subject,
            PublicKey publicKey,
            PrivateKey issuerPrivateKey,
            Date notBefore,
            Date notAfter,
            BigInteger serialNumber,
            String signatureAlgorithm,
            String crlDistributionPoint,
            HybridCertificateRequestContext hybridContext
    ) throws Exception;

    /**
     * 生成CA证书(自签名)
     */
    X509Certificate generateCACertificate(
            X500Name subject,
            KeyPair keyPair,
            Date notBefore,
            Date notAfter,
            BigInteger serialNumber,
            String signatureAlgorithm,
            String crlDistributionPoint
    ) throws Exception;

    /**
     * 生成CRL
     */
    X509CRL generateCRL(
            X500Name issuer,
            PrivateKey issuerPrivateKey,
            Date thisUpdate,
            Date nextUpdate,
            BigInteger crlNumber,
            String signatureAlgorithm,
            List<RevokedCertificate> revokedCerts
    ) throws Exception;

    /**
     * 转换证书为PEM格式
     */
    String toPEM(X509Certificate cert);

    /**
     * 转换CRL为PEM格式
     */
    String toPEM(X509CRL crl);

    /**
     * 验证证书签名
     */
    boolean verifyCertificateSignature(X509Certificate cert, PublicKey issuerPublicKey);

    /**
     * 从CRL获取CRL编号
     */
    BigInteger getCRLNumber(X509CRL crl);
}
