package org.wyman.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.wyman.domain.signing.model.aggregate.CertificateAuthority;
import org.wyman.domain.signing.service.SigningService;
import org.wyman.infrastructure.adapter.repository.CertificateAuthorityRepository;

import java.util.List;

/**
 * CA初始化器
 * 在应用启动时检查并初始化根CA
 */
@Slf4j
//@Component  // 暂时禁用自动初始化,通过API手动创建CA
public class CAInitializer implements CommandLineRunner {

    private final SigningService signingService;
    private final CertificateAuthorityRepository caRepository;

    public CAInitializer(SigningService signingService,
                         CertificateAuthorityRepository caRepository) {
        this.signingService = signingService;
        this.caRepository = caRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化CA...");

        // 检查是否已存在CA
        List<CertificateAuthority> existingCAs = caRepository.findAll();

        if (existingCAs.isEmpty()) {
            log.info("未发现CA,开始创建根CA...");

            // 创建根CA (使用SM2算法)
            CertificateAuthority rootCA = signingService.createCertificateAuthority(
                "RootCA",
                "CN=Hybird Root CA,O=Hybird,C=CN",
                "SM2",
                3650  // 10年有效期
            );

            log.info("根CA创建成功: CA ID={}, CA Name={}, DN={}",
                rootCA.getCaId(),
                rootCA.getCaName(),
                rootCA.getCaCertificate().getSubjectDN());

            // 保存CA到数据库
            caRepository.save(rootCA);

            log.info("CA初始化完成!");
        } else {
            log.info("发现 {} 个已存在的CA,检查是否需要更新...", existingCAs.size());

            for (CertificateAuthority ca : existingCAs) {
                if (ca.getCaCertificate() == null || ca.getCaCertificate().getPemEncoded() == null
                    || ca.getCaCertificate().getPemEncoded().isEmpty()
                    || ca.getCaCertificate().getPemEncoded().equals("MOCK_CA_CERTIFICATE")) {
                    log.warn("CA {} 的证书为空或无效,重新创建证书...", ca.getCaName());

                    // 重新创建CA证书
                    CertificateAuthority newCA = signingService.createCertificateAuthority(
                        ca.getCaName(),
                        "CN=Hybird Root CA,O=Hybird,C=CN",
                        "SM2",
                        3650
                    );

                    // 直接更新现有CA对象,保持caId和caName
                    ca.setCaCertificate(newCA.getCaCertificate());

                    log.info("CA证书重新创建成功: CA ID={}, DN={}",
                        ca.getCaId(),
                        ca.getCaCertificate().getSubjectDN());

                    // 保存更新到数据库
                    caRepository.save(ca);
                } else {
                    log.info("CA {} 正常, DN={}", ca.getCaName(),
                        ca.getCaCertificate().getSubjectDN());
                }
            }
        }
    }
}
