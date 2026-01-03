package org.wyman.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 证书签发响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateIssuanceResponse {
    /**
     * 证书序列号
     */
    private String serialNumber;

    /**
     * 证书PEM编码
     */
    private String certificatePem;

    /**
     * 主题DN
     */
    private String subjectDN;

    /**
     * 颁发者DN
     */
    private String issuerDN;

    /**
     * 有效期开始
     */
    private String notBefore;

    /**
     * 有效期结束
     */
    private String notAfter;

    /**
     * 签名算法
     */
    private String signatureAlgorithm;

    /**
     * CRL分发点
     */
    private String crlDistributionPoint;
}
