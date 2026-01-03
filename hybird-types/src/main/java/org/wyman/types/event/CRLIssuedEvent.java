package org.wyman.types.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * CRL签发事件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CRLIssuedEvent extends BaseDomainEvent {
    private String crlNumber;
    private String crlUrl;
    private int revokedCount;

    public CRLIssuedEvent(String crlNumber, String crlUrl, int revokedCount) {
        super("CRLIssued");
        this.crlNumber = crlNumber;
        this.crlUrl = crlUrl;
        this.revokedCount = revokedCount;
    }
}
