package org.wyman.infrastructure.adapter.repository;

import org.springframework.stereotype.Repository;
import org.wyman.domain.audit.adapter.port.IAuditEventRepository;
import org.wyman.domain.audit.model.aggregate.AuditEvent;
import org.wyman.infrastructure.dao.mapper.AuditLogMapper;
import org.wyman.infrastructure.dao.po.AuditLogPO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审计事件仓储MyBatis实现
 */
@Repository
public class AuditEventRepository implements IAuditEventRepository {

    private final AuditLogMapper auditLogMapper;

    public AuditEventRepository(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    public void save(AuditEvent auditEvent) {
        auditEvent.computePayloadHash();
        AuditLogPO po = AuditLogPO.builder()
                .eventType(auditEvent.getEventType())
                .eventData(auditEvent.getPayload())
                .operator(auditEvent.getOperator())
                .ipAddress(auditEvent.getClientIp())
                .createTime(auditEvent.getCreateTime() != null ?
                    auditEvent.getCreateTime().getTimestamp() : LocalDateTime.now())
                .build();
        auditLogMapper.insert(po);
    }

    @Override
    public void saveBatch(List<AuditEvent> auditEvents) {
        for (AuditEvent auditEvent : auditEvents) {
            auditEvent.computePayloadHash();
            AuditLogPO po = AuditLogPO.builder()
                    .eventType(auditEvent.getEventType())
                    .eventData(auditEvent.getPayload())
                    .operator(auditEvent.getOperator())
                    .ipAddress(auditEvent.getClientIp())
                    .createTime(auditEvent.getCreateTime() != null ?
                        auditEvent.getCreateTime().getTimestamp() : LocalDateTime.now())
                    .build();
            auditLogMapper.insert(po);
        }
    }

    @Override
    public AuditEvent findById(String eventId) {
        // 简化实现:实际应该在Mapper中添加按ID查询
        List<AuditLogPO> poList = auditLogMapper.selectAll();
        AuditLogPO po = poList.stream()
                .filter(p -> eventId.equals(p.getLogId().toString()))
                .findFirst()
                .orElse(null);

        return toDomain(po);
    }

    @Override
    public List<AuditEvent> findByEventType(String eventType) {
        List<AuditLogPO> poList = auditLogMapper.selectByEventType(eventType);
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<AuditEvent> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        // 简化实现:实际应该在Mapper中添加时间范围查询
        List<AuditLogPO> poList = auditLogMapper.selectAll();
        return poList.stream()
                .filter(po -> {
                    if (startTime != null && po.getCreateTime().isBefore(startTime)) return false;
                    if (endTime != null && po.getCreateTime().isAfter(endTime)) return false;
                    return true;
                })
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditEvent> findByOperator(String operator) {
        // 简化实现:实际应该在Mapper中添加操作者查询
        List<AuditLogPO> poList = auditLogMapper.selectAll();
        return poList.stream()
                .filter(po -> operator.equals(po.getOperator()))
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean verifyEventIntegrity(String eventId) {
        AuditEvent event = findById(eventId);
        return event != null && event.verifyPayloadIntegrity();
    }

    private AuditEvent toDomain(AuditLogPO po) {
        if (po == null) {
            return null;
        }

        AuditEvent event = new AuditEvent(po.getEventType(), "SAVE", po.getOperator());
        event.setPayload(po.getEventData());
        event.setClientIp(po.getIpAddress());
        event.setCreateTime(new org.wyman.domain.audit.valobj.TimestampValueObject(po.getCreateTime()));
        return event;
    }
}
