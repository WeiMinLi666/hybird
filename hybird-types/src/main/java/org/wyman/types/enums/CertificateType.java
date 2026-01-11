package org.wyman.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 证书类型枚举
 */
@Getter
@AllArgsConstructor
public enum CertificateType {
    DEVICE_CERT("DEVICE_CERT", "设备证书"),
    PLATFORM_CERT("PLATFORM_CERT", "平台证书"),
    CA_CERT("CA_CERT", "CA根证书"),
    INTERMEDIATE_CERT("INTERMEDIATE_CERT", "中级证书");

    private final String code;
    private final String desc;

    public static CertificateType from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("证书类型不能为空");
        }
        String upper = value.trim().toUpperCase();
        // 支持 code / name / 常见别名
        for (CertificateType type : values()) {
            if (type.name().equals(upper) || type.code.equals(upper)) {
                return type;
            }
        }
        switch (upper) {
            case "DEVICE":
                return DEVICE_CERT;
            case "PLATFORM":
                return PLATFORM_CERT;
            case "CA":
            case "ROOT":
                return CA_CERT;
            case "INTERMEDIATE":
            case "SUBCA":
                return INTERMEDIATE_CERT;
            default:
                throw new IllegalArgumentException("不支持的证书类型: " + value);
        }
    }
}
