package org.wyman.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 证书状态枚举
 */
@Getter
@AllArgsConstructor
public enum CertificateStatus {
    PENDING_ISSUANCE("PENDING_ISSUANCE", "待签发"),
    ACTIVE("ACTIVE", "激活"),
    REVOKED("REVOKED", "已吊销"),
    EXPIRED("EXPIRED", "已过期"),
    RENEWAL_DUE("RENEWAL_DUE", "待续期");

    private final String code;
    private final String desc;
}
