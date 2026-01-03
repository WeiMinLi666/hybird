package org.wyman.domain.status.service;

import org.springframework.stereotype.Service;
import org.wyman.domain.status.adapter.port.IRevocationStatusCacheRepository;
import org.wyman.domain.status.model.aggregate.RevocationStatusCache;
import org.wyman.domain.status.valobj.CRLMetadata;

import java.util.List;
import java.util.Map;

/**
 * 吊销状态查询领域服务
 */
@Service
public class RevocationStatusService {

    private final IRevocationStatusCacheRepository cacheRepository;

    public RevocationStatusService(IRevocationStatusCacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    /**
     * 查询证书吊销状态
     */
    public boolean checkRevocationStatus(String serialNumber) {
        RevocationStatusCache cache = cacheRepository.findLatest();
        if (cache == null) {
            return false;
        }
        return cache.isRevoked(serialNumber);
    }

    /**
     * 批量查询证书吊销状态
     */
    public Map<String, Boolean> batchCheckRevocationStatus(List<String> serialNumbers) {
        RevocationStatusCache cache = cacheRepository.findLatest();
        if (cache == null) {
            // 如果缓存不存在,返回全部未吊销
            return serialNumbers.stream()
                .collect(java.util.stream.Collectors.toMap(
                    sn -> sn,
                    sn -> false
                ));
        }
        return cache.batchCheckRevocation(serialNumbers);
    }

    /**
     * 查询吊销详情
     */
    public RevocationStatusCache.RevocationDetail getRevocationDetail(String serialNumber) {
        RevocationStatusCache cache = cacheRepository.findLatest();
        if (cache == null) {
            return null;
        }
        return cache.getRevocationDetail(serialNumber);
    }

    /**
     * 更新吊销状态缓存
     */
    public void updateCacheFromCRL(CRLMetadata crlMetadata,
                                    List<RevocationStatusCache.RevocationDetail> revokedDetails) {
        RevocationStatusCache cache = cacheRepository.findLatest();
        if (cache == null) {
            cache = new RevocationStatusCache("default-cache");
        }
        cache.updateFromCRL(crlMetadata, revokedDetails);
        cacheRepository.save(cache);
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStatistics getCacheStatistics() {
        RevocationStatusCache cache = cacheRepository.findLatest();
        if (cache == null) {
            return new CacheStatistics(0, null, null);
        }
        return new CacheStatistics(
            cache.getRevokedCount(),
            cache.getLastUpdateTime(),
            cache.getCrlMetadata()
        );
    }

    /**
     * 缓存统计信息
     */
    public record CacheStatistics(
        int revokedCount,
        java.time.LocalDateTime lastUpdateTime,
        CRLMetadata crlMetadata
    ) {}
}
