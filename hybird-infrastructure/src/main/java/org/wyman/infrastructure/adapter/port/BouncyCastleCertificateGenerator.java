package org.wyman.infrastructure.adapter.port;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Component;
import org.wyman.domain.signing.adapter.port.ICertificateGenerator;
import org.wyman.domain.signing.valobj.RevokedCertificate;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

/**
 * Bouncy Castle证书生成器实现(Infrastructure层)
 */
@Component
public class BouncyCastleCertificateGenerator implements ICertificateGenerator {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Override
    public X509Certificate generateCertificate(
            X500Name issuer,
            X500Name subject,
            PublicKey publicKey,
            PrivateKey issuerPrivateKey,
            Date notBefore,
            Date notAfter,
            BigInteger serialNumber,
            String signatureAlgorithm,
            String crlDistributionPoint
    ) throws Exception {
        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
                issuer,
                serialNumber,
                notBefore,
                notAfter,
                subject,
                SubjectPublicKeyInfo.getInstance(publicKey.getEncoded())
        );

        // 添加CRL分发点扩展
        if (crlDistributionPoint != null) {
            DistributionPointName dpn = new DistributionPointName(
                    new GeneralNames(new GeneralName(GeneralName.uniformResourceIdentifier, crlDistributionPoint))
            );
            certBuilder.addExtension(Extension.cRLDistributionPoints, false,
                    new CRLDistPoint(new DistributionPoint[]{new DistributionPoint(dpn, null, null)}));
        }

