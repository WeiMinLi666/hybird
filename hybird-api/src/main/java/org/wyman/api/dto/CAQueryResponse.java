package org.wyman.api.dto;

import lombok.Data;

/**
 * CA查询响应
 */
@Data
public class CAQueryResponse {
    /**
     * CA ID
     */
    private String caId;

    /**
     * CA名称
     */
    private String caName;

    /**
     * 主题DN
     */
    private String subjectDN;

    /**
     * 签名算法
     */
    private String signatureAlgorithm;

    /**
     * 状态
     */
    private String status;

    /**
     * 证书PEM编码
     */
    private String certificatePem;

    /**
     * 公钥
     */
    private String publicKey;

    /**
     * 创建时间
     */
    private String createTime;
}
