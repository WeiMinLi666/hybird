package org.wyman.infrastructure.adapter.port;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.springframework.stereotype.Component;
import org.wyman.domain.authentication.adapter.port.ICSRParser;
import org.wyman.domain.authentication.valobj.CertificateSigningRequest;

import java.security.PublicKey;
import java.util.Base64;

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
}
