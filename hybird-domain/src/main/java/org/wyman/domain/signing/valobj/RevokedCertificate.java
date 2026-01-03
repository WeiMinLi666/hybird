package org.wyman.domain.signing.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String revocationReason;
}
