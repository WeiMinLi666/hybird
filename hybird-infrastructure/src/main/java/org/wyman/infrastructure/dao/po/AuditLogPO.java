package org.wyman.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审计日志PO实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogPO {
    /**
     * 日志ID
     */
    private Long logId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事件数据(JSON)
     */
    private String eventData;

    /**
     * 操作人
     */
    private String operator;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
