package org.wyman.domain.audit.adapter.port;

import org.wyman.domain.audit.model.aggregate.AuditEvent;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计事件仓储接口
 */
public interface IAuditEventRepository {
    /**
     * 保存审计事件
     */
    void save(AuditEvent auditEvent);

    /**
     * 批量保存审计事件
     */
    void saveBatch(List<AuditEvent> auditEvents);

    /**
     * 根据事件ID查询
     */
    AuditEvent findById(String eventId);

    /**
     * 根据事件类型查询
     */
    List<AuditEvent> findByEventType(String eventType);

    /**
     * 根据时间范围查询
     */
    List<AuditEvent> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据操作者查询
     */
    List<AuditEvent> findByOperator(String operator);

    /**
     * 验证事件完整性
     */
    boolean verifyEventIntegrity(String eventId);
}
