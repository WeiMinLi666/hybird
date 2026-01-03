package org.wyman.types.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 身份验证完成事件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AuthenticationCompletedEvent extends BaseDomainEvent {
    private String requestId;
    private String applicantId;
    private boolean success;
    private String reason;

    public AuthenticationCompletedEvent(String requestId, String applicantId, boolean success, String reason) {
        super("AuthenticationCompleted");
        this.requestId = requestId;
        this.applicantId = applicantId;
        this.success = success;
        this.reason = reason;
    }
}
