package org.wyman.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 身份验证请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {
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
     * 客户端IP
     */
    private String clientIp;
}
