package org.wyman.api.dto;

import lombok.Data;

/**
 * CA创建请求
 */
@Data
public class CACreateRequest {
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
     * 有效期(天)
     */
    private Integer validityDays;
}
