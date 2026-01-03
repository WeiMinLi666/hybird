package org.wyman.types.event;

import java.time.LocalDateTime;

/**
 * 领域事件接口
 */
public interface IDomainEvent {
    /**
     * 事件ID
     */
    String getEventId();

    /**
     * 事件发生时间
     */
    LocalDateTime getOccurredOn();

    /**
     * 事件类型
     */
    String getEventType();
}
