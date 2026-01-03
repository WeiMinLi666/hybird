package org.wyman.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 证书PO实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificatePO {
    /**
     * 证书序列号
     */
    private String serialNumber;

    /**
     * 证书类型
     */
    private String certificateType;

    /**
     * 主题DN
     */
    private String subjectDn;

    /**
     * 颁发者DN
     */
    private String issuerDn;

    /**
     * 状态
     */
    private String status;

    /**
     * 有效期开始
     */
    private LocalDateTime notBefore;

    /**
     * 有效期结束
     */
    private LocalDateTime notAfter;

    /**
     * 申请者ID
     */
    private String applicantId;

    /**
     * 签发请求ID
     */
    private String issuanceRequestId;

    /**
     * 证书PEM编码
     */
    private String pemEncoded;

    /**
     * 吊销原因
     */
    private String revocationReason;

    /**
     * 吊销操作人
     */
    private String revokedBy;

    /**
     * 吊销备注
     */
    private String revocationComments;

    /**
     * 续期通知天数
     */
    private Integer renewalNoticeDays;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
