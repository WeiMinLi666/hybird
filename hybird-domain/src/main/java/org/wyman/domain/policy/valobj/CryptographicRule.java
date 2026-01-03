package org.wyman.domain.policy.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 密码学规则值对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptographicRule {
    /**
     * 允许的签名算法
     */
    private List<String> allowedSignatureAlgorithms;

    /**
     * 允许的密钥封装算法
     */
    private List<String> allowedKeyEncapsulationAlgorithms;

    /**
     * 最小密钥长度(位)
     */
    private int minKeyLength;

    /**
     * 是否要求混合签名(经典+后量子)
     */
    private boolean requireHybridSignature;

    /**
     * 验证算法组合
     */
    public boolean validateAlgorithmCombination(String signatureAlg, String kemAlg) {
        boolean sigValid = allowedSignatureAlgorithms == null ||
                          allowedSignatureAlgorithms.isEmpty() ||
                          allowedSignatureAlgorithms.contains(signatureAlg);

        boolean kemValid = allowedKeyEncapsulationAlgorithms == null ||
                          allowedKeyEncapsulationAlgorithms.isEmpty() ||
                          kemAlg == null ||
                          allowedKeyEncapsulationAlgorithms.contains(kemAlg);

        return sigValid && kemValid;
    }
}
