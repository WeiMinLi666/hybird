package org.wyman.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 证书状态查询请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateStatusCheckRequest {
    /**
     * 单个证书序列号
     */
    private String serialNumber;

    /**
     * 批量证书序列号列表
     */
    private List<String> serialNumbers;

    /**
     * 查询类型: single/batch
     */
    private String queryType;
}
