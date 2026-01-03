package org.wyman.trigger.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.wyman.api.dto.AuditLogQueryRequest;
import org.wyman.api.dto.AuditLogResponse;
import org.wyman.api.response.Response;
import org.wyman.domain.audit.service.AuditService;
import org.wyman.domain.audit.model.aggregate.AuditEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审计日志控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/audit")
@CrossOrigin("*")
public class AuditController {

    private final AuditService auditService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * 查询审计日志
     */
    @PostMapping("/query")
    public Response<List<AuditLogResponse>> queryAuditLogs(@RequestBody AuditLogQueryRequest request) {
        try {
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;

            if (request.getStartTime() != null && !request.getStartTime().isEmpty()) {
                startTime = LocalDateTime.parse(request.getStartTime(), DATE_FORMATTER);
            }
            if (request.getEndTime() != null && !request.getEndTime().isEmpty()) {
                endTime = LocalDateTime.parse(request.getEndTime(), DATE_FORMATTER);
            }

            List<AuditEvent> events = auditService.queryAuditEvents(
                request.getOperator(),
                request.getOperationType(),
                startTime,
                endTime,
                request.getPageSize(),
                request.getPageNumber()
            );

            List<AuditLogResponse> responses = events.stream()
                .map(event -> {
                    AuditLogResponse response = new AuditLogResponse();
                    response.setEventId(event.getEventId());
                    response.setOperator(event.getOperator());
                    response.setOperationType(event.getOperationType());
                    response.setResourceType(event.getTargetResource() != null ? extractResourceType(event.getTargetResource()) : "");
                    response.setResourceId(event.getTargetResource() != null ? extractResourceId(event.getTargetResource()) : "");
                    response.setResult(event.getResultStatus());
                    response.setDetails(event.getPayload());
                    response.setTimestamp(event.getTimestamp().getFormatted());
                    return response;
                })
                .collect(Collectors.toList());

            return Response.success(responses);
        } catch (Exception e) {
            log.error("查询审计日志失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询操作日志(按资源)
     */
    @GetMapping("/resource/{resourceType}/{resourceId}")
    public Response<List<AuditLogResponse>> getAuditLogsByResource(
            @PathVariable String resourceType,
            @PathVariable String resourceId) {
        try {
            List<AuditEvent> events = auditService.queryAuditEventsByResource(resourceType, resourceId);

            List<AuditLogResponse> responses = events.stream()
                .map(event -> {
                    AuditLogResponse response = new AuditLogResponse();
                    response.setEventId(event.getEventId());
                    response.setOperator(event.getOperator());
                    response.setOperationType(event.getOperationType());
                    response.setResourceType(event.getTargetResource() != null ? extractResourceType(event.getTargetResource()) : "");
                    response.setResourceId(event.getTargetResource() != null ? extractResourceId(event.getTargetResource()) : "");
                    response.setResult(event.getResultStatus());
                    response.setDetails(event.getPayload());
                    response.setTimestamp(event.getTimestamp().getFormatted());
                    return response;
                })
                .collect(Collectors.toList());

            return Response.success(responses);
        } catch (Exception e) {
            log.error("查询资源审计日志失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 统计操作日志
     */
    @GetMapping("/statistics")
    public Response<Object> getAuditStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            LocalDateTime start = null;
            LocalDateTime end = null;

            if (startTime != null && !startTime.isEmpty()) {
                start = LocalDateTime.parse(startTime, DATE_FORMATTER);
            }
            if (endTime != null && !endTime.isEmpty()) {
                end = LocalDateTime.parse(endTime, DATE_FORMATTER);
            }

            var statistics = auditService.getAuditStatistics(start, end);
            return Response.success(statistics);
        } catch (Exception e) {
            log.error("获取审计统计失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 从目标资源字符串中提取资源类型
     */
    private String extractResourceType(String targetResource) {
        if (targetResource == null || !targetResource.contains(":")) {
            return targetResource != null ? targetResource : "";
        }
        return targetResource.split(":")[0];
    }

    /**
     * 从目标资源字符串中提取资源ID
     */
    private String extractResourceId(String targetResource) {
        if (targetResource == null || !targetResource.contains(":")) {
            return "";
        }
        String[] parts = targetResource.split(":");
        return parts.length > 1 ? parts[1] : "";
    }
}
