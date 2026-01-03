package org.wyman.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 证书颁发机构PO实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateAuthorityPO {
    /**
     * CA ID
     */
    private String caId;

    /**
     * CA名称
     */
    private String caName;

    /**
     * CA DN
     */
    private String caDn;

    /**
     * CA证书PEM
     */
    private String certificatePem;

    /**
     * 私钥别名
     */
    private String privateKeyAlias;

    /**
     * 密钥类型
     */
    private String keyType;

    /**
     * 密钥大小
     */
    private Integer keySize;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
