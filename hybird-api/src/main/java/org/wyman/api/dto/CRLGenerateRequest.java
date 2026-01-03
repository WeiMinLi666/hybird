package org.wyman.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CRL生成请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CRLGenerateRequest {
    /**
     * CA名称
     */
    @NotBlank(message = "CA名称不能为空")
    private String caName;

    /**
     * 签名算法
     */
    @NotBlank(message = "签名算法不能为空")
    private String signatureAlgorithm;

    /**
     * 操作者
     */
    @NotBlank(message = "操作者不能为空")
    private String operator;
}
