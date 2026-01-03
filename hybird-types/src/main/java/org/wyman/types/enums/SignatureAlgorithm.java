package org.wyman.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 签名算法枚举
 */
@Getter
@AllArgsConstructor
public enum SignatureAlgorithm {
    SM2("SM2", "国密SM2签名算法", false),
    ML_DSA("ML-DSA", "后量子ML-DSA签名算法", true),
    RSA2048("RSA2048", "RSA 2048位签名算法", false),
    RSA4096("RSA4096", "RSA 4096位签名算法", false),
    ECDSA_P256("ECDSA_P256", "ECDSA P-256曲线", false);

    private final String code;
    private final String description;
    private final boolean isPostQuantum;
}
