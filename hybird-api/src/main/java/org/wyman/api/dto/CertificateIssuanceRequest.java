package org.wyman.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 证书签发请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateIssuanceRequest {
    /**
     * 申请者ID
     */
    @NotBlank(message = "申请者ID不能为空")
    private String applicantId;

    /**
     * IdP身份令牌
     */
    @NotBlank(message = "身份令牌不能为空")
    private String idToken;

    /**
     * CSR PEM数据
     */
    @NotBlank(message = "CSR数据不能为空")
    private String csrPemData;

    /**
     * 证书类型
     */
    @NotNull(message = "证书类型不能为空")
    private String certificateType;

    /**
     * 签名算法
     */
    @NotBlank(message = "签名算法不能为空")
    private String signatureAlgorithm;

    /**
     * 密钥封装算法
     */
    private String kemAlgorithm;

    /**
     * 有效期开始时间
     */
    private LocalDateTime notBefore;

    /**
     * 有效期结束时间
     */
    @NotNull(message = "有效期结束时间不能为空")
    private LocalDateTime notAfter;

    /**
     * CA名称
     */
    @NotBlank(message = "CA名称不能为空")
    private String caName;
}
