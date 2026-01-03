package org.wyman.domain.status.adapter.port;

import org.wyman.domain.status.model.aggregate.RevocationStatusCache;

/**
 * 吊销状态缓存仓储接口
 */
public interface IRevocationStatusCacheRepository {
    /**
     * 保存缓存
     */
    void save(RevocationStatusCache cache);

    /**
     * 根据缓存ID查询
     */
    RevocationStatusCache findById(String cacheId);

    /**
     * 获取最新的缓存
     */
    RevocationStatusCache findLatest();

    /**
     * 删除缓存
     */
    void delete(String cacheId);
}
