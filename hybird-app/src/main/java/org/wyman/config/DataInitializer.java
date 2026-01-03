package org.wyman.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.wyman.domain.policy.service.PolicyService;
import org.wyman.domain.signing.adapter.port.ICertificateAuthorityRepository;
import org.wyman.domain.signing.model.aggregate.CertificateAuthority;
import org.wyman.domain.signing.valobj.Certificate;
import org.wyman.types.enums.CertificateType;

import java.time.LocalDateTime;

/**
 * 数据初始化器
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final PolicyService policyService;
    private final ICertificateAuthorityRepository caRepository;

    public DataInitializer(PolicyService policyService,
                          ICertificateAuthorityRepository caRepository) {
        this.policyService = policyService;
        this.caRepository = caRepository;
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
            Certificate caCert = new Certificate();
            caCert.setSerialNumber("1");
            caCert.setSubjectDN("CN=Root CA,O=Hybrid Certificate System");
            caCert.setIssuerDN("CN=Root CA,O=Hybrid Certificate System");
            caCert.setNotBefore(LocalDateTime.now());
            caCert.setNotAfter(LocalDateTime.now().plusYears(10));
            caCert.setSignatureAlgorithm("SM2");
            caCert.setPemEncoded("MOCK_CA_CERTIFICATE");

            CertificateAuthority ca = new CertificateAuthority(
                "ca-001",
                "RootCA",
                caCert
            );

            caRepository.save(ca);
            System.out.println("默认CA创建成功: " + ca.getCaName());
        } catch (Exception e) {
            System.out.println("默认CA可能已存在: " + e.getMessage());
        }
    }
}
