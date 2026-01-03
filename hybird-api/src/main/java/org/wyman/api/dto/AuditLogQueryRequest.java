package org.wyman.api.dto;

import lombok.Data;

/**
 * 审计日志查询请求
 */
@Data
public class AuditLogQueryRequest {
    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 页码
     */
    private Integer pageNumber = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;
}
