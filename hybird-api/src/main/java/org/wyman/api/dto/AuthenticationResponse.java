package org.wyman.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 身份验证响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 验证状态
     */
    private String status;

    /**
     * 验证成功
     */
    private boolean success;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 验证时间
     */
    private String validationTime;
}
