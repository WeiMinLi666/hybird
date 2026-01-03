package org.wyman.types.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 证书吊销事件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CertificateRevokedEvent extends BaseDomainEvent {
    private String certificateSerial;
    private String reason;
    private LocalDateTime revocationDate;

    public CertificateRevokedEvent(String certificateSerial, String reason, LocalDateTime revocationDate) {
        super("CertificateRevoked");
        this.certificateSerial = certificateSerial;
        this.reason = reason;
        this.revocationDate = revocationDate;
    }
}
