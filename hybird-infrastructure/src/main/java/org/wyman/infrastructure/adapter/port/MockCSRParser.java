package org.wyman.infrastructure.adapter.port;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.springframework.stereotype.Component;
import org.wyman.domain.authentication.adapter.port.ICSRParser;
import org.wyman.domain.authentication.valobj.CertificateSigningRequest;
import org.wyman.types.constants.HybridCertificateOids;

import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * CSR解析器实现(Bouncy Castle)
 */
@Component
public class MockCSRParser implements ICSRParser {

    static {
        java.security.Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public CertificateSigningRequest parsePEM(String pemData) {
        try {
            // 移除PEM头尾
            String normalized = pemData
                .replace("-----BEGIN CERTIFICATE REQUEST-----", "")
                .replace("-----END CERTIFICATE REQUEST-----", "")
                .replace("-----BEGIN NEW CERTIFICATE REQUEST-----", "")
                .replace("-----END NEW CERTIFICATE REQUEST-----", "")
                .replaceAll("\\s", "");

            byte[] derData = Base64.getDecoder().decode(normalized);

            // 使用Bouncy Castle解析CSR
            PKCS10CertificationRequest csrHolder = new PKCS10CertificationRequest(derData);
            JcaPKCS10CertificationRequest csr = new JcaPKCS10CertificationRequest(csrHolder);

            CertificateSigningRequest csrVO = new CertificateSigningRequest();
            csrVO.setCsrData(pemData);
            csrVO.setSubjectDN(csr.getSubject().toString());
            PublicKey publicKey = csr.getPublicKey();
            csrVO.setPublicKey(publicKey);
            csrVO.setSignatureAlgorithmOid(csr.getSignatureAlgorithm().getAlgorithm().getId());

            // 验证签名
            ContentVerifierProvider verifierProvider = new JcaContentVerifierProviderBuilder()
                .setProvider(new BouncyCastleProvider())
                .build(csr.getPublicKey());
            csrVO.setSignatureValid(csr.isSignatureValid(verifierProvider));

            // 获取公钥算法
            String algorithm = publicKey.getAlgorithm();
            csrVO.setPublicKeyAlgorithm(algorithm);

            Map<String, String> attributes = extractAttributes(csrHolder, csrVO);
            csrVO.setExtensions(attributes);

            return csrVO;

        } catch (Exception e) {
            throw new RuntimeException("解析CSR失败", e);
        }
    }

    @Override
    public CertificateSigningRequest parseDER(byte[] derData) {
        try {
            PKCS10CertificationRequest csrHolder = new PKCS10CertificationRequest(derData);
            JcaPKCS10CertificationRequest csr = new JcaPKCS10CertificationRequest(csrHolder);

            CertificateSigningRequest csrVO = new CertificateSigningRequest();
            csrVO.setCsrData(Base64.getEncoder().encodeToString(derData));
            csrVO.setSubjectDN(csr.getSubject().toString());
            PublicKey publicKey = csr.getPublicKey();
            csrVO.setPublicKey(publicKey);
            csrVO.setSignatureAlgorithmOid(csr.getSignatureAlgorithm().getAlgorithm().getId());

            // 验证签名
            ContentVerifierProvider verifierProvider = new JcaContentVerifierProviderBuilder()
                .setProvider(new BouncyCastleProvider())
                .build(csr.getPublicKey());
            csrVO.setSignatureValid(csr.isSignatureValid(verifierProvider));

            Map<String, String> attributes = extractAttributes(csrHolder, csrVO);
            csrVO.setExtensions(attributes);

            return csrVO;
        } catch (Exception e) {
            throw new RuntimeException("解析CSR失败", e);
        }
    }

    @Override
    public boolean verifySignature(CertificateSigningRequest csr) {
        try {
            String pemData = csr.getCsrData();
            return parsePEM(pemData).isSignatureValid();
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, String> extractAttributes(PKCS10CertificationRequest csrHolder,
                                                  CertificateSigningRequest target) {
        Attribute[] attrs = csrHolder.getAttributes();
        if (attrs == null || attrs.length == 0) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        for (Attribute attribute : attrs) {
            String oid = attribute.getAttrType().getId();
            ASN1Encodable value = attribute.getAttrValues().getObjectAt(0);
            String stringValue = toAttributeString(value);
            map.put(oid, stringValue);

            if (HybridCertificateOids.ATTR_PQC_SIGNATURE_PUBLIC_KEY_INFO.equals(oid)) {
                target.setPqSignaturePublicKeyPem(stringValue);
            } else if (HybridCertificateOids.ATTR_PQC_KEK_PUBLIC_KEY_INFO.equals(oid)) {
                target.setPqKekPublicKeyPem(stringValue);
            } else if (HybridCertificateOids.ATTR_PQC_SIGNATURE_VALUE.equals(oid)) {
                target.setPqSignatureValue(toAttributeBytes(value));
            } else if (HybridCertificateOids.ATTR_PQC_KEK_POP_PROOF.equals(oid)) {
                target.setPqKekProofValue(toAttributeBytes(value));
            }
        }
        return map;
    }

    private String toAttributeString(ASN1Encodable value) {
        if (value instanceof ASN1String asn1String) {
            return asn1String.getString();
        }
        if (value instanceof DEROctetString octet) {
            return Base64.getEncoder().encodeToString(octet.getOctets());
        }
        return value.toString();
    }

    private byte[] toAttributeBytes(ASN1Encodable value) {
        if (value instanceof DEROctetString octet) {
            return octet.getOctets();
        }
        return toAttributeString(value).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
