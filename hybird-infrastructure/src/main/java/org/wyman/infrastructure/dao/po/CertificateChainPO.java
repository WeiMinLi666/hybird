package org.wyman.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 证书链PO实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateChainPO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 证书序列号
     */
    private String certificateSerialNumber;

    /**
     * 父证书序列号
     */
    private String parentSerialNumber;

    /**
     * 链层级
     */
    private Integer level;

    /**
     * 排序索引
     */
    private Integer orderIndex;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
