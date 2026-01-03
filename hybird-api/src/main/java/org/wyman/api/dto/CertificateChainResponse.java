package org.wyman.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 证书链响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateChainResponse {
    /**
     * 证书序列号
     */
    private String serialNumber;

    /**
     * 证书链列表(从端证书到根证书)
     */
    private List<CertificateInfo> chain;

    /**
     * 证书信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateInfo {
        /**
         * 序列号
         */
        private String serialNumber;

        /**
         * 主题DN
         */
        private String subjectDN;

        /**
         * 颁发者DN
         */
        private String issuerDN;

        /**
         * 证书PEM
         */
        private String certificatePem;

        /**
         * 层级(0表示端证书)
         */
        private Integer level;
    }
}
