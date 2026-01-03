package org.wyman.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 申请证书请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateApplyRequest {
    /**
     * 申请者ID
     */
    @NotBlank(message = "申请者ID不能为空")
    private String applicantId;

    /**
     * 申请者姓名
     */
    @NotBlank(message = "申请者姓名不能为空")
    private String applicantName;

    /**
     * 申请者邮箱
     */
    private String applicantEmail;

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
     * 证书类型(CLASSICAL, HYBRID, POST_QUANTUM)
     */
    @NotNull(message = "证书类型不能为空")
    private String certificateType;

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
