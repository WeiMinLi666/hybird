package org.wyman.infrastructure.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.wyman.infrastructure.dao.po.AuditLogPO;

import java.util.List;

/**
 * 审计日志Mapper接口
 */
@Mapper
public interface AuditLogMapper {

    /**
     * 插入审计日志
     */
    int insert(AuditLogPO auditLog);

    /**
     * 根据事件类型查询
     */
    List<AuditLogPO> selectByEventType(@org.apache.ibatis.annotations.Param("eventType") String eventType);

    /**
     * 查询所有日志
     */
    List<AuditLogPO> selectAll();
}
