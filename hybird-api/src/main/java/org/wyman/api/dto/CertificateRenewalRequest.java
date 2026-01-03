package org.wyman.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 续期证书请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRenewalRequest {
    /**
     * 原证书序列号
     */
    @NotBlank(message = "原证书序列号不能为空")
    private String oldSerialNumber;

    /**
     * 新CSR PEM数据
     */
    @NotBlank(message = "CSR数据不能为空")
    private String csrPemData;

    /**
     * 新证书有效期结束时间
     */
    @NotNull(message = "有效期结束时间不能为空")
    private LocalDateTime notAfter;

    /**
     * 操作者
     */
    @NotBlank(message = "操作者不能为空")
    private String operator;
}
