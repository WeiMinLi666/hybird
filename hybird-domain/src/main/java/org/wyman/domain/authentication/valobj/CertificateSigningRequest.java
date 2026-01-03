package org.wyman.domain.authentication.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.PublicKey;
import java.util.Map;

/**
 * 证书签名请求值对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateSigningRequest {
    /**
     * CSR编码数据(PEM/DER格式)
     */
    private String csrData;

    /**
     * 主题DN
     */
    private String subjectDN;

    /**
     * 公钥
     */
    private PublicKey publicKey;

    /**
     * 公钥算法
     */
    private String publicKeyAlgorithm;

    /**
     * 签名算法OID
     */
    private String signatureAlgorithmOid;

    /**
     * 扩展属性
     */
    private Map<String, String> extensions;

    /**
     * 签名验证是否通过
     */
    private boolean signatureValid;
}
