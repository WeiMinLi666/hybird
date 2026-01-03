package org.wyman.domain.status.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wyman.domain.status.valobj.CRLMetadata;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 吊销状态缓存聚合根
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevocationStatusCache {
    /**
     * 缓存ID
     */
    private String cacheId;

    /**
     * 缓存更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * CRL元数据
     */
    private CRLMetadata crlMetadata;

    /**
     * 吊销证书序列号集合
     */
    private Set<String> revokedSerialNumbers;

    /**
     * 吊销详情映射(序列号 -> 吊销信息)
     */
    private Map<String, RevocationDetail> revocationDetails;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    public RevocationStatusCache(String cacheId) {
        this.cacheId = cacheId;
        this.lastUpdateTime = LocalDateTime.now();
        this.createTime = LocalDateTime.now();
        this.revokedSerialNumbers = ConcurrentHashMap.newKeySet();
        this.revocationDetails = new ConcurrentHashMap<>();
    }

    /**
     * 查询证书吊销状态
     */
    public boolean isRevoked(String serialNumber) {
        return revokedSerialNumbers != null && revokedSerialNumbers.contains(serialNumber);
    }

    /**
     * 查询吊销详情
     */
    public RevocationDetail getRevocationDetail(String serialNumber) {
        return revocationDetails != null ? revocationDetails.get(serialNumber) : null;
    }

    /**
     * 批量查询吊销状态
     */
    public Map<String, Boolean> batchCheckRevocation(List<String> serialNumbers) {
        Map<String, Boolean> result = new HashMap<>();
        for (String serialNumber : serialNumbers) {
            result.put(serialNumber, isRevoked(serialNumber));
        }
        return result;
    }

    /**
     * 更新CRL数据
     */
    public void updateFromCRL(CRLMetadata metadata, List<RevocationDetail> revokedDetails) {
        this.crlMetadata = metadata;
        this.lastUpdateTime = LocalDateTime.now();

        // 重建吊销证书集合
        this.revokedSerialNumbers = ConcurrentHashMap.newKeySet();
        this.revocationDetails = new ConcurrentHashMap<>();

        if (revokedDetails != null) {
            for (RevocationDetail detail : revokedDetails) {
                this.revokedSerialNumbers.add(detail.getSerialNumber());
                this.revocationDetails.put(detail.getSerialNumber(), detail);
            }
        }
    }

    /**
     * 添加吊销条目
     */
    public void addRevokedEntry(RevocationDetail detail) {
        if (revokedSerialNumbers == null) {
            revokedSerialNumbers = ConcurrentHashMap.newKeySet();
        }
        if (revocationDetails == null) {
            revocationDetails = new ConcurrentHashMap<>();
        }
        revokedSerialNumbers.add(detail.getSerialNumber());
        revocationDetails.put(detail.getSerialNumber(), detail);
        this.lastUpdateTime = LocalDateTime.now();
    }

    /**
     * 移除吊销条目
     */
    public void removeRevokedEntry(String serialNumber) {
        if (revokedSerialNumbers != null) {
            revokedSerialNumbers.remove(serialNumber);
        }
        if (revocationDetails != null) {
            revocationDetails.remove(serialNumber);
        }
        this.lastUpdateTime = LocalDateTime.now();
    }

    /**
     * 获取吊销证书数量
     */
    public int getRevokedCount() {
        return revokedSerialNumbers != null ? revokedSerialNumbers.size() : 0;
    }

    /**
     * 清空缓存
     */
    public void clear() {
        if (revokedSerialNumbers != null) {
            revokedSerialNumbers.clear();
        }
        if (revocationDetails != null) {
            revocationDetails.clear();
        }
        this.lastUpdateTime = LocalDateTime.now();
    }

    /**
     * 吊销详情内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevocationDetail {
        private String serialNumber;
        private LocalDateTime revocationDate;
        private String revocationReason;
    }
}
