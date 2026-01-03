package org.wyman.domain.lifecycle.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 吊销信息值对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevocationInfo {
    /**
     * 吊销时间
     */
    private LocalDateTime revocationTime;

    /**
     * 吊销原因
     */
    private String revocationReason;

    /**
     * 吊销操作人
     */
    private String revokedBy;

    /**
     * 吊销备注
     */
    private String comments;

    public RevocationInfo(String revocationReason, String revokedBy) {
        this.revocationTime = LocalDateTime.now();
        this.revocationReason = revocationReason;
        this.revokedBy = revokedBy;
    }
}
