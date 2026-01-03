package org.wyman.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询证书响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateQueryResponse {
    /**
     * 证书序列号
     */
    private String serialNumber;

    /**
     * 证书类型
     */
    private String certificateType;

    /**
     * 状态
     */
    private String status;

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
     * 申请者ID
     */
    private String applicantId;

    /**
     * 签名算法
     */
    private String signatureAlgorithm;

    /**
     * 证书PEM编码
     */
    private String certificatePem;

    /**
     * CRL分发点
     */
    private String crlDistributionPoint;
}
