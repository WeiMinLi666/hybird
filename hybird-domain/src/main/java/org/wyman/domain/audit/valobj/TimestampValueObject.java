package org.wyman.domain.audit.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 时间戳值对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimestampValueObject {
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 时间戳格式化字符串
     */
    private String formatted;

    public TimestampValueObject(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        this.formatted = timestamp.toString();
    }

    /**
     * 获取当前时间戳
     */
    public static TimestampValueObject now() {
        return new TimestampValueObject(LocalDateTime.now());
    }
}
