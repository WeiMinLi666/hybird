package org.wyman.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 证书吊销请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRevocationRequest {
    /**
     * 证书序列号
     */
    @NotBlank(message = "证书序列号不能为空")
    private String serialNumber;

    /**
     * 吊销原因代码
     */
    @NotNull(message = "吊销原因不能为空")
    private Integer reasonCode;

    /**
     * 操作者
     */
    @NotBlank(message = "操作者不能为空")
    private String operator;

    /**
     * 备注
     */
    private String comments;
}
