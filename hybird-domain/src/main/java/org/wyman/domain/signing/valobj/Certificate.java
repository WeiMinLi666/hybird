package org.wyman.domain.signing.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.PublicKey;
import java.time.LocalDateTime;

/**
 * 证书值对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {
    /**
     * 证书序列号
     */
    private String serialNumber;

    /**
     * 颁发者DN
     */
    private String issuerDN;

    /**
     * 主题DN
     */
    private String subjectDN;

    /**
     * 公钥
     */
    private PublicKey publicKey;

    /**
     * 有效期开始
     */
    private LocalDateTime notBefore;

    /**
     * 有效期结束
     */
    private LocalDateTime notAfter;

    /**
     * 签名算法
     */
    private String signatureAlgorithm;

    /**
     * 证书编码(PEM格式)
     */
    private String pemEncoded;

    /**
     * CRL分发点
     */
    private String crlDistributionPoint;
}
