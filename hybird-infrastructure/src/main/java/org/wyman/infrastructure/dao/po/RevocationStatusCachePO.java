package org.wyman.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 吊销状态缓存PO实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevocationStatusCachePO {
    /**
     * 缓存ID
     */
    private String cacheId;

    /**
     * 缓存数据(JSON)
     */
    private String cacheData;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 是否最新
     */
    private Boolean isLatest;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
