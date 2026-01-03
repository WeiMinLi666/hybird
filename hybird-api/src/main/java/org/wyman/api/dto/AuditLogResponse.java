package org.wyman.api.dto;

import lombok.Data;

/**
 * 审计日志响应
 */
@Data
public class AuditLogResponse {
    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * 结果
     */
    private String result;

    /**
     * 详情
     */
    private String details;

    /**
     * 时间戳
     */
    private String timestamp;
}
