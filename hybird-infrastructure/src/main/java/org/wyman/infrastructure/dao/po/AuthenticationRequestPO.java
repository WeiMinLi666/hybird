package org.wyman.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 身份验证请求PO实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequestPO {
    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 申请者ID
     */
    private String applicantId;

    /**
     * 申请者名称
     */
    private String applicantName;

    /**
     * 申请者邮箱
     */
    private String applicantEmail;

    /**
     * CSR内容
     */
    private String csrContent;

    /**
     * CSR主题DN
     */
    private String csrSubjectDn;

    /**
     * 公钥算法
     */
    private String publicKeyAlgorithm;

    /**
     * 状态
     */
    private String status;

    /**
     * 验证失败原因
     */
    private String failureReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
