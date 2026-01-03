package org.wyman.domain.lifecycle.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知策略值对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPolicy {
    /**
     * 临期通知天数阈值
     */
    private int expiryNotificationDays;

    /**
     * 是否启用邮件通知
     */
    private boolean emailEnabled;

    /**
     * 是否启用短信通知
     */
    private boolean smsEnabled;

    /**
     * 通知邮箱地址
     */
    private String notificationEmail;

    /**
     * 通知手机号
     */
    private String notificationPhone;

    public NotificationPolicy(int expiryNotificationDays) {
        this.expiryNotificationDays = expiryNotificationDays;
        this.emailEnabled = true;
        this.smsEnabled = false;
    }

    /**
     * 检查是否需要发送临期通知
     */
    public boolean shouldNotifyExpiry(int daysUntilExpiry) {
        return daysUntilExpiry > 0 && daysUntilExpiry <= expiryNotificationDays;
    }
}
