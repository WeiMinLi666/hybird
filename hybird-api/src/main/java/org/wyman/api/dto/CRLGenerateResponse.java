package org.wyman.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CRL生成响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CRLGenerateResponse {
    /**
     * CRL编号
     */
    private String crlNumber;

    /**
     * CRL PEM编码
     */
    private String crlPem;

    /**
     * CRL URL
     */
    private String crlUrl;

    /**
     * 本次更新时间
     */
    private LocalDateTime thisUpdate;

    /**
     * 下次更新时间
     */
    private LocalDateTime nextUpdate;

    /**
     * 吊销证书数量
     */
    private int revokedCount;
}
