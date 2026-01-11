package org.wyman.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.wyman.domain.policy.service.PolicyService;
import org.wyman.domain.signing.adapter.port.ICertificateAuthorityRepository;
import org.wyman.domain.signing.adapter.port.ICertificateGenerator;
import org.wyman.domain.signing.model.aggregate.CertificateAuthority;
import org.wyman.domain.signing.valobj.Certificate;
import org.wyman.types.enums.CertificateType;

import java.security.KeyPair;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 数据初始化器
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final PolicyService policyService;
    private final ICertificateAuthorityRepository caRepository;
    private final ICertificateGenerator certificateGenerator;

    public DataInitializer(PolicyService policyService,
                          ICertificateAuthorityRepository caRepository,
                          ICertificateGenerator certificateGenerator) {
        this.policyService = policyService;
        this.caRepository = caRepository;
        this.certificateGenerator = certificateGenerator;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("========================================");
        System.out.println("初始化默认数据...");

        // 初始化默认证书策略
        initializeDefaultPolicies();

        // 初始化默认CA
        initializeDefaultCA();

        System.out.println("默认数据初始化完成");
        System.out.println("========================================");
    }

    private void initializeDefaultPolicies() {
        System.out.println("初始化默认证书策略...");

        // 设备证书策略
        try {
            policyService.createDefaultPolicy(
                CertificateType.DEVICE_CERT,
                "设备证书策略"
            );
            System.out.println("设备证书策略创建成功");
        } catch (Exception e) {
            System.out.println("设备证书策略可能已存在: " + e.getMessage());
        }

        // 平台证书策略
        try {
            policyService.createDefaultPolicy(
                CertificateType.PLATFORM_CERT,
                "平台证书策略"
            );
            System.out.println("平台证书策略创建成功");
        } catch (Exception e) {
            System.out.println("平台证书策略可能已存在: " + e.getMessage());
        }
    }

    private void initializeDefaultCA() {
        System.out.println("初始化默认CA...");

        try {
            // 检查是否已存在CA
            var existingCA = caRepository.findByName("RootCA");
            if (existingCA != null && existingCA.getCaCertificate() != null
                && existingCA.getCaCertificate().getPemEncoded() != null
                && !existingCA.getCaCertificate().getPemEncoded().isEmpty()
                && !existingCA.getCaCertificate().getPemEncoded().equals("MOCK_CA_CERTIFICATE")) {
                System.out.println("CA已存在且有效,跳过创建: " + existingCA.getCaName());
                return;
            }

            System.out.println("生成真实的CA证书...");

            // 生成CA密钥对
            KeyPair caKeyPair = certificateGenerator.generateKeyPair("SM2");

            // 计算有效期
            LocalDateTime notBefore = LocalDateTime.now();
            LocalDateTime notAfter = notBefore.plusYears(10);
            Date notBeforeDate = Date.from(notBefore.atZone(java.time.ZoneId.systemDefault()).toInstant());
            Date notAfterDate = Date.from(notAfter.atZone(java.time.ZoneId.systemDefault()).toInstant());

            // 解析DN
            org.bouncycastle.asn1.x500.X500Name x500Name =
                new org.bouncycastle.asn1.x500.X500Name("CN=Root CA,O=Hybrid Certificate System,C=CN");

            // 生成CA自签名证书
            java.security.cert.X509Certificate x509CACert = certificateGenerator.generateCACertificate(
                x500Name,
                caKeyPair,
                notBeforeDate,
                notAfterDate,
                java.math.BigInteger.valueOf(1),
                "SM2",
                "http://crl.example.com/ca.crl"
            );

            // 转换为领域模型
            Certificate caCert = new Certificate();
            caCert.setSerialNumber(x509CACert.getSerialNumber().toString(16));
            caCert.setSubjectDN(x509CACert.getSubjectX500Principal().getName());
            caCert.setIssuerDN(x509CACert.getIssuerX500Principal().getName());
            caCert.setSignatureAlgorithm("SM2");
            caCert.setPemEncoded(certificateGenerator.toPEM(x509CACert));

            // 与测试脚本保持一致的 CA ID
            CertificateAuthority ca = new CertificateAuthority(
                "CA001",
                "RootCA",
                caCert
            );

            caRepository.save(ca);
            System.out.println("默认CA创建成功: " + ca.getCaName() + ", DN: " + caCert.getSubjectDN());
        } catch (Exception e) {
            System.out.println("默认CA可能已存在或创建失败: " + e.getMessage());
        }
    }
}
