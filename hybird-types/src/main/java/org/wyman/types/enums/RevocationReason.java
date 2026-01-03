package org.wyman.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 吊销原因枚举
 */
@Getter
@AllArgsConstructor
public enum RevocationReason {
    KEY_COMPROMISE(1, "密钥泄露"),
    CA_COMPROMISE(2, "CA泄露"),
    AFFILIATION_CHANGED(3, "隶属关系变更"),
    SUPERSEDED(4, "已被替代"),
    CESSATION_OF_OPERATION(5, "停止运营"),
    CERTIFICATE_HOLD(6, "证书挂起"),
    REMOVE_FROM_CRL(8, "从CRL中移除"),
    PRIVILEGE_WITHDRAWN(9, "权限撤销"),
    AA_COMPROMISE(10, "AA泄露");

    private final int code;
    private final String desc;
}
