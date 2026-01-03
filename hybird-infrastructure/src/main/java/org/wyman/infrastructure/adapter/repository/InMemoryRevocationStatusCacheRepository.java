package org.wyman.infrastructure.adapter.repository;

import org.springframework.stereotype.Repository;
import org.wyman.domain.status.adapter.port.IRevocationStatusCacheRepository;
import org.wyman.domain.status.model.aggregate.RevocationStatusCache;

import java.util.HashMap;
import java.util.Map;

/**
 * 吊销状态缓存仓储内存实现
 */
@Repository
public class InMemoryRevocationStatusCacheRepository implements IRevocationStatusCacheRepository {

    private final Map<String, RevocationStatusCache> storage = new HashMap<>();

    @Override
    public void save(RevocationStatusCache cache) {
        storage.put(cache.getCacheId(), cache);
    }

    @Override
    public RevocationStatusCache findById(String cacheId) {
        return storage.get(cacheId);
    }

    @Override
    public RevocationStatusCache findLatest() {
        return storage.values().stream()
            .findFirst()
            .orElse(null);
    }

    @Override
    public void delete(String cacheId) {
        storage.remove(cacheId);
    }
}
