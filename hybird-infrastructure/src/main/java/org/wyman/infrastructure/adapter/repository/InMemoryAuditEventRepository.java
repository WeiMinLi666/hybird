package org.wyman.infrastructure.adapter.repository;

import org.springframework.stereotype.Repository;
import org.wyman.domain.audit.adapter.port.IAuditEventRepository;
import org.wyman.domain.audit.model.aggregate.AuditEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审计事件仓储内存实现
 */
@Repository
public class InMemoryAuditEventRepository implements IAuditEventRepository {

    private final Map<String, AuditEvent> storage = new HashMap<>();

    @Override
    public void save(AuditEvent auditEvent) {
        storage.put(auditEvent.getEventId(), auditEvent);
    }

    @Override
    public void saveBatch(List<AuditEvent> auditEvents) {
        auditEvents.forEach(event -> storage.put(event.getEventId(), event));
    }

    @Override
    public AuditEvent findById(String eventId) {
        return storage.get(eventId);
    }

    @Override
    public List<AuditEvent> findByEventType(String eventType) {
        return storage.values().stream()
            .filter(event -> eventType.equals(event.getEventType()))
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditEvent> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return storage.values().stream()
            .filter(event -> event.getTimestamp().getTimestamp().isAfter(startTime) &&
                         event.getTimestamp().getTimestamp().isBefore(endTime))
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditEvent> findByOperator(String operator) {
        return storage.values().stream()
            .filter(event -> operator.equals(event.getOperator()))
            .collect(Collectors.toList());
    }

    @Override
    public boolean verifyEventIntegrity(String eventId) {
        AuditEvent event = storage.get(eventId);
        return event != null && event.verifyPayloadIntegrity();
    }
}