        // 添加密钥用法扩展
        certBuilder.addExtension(Extension.keyUsage, true,
                new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment | KeyUsage.keyCertSign));

        // 添加基本约束扩展(非CA)
        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));

        String signerAlgorithm = getSignerAlgorithm(signatureAlgorithm);

        ContentSigner signer = new JcaContentSignerBuilder(signerAlgorithm)
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build(issuerPrivateKey);

        X509CertificateHolder certHolder = certBuilder.build(signer);
        return new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(certHolder);
    }

    @Override
    public X509Certificate generateCACertificate(
            X500Name subject,
            KeyPair keyPair,
            Date notBefore,
            Date notAfter,
            BigInteger serialNumber,
            String signatureAlgorithm,
            String crlDistributionPoint
    ) throws Exception {
        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
                subject,
                serialNumber,
                notBefore,
                notAfter,
                subject,
                SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded())
        );

        // 添加CRL分发点扩展
        if (crlDistributionPoint != null) {
            DistributionPointName dpn = new DistributionPointName(
                    new GeneralNames(new GeneralName(GeneralName.uniformResourceIdentifier, crlDistributionPoint))
            );
            certBuilder.addExtension(Extension.cRLDistributionPoints, false,
                    new CRLDistPoint(new DistributionPoint[]{new DistributionPoint(dpn, null, null)}));
        }

        // 添加密钥用法扩展(CA)
        certBuilder.addExtension(Extension.keyUsage, true,
                new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign | KeyUsage.cRLSign));

        // 添加基本约束扩展(CA)
        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));

        // 生成SKID和AKID
        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                extUtils.createSubjectKeyIdentifier(keyPair.getPublic()));
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false,
                extUtils.createAuthorityKeyIdentifier(keyPair.getPublic()));

        String signerAlgorithm = getSignerAlgorithm(signatureAlgorithm);

        ContentSigner signer = new JcaContentSignerBuilder(signerAlgorithm)
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build(keyPair.getPrivate());

        X509CertificateHolder certHolder = certBuilder.build(signer);
        return new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(certHolder);
    }

    @Override
    public X509CRL generateCRL(
            X500Name issuer,
            PrivateKey issuerPrivateKey,
            Date thisUpdate,
            Date nextUpdate,
            BigInteger crlNumber,
            String signatureAlgorithm,
            List<RevokedCertificate> revokedCerts
    ) throws Exception {
        X509v2CRLBuilder crlBuilder = new X509v2CRLBuilder(issuer, thisUpdate);
        crlBuilder.setNextUpdate(nextUpdate);

        // 添加CRL编号扩展
        crlBuilder.addExtension(Extension.cRLNumber, false, new ASN1Integer(crlNumber));

        // 添加吊销证书列表
        if (revokedCerts != null) {
            for (RevokedCertificate revokedCert : revokedCerts) {
                Date revocationDate = Date.from(revokedCert.getRevocationDate()
                        .atZone(java.time.ZoneId.systemDefault()).toInstant());
                BigInteger serial = new BigInteger(revokedCert.getSerialNumber());

                // 添加吊销原因扩展
                ExtensionsGenerator extGen = new ExtensionsGenerator();
                extGen.addExtension(Extension.reasonCode, false,
                        CRLReason.lookup(revokedCert.getReason().getCode()));

                crlBuilder.addCRLEntry(serial, revocationDate, extGen.generate());
            }
        }

        String signerAlgorithm = getSignerAlgorithm(signatureAlgorithm);

        ContentSigner signer = new JcaContentSignerBuilder(signerAlgorithm)
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build(issuerPrivateKey);

        X509CRLHolder crlHolder = crlBuilder.build(signer);
        return new JcaX509CRLConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCRL(crlHolder);
    }

    /**
     * 获取签名算法标识
     */
    private String getSignerAlgorithm(String signatureAlgorithm) {
        switch (signatureAlgorithm) {
            case "SM2":
                return "SM3withSM2";
            case "SHA256withRSA":
            case "RSA2048":
            case "RSA4096":
                return "SHA256withRSA";
            case "SHA384withRSA":
                return "SHA384withRSA";
            case "SHA512withRSA":
                return "SHA512withRSA";
            case "SHA256withECDSA":
            case "ECDSA_P256":
                return "SHA256withECDSA";
            case "SHA384withECDSA":
                return "SHA384withECDSA";
            case "SHA512withECDSA":
                return "SHA512withECDSA";
            default:
                return "SHA256withRSA";
        }
    }

    @Override
    public String toPEM(X509Certificate cert) {
        try {
            String base64 = java.util.Base64.getMimeEncoder(64, "\n".getBytes())
                    .encodeToString(cert.getEncoded());
            return "-----BEGIN CERTIFICATE-----\n" + base64 + "\n-----END CERTIFICATE-----";
        } catch (Exception e) {
            throw new RuntimeException("转换证书为PEM失败", e);
        }
    }

    @Override
    public String toPEM(X509CRL crl) {
        try {
            String base64 = java.util.Base64.getMimeEncoder(64, "\n".getBytes())
                    .encodeToString(crl.getEncoded());
            return "-----BEGIN X509 CRL-----\n" + base64 + "\n-----END X509 CRL-----";
        } catch (CRLException e) {
            throw new RuntimeException("转换CRL为PEM失败", e);
        }
    }

    @Override
    public KeyPair generateKeyPair(String algorithm) throws Exception {
        KeyPairGenerator keyPairGenerator;

        switch (algorithm) {
            case "SM2":
                keyPairGenerator = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
                keyPairGenerator.initialize(new java.security.spec.ECGenParameterSpec("sm2p256v1"));
                break;
            case "RSA2048":
                keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                break;
            case "RSA4096":
                keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(4096);
                break;
            case "ECDSA_P256":
                keyPairGenerator = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
                keyPairGenerator.initialize(new java.security.spec.ECGenParameterSpec("secp256r1"));
                break;
            default:
                keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
        }

        return keyPairGenerator.generateKeyPair();
    }

    @Override
    public boolean verifyCertificateSignature(X509Certificate cert, PublicKey issuerPublicKey) {
        try {
            cert.verify(issuerPublicKey, BouncyCastleProvider.PROVIDER_NAME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public BigInteger getCRLNumber(X509CRL crl) {
        try {
            byte[] crlNumberExt = crl.getExtensionValue(Extension.cRLNumber.getId());
            if (crlNumberExt != null) {
                ASN1InputStream asn1In = new ASN1InputStream(crlNumberExt);
                ASN1Encodable obj = asn1In.readObject();
                asn1In.close();
                return ASN1Integer.getInstance(obj).getValue();
            }
            return BigInteger.ZERO;
        } catch (Exception e) {
            return BigInteger.ZERO;
        }
    }
}
