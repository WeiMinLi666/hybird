package org.wyman.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 身份验证请求状态
 */
@Getter
@AllArgsConstructor
public enum AuthRequestStatus {
    PENDING_VALIDATION("PENDING_VALIDATION", "待验证"),
    VALIDATION_SUCCESSFUL("VALIDATION_SUCCESSFUL", "验证成功"),
    VALIDATION_FAILED("VALIDATION_FAILED", "验证失败");

    private final String code;
    private final String desc;
}
