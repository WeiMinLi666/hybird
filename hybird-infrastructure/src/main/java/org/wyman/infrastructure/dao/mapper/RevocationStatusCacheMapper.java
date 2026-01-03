package org.wyman.infrastructure.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wyman.infrastructure.dao.po.RevocationStatusCachePO;

/**
 * 吊销状态缓存Mapper接口
 */
@Mapper
public interface RevocationStatusCacheMapper {

    /**
     * 插入缓存
     */
    int insert(RevocationStatusCachePO cache);

    /**
     * 更新缓存
     */
    int update(RevocationStatusCachePO cache);

    /**
     * 根据缓存ID查询
     */
    RevocationStatusCachePO selectById(@Param("cacheId") String cacheId);

    /**
     * 查询最新的缓存
     */
    RevocationStatusCachePO selectLatest();

    /**
     * 删除缓存
     */
    int deleteById(@Param("cacheId") String cacheId);

    /**
     * 清理所有旧缓存标记
     */
    int clearAllLatestFlag();
}
