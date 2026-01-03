package org.wyman.types.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 基础领域事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseDomainEvent implements IDomainEvent {
    private String eventId;
    private LocalDateTime occurredOn;
    private String eventType;

    protected BaseDomainEvent(String eventType) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.eventType = eventType;
    }
}
