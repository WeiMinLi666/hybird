package org.wyman.domain.signing.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 证书吊销列表值对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CRL {
    /**
     * CRL序列号
     */
    private String crlNumber;

    /**
     * 颁发者DN
     */
    private String issuerDN;

    /**
     * 本次更新时间
     */
    private LocalDateTime thisUpdate;

    /**
     * 下次更新时间
     */
    private LocalDateTime nextUpdate;

    /**
     * 吊销条目
     */
    private List<RevokedCertificate> revokedCertificates;

    /**
     * CRL编码(PEM格式)
     */
    private String pemEncoded;

    /**
     * 签名算法
     */
    private String signatureAlgorithm;
}
