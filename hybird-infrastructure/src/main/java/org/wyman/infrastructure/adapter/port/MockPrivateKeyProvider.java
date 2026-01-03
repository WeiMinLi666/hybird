package org.wyman.infrastructure.adapter.port;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;
import org.wyman.domain.signing.adapter.port.IPrivateKeyProvider;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;

/**
 * 模拟私钥提供者实现
 */
@Component
public class MockPrivateKeyProvider implements IPrivateKeyProvider {

    private final KeyPair sm2KeyPair;
    private final KeyPair rsaKeyPair;
    private final KeyPair ecdsaKeyPair;

    static {
        // 注册 Bouncy Castle 提供者
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public MockPrivateKeyProvider() {
        try {
            // 生成SM2密钥对（使用BouncyCastle的sm2p256v1曲线）
            KeyPairGenerator sm2Kpg = KeyPairGenerator.getInstance("EC", "BC");
            sm2Kpg.initialize(new ECGenParameterSpec("sm2p256v1"));
            this.sm2KeyPair = sm2Kpg.generateKeyPair();

            // 生成RSA密钥对
            KeyPairGenerator rsaKpg = KeyPairGenerator.getInstance("RSA");
            rsaKpg.initialize(2048);
            this.rsaKeyPair = rsaKpg.generateKeyPair();

            // 生成ECDSA密钥对（用于ECDSA_P256）
            KeyPairGenerator ecdsaKpg = KeyPairGenerator.getInstance("EC");
            ecdsaKpg.initialize(new ECGenParameterSpec("secp256r1"));
            this.ecdsaKeyPair = ecdsaKpg.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("初始化密钥对失败", e);
        }
    }

    @Override
    public PrivateKey getSigningPrivateKey(String algorithm) {
        if (algorithm.equals("SM2")) {
            return sm2KeyPair.getPrivate();
        } else if (algorithm.equals("RSA2048") || algorithm.equals("RSA4096")) {
            return rsaKeyPair.getPrivate();
        } else if (algorithm.equals("ECDSA_P256")) {
            return ecdsaKeyPair.getPrivate();
        }
        throw new IllegalArgumentException("不支持的算法: " + algorithm);
    }


    @Override
    public PublicKey getPublicKey(String algorithm) {
        if (algorithm.equals("SM2")) {
            return sm2KeyPair.getPublic();
        } else if (algorithm.equals("RSA2048") || algorithm.equals("RSA4096")) {
            return rsaKeyPair.getPublic();
        } else if (algorithm.equals("ECDSA_P256")) {
            return ecdsaKeyPair.getPublic();
        }
        throw new IllegalArgumentException("不支持的算法: " + algorithm);
    }

    @Override
    public String getKeyAlias(String algorithm) {
        return "key-" + algorithm.toLowerCase();
    }
}
