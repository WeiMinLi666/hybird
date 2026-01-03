package org.wyman.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 证书状态查询响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateStatusCheckResponse {
    /**
     * 证书序列号
     */
    private String serialNumber;

    /**
     * 是否已吊销
     */
    private Boolean revoked;

    /**
     * 批量查询结果映射
     */
    private Map<String, Boolean> batchResults;

    /**
     * 吊销日期
     */
    private LocalDateTime revocationDate;

    /**
     * 吊销原因
     */
    private String revocationReason;

    /**
     * CRL更新时间
     */
    private LocalDateTime crlUpdateTime;
}
