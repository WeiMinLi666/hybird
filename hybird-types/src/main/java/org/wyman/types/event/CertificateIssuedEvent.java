package org.wyman.types.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 证书签发事件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CertificateIssuedEvent extends BaseDomainEvent {
    private String certificateSerial;
    private String subject;
    private String requestId;

    public CertificateIssuedEvent(String certificateSerial, String subject, String requestId) {
        super("CertificateIssued");
        this.certificateSerial = certificateSerial;
        this.subject = subject;
        this.requestId = requestId;
    }
}
