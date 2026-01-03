package org.wyman.domain.status.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CRL元数据值对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CRLMetadata {
    /**
     * CRL编号
     */
    private String crlNumber;

    /**
     * 颁发者DN
     */
    private String issuerDN;

    /**
     * 本次更新时间
     */
    private LocalDateTime thisUpdate;

    /**
     * 下次更新时间
     */
    private LocalDateTime nextUpdate;

    /**
     * CRL下载URL
     */
    private String crlUrl;

    /**
     * 吊销证书数量
     */
    private int revokedCount;

    /**
     * 检查CRL是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(nextUpdate);
    }

    /**
     * 检查CRL是否需要更新
     */
    public boolean needsUpdate() {
        return LocalDateTime.now().plusHours(1).isAfter(nextUpdate);
    }
}
