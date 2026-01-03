package org.wyman.domain.signing.service;

import org.springframework.stereotype.Service;
import org.wyman.domain.signing.adapter.port.ICertificateAuthorityRepository;
import org.wyman.domain.signing.adapter.port.IObjectStorageGateway;
import org.wyman.domain.signing.adapter.port.IPrivateKeyProvider;
import org.wyman.domain.signing.model.aggregate.CertificateAuthority;
import org.wyman.domain.signing.valobj.CRL;
import org.wyman.domain.signing.valobj.Certificate;
import org.wyman.domain.signing.valobj.RevokedCertificate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 签名领域服务
 */
@Service
public class SigningService {

    private final ICertificateAuthorityRepository caRepository;
    private final IObjectStorageGateway objectStorageGateway;
    private final IPrivateKeyProvider keyProvider;

    public SigningService(ICertificateAuthorityRepository caRepository,
                          IObjectStorageGateway objectStorageGateway,
                          IPrivateKeyProvider keyProvider) {
        this.caRepository = caRepository;
        this.objectStorageGateway = objectStorageGateway;
        this.keyProvider = keyProvider;
    }

    /**
     * 签发证书
     */
    public Certificate issueCertificate(String caName,
                                       String subjectDN,
                                       java.security.PublicKey publicKey,
                                       LocalDateTime notBefore,
                                       LocalDateTime notAfter,
                                       String signatureAlgorithm,
                                       String kemAlgorithm) {
        CertificateAuthority ca = caRepository.findByName(caName);
        if (ca == null) {
            throw new RuntimeException("CA不存在: " + caName);
        }

        Certificate certificate = ca.issueCertificate(
            subjectDN,
            publicKey,
            notBefore,
            notAfter,
            signatureAlgorithm,
            kemAlgorithm,
            keyProvider
        );

        // 保存CA状态
        caRepository.save(ca);

        return certificate;
    }

    /**
     * 生成CRL
     */
    public CRL generateCRL(String caName,
                           List<RevokedCertificate> revokedCerts,
                           String signatureAlgorithm) {
        CertificateAuthority ca = caRepository.findByName(caName);
        if (ca == null) {
            throw new RuntimeException("CA不存在: " + caName);
        }

        CRL crl = ca.generateCRL(revokedCerts, keyProvider, signatureAlgorithm);

        // 上传CRL到对象存储
        objectStorageGateway.uploadCRL(crl.getCrlNumber(), crl.getPemEncoded());

        // 保存CA状态
        caRepository.save(ca);

        return crl;
    }
}
