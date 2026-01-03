package org.wyman.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询证书请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateQueryRequest {
    /**
     * 证书序列号
     */
    @NotBlank(message = "证书序列号不能为空")
    private String serialNumber;
}
