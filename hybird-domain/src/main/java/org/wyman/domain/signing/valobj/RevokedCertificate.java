package org.wyman.domain.signing.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wyman.types.enums.RevocationReason;

import java.time.LocalDateTime;

/**
 * 被吊销证书条目
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevokedCertificate {
    /**
     * 证书序列号
     */
    private String serialNumber;

    /**
     * 吊销日期
     */
    private LocalDateTime revocationDate;

    /**
     * 吊销原因
     */
    private RevocationReason reason;

    /**
     * 吊销原因(字符串，兼容旧代码)
     */
    @Deprecated
    public String getRevocationReason() {
        return reason != null ? reason.getDesc() : null;
    }

    @Deprecated
    public void setRevocationReason(String reason) {
        // 尝试从描述匹配枚举
        if (reason != null) {
            for (RevocationReason r : RevocationReason.values()) {
                if (r.getDesc().equals(reason)) {
                    this.reason = r;
                    break;
                }
            }
        }
    }
}
