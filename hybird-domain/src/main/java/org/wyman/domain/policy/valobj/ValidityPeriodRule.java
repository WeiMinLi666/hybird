package org.wyman.domain.policy.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.temporal.ChronoUnit;

/**
 * 有效期规则值对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidityPeriodRule {
    /**
     * 最短有效期(天)
     */
    private int minDays;

    /**
     * 最长有效期(天)
     */
    private int maxDays;

    /**
     * 验证有效期
     */
    public boolean validateValidityPeriod(int days) {
        return days >= minDays && days <= maxDays;
    }

    /**
     * 验证有效期
     */
    public boolean validateValidityPeriod(java.time.LocalDateTime notBefore,
                                           java.time.LocalDateTime notAfter) {
        long days = ChronoUnit.DAYS.between(notBefore, notAfter);
        return validateValidityPeriod((int) days);
    }
}
