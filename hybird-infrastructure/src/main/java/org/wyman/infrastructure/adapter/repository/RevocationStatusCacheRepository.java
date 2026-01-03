package org.wyman.infrastructure.adapter.repository;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Repository;
import org.wyman.domain.status.adapter.port.IRevocationStatusCacheRepository;
import org.wyman.domain.status.model.aggregate.RevocationStatusCache;
import org.wyman.infrastructure.dao.mapper.RevocationStatusCacheMapper;
import org.wyman.infrastructure.dao.po.RevocationStatusCachePO;

import java.time.LocalDateTime;

/**
 * 吊销状态缓存仓储MyBatis实现
 */
@Repository
public class RevocationStatusCacheRepository implements IRevocationStatusCacheRepository {

    private final RevocationStatusCacheMapper revocationStatusCacheMapper;

    public RevocationStatusCacheRepository(RevocationStatusCacheMapper revocationStatusCacheMapper) {
        this.revocationStatusCacheMapper = revocationStatusCacheMapper;
    }

    @Override
    public void save(RevocationStatusCache cache) {
        RevocationStatusCachePO po = toPO(cache);
        RevocationStatusCachePO existing = revocationStatusCacheMapper.selectById(cache.getCacheId());

        // 如果是新缓存，先清除所有旧缓存标记
        if (existing == null) {
            revocationStatusCacheMapper.clearAllLatestFlag();
            po.setIsLatest(true);
        }

        if (existing == null) {
            revocationStatusCacheMapper.insert(po);
        } else {
            revocationStatusCacheMapper.update(po);
        }
    }

    @Override
    public RevocationStatusCache findById(String cacheId) {
        RevocationStatusCachePO po = revocationStatusCacheMapper.selectById(cacheId);
        return toDomain(po);
    }

    @Override
    public RevocationStatusCache findLatest() {
        RevocationStatusCachePO po = revocationStatusCacheMapper.selectLatest();
        return toDomain(po);
    }

    @Override
    public void delete(String cacheId) {
        revocationStatusCacheMapper.deleteById(cacheId);
    }

    private RevocationStatusCachePO toPO(RevocationStatusCache cache) {
        // 将整个缓存对象序列化为JSON
        String cacheData = JSON.toJSONString(cache);

        return RevocationStatusCachePO.builder()
                .cacheId(cache.getCacheId())
                .cacheData(cacheData)
                .version(1)
                .isLatest(true)
                .createTime(cache.getCreateTime() != null ? cache.getCreateTime() : LocalDateTime.now())
                .updateTime(cache.getLastUpdateTime() != null ? cache.getLastUpdateTime() : LocalDateTime.now())
                .build();
    }

    private RevocationStatusCache toDomain(RevocationStatusCachePO po) {
        if (po == null || po.getCacheData() == null) {
            return null;
        }

        // 从JSON反序列化为缓存对象
        return JSON.parseObject(po.getCacheData(), RevocationStatusCache.class);
    }
}
