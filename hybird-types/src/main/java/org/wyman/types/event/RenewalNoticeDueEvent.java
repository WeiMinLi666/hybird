package org.wyman.types.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 证书临期通知事件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class RenewalNoticeDueEvent extends BaseDomainEvent {
    private String certificateSerial;
    private String subject;
    private int daysUntilExpiry;

    public RenewalNoticeDueEvent(String certificateSerial, String subject, int daysUntilExpiry) {
        super("RenewalNoticeDue");
        this.certificateSerial = certificateSerial;
        this.subject = subject;
        this.daysUntilExpiry = daysUntilExpiry;
    }
}
