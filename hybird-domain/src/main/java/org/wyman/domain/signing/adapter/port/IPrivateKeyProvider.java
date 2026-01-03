package org.wyman.domain.signing.adapter.port;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 私钥提供者接口
 */
public interface IPrivateKeyProvider {
    /**
     * 获取签名私钥
     */
    PrivateKey getSigningPrivateKey(String algorithm);

    /**
     * 获取对应的公钥
     */
    PublicKey getPublicKey(String algorithm);

    /**
     * 获取密钥对别名
     */
    String getKeyAlias(String algorithm);
}
