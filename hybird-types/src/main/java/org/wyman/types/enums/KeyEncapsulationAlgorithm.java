package org.wyman.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 密钥封装算法枚举
 */
@Getter
@AllArgsConstructor
public enum KeyEncapsulationAlgorithm {
    ML_KEM("ML-KEM", "后量子ML-KEM密钥封装算法", true),
    RSA_KEM("RSA-KEM", "RSA密钥封装算法", false);

    private final String code;
    private final String description;
    private final boolean isPostQuantum;
}
