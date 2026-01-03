package org.wyman.domain.chain.service;

import org.springframework.stereotype.Service;
import org.wyman.domain.lifecycle.adapter.port.ICertificateRepository;
import org.wyman.domain.lifecycle.model.aggregate.Certificate;

import java.util.ArrayList;
import java.util.List;

/**
 * 证书链领域服务
 */
@Service
public class CertificateChainService {

    private final ICertificateRepository certificateRepository;

    public CertificateChainService(ICertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    /**
     * 证书链信息
     */
    public static class CertificateChainInfo {
        private String serialNumber;
        private String subjectDN;
        private String issuerDN;
        private String certificatePem;
        private int level;

        public CertificateChainInfo(String serialNumber, String subjectDN, String issuerDN,
                                     String certificatePem, int level) {
            this.serialNumber = serialNumber;
            this.subjectDN = subjectDN;
            this.issuerDN = issuerDN;
            this.certificatePem = certificatePem;
            this.level = level;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public String getSubjectDN() {
            return subjectDN;
        }

        public void setSubjectDN(String subjectDN) {
            this.subjectDN = subjectDN;
        }

        public String getIssuerDN() {
            return issuerDN;
        }

        public void setIssuerDN(String issuerDN) {
            this.issuerDN = issuerDN;
        }

        public String getCertificatePem() {
            return certificatePem;
        }

        public void setCertificatePem(String certificatePem) {
            this.certificatePem = certificatePem;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }
    }

    /**
     * 查询证书链
     * 注意: 此实现使用简化的证书链构建逻辑
     * 实际生产环境需要根据证书链表或证书中的 AuthorityKeyIdentifier 和 SubjectKeyIdentifier 构建
     */
    public List<CertificateChainInfo> getCertificateChain(String serialNumber) {
        List<CertificateChainInfo> chain = new ArrayList<>();

        Certificate certificate = certificateRepository.findBySerialNumber(serialNumber);
        if (certificate == null) {
            return chain;
        }

        // 添加当前证书
        chain.add(new CertificateChainInfo(
            certificate.getSerialNumber(),
            certificate.getSubjectDN(),
            certificate.getIssuerDN(),
            certificate.getPemEncoded(),
            0
        ));

        // 简化实现: 如果 IssuerDN 与 SubjectDN 不同，尝试查找父证书
        // 实际实现应该从证书链表或通过证书扩展信息构建
        if (!certificate.getSubjectDN().equals(certificate.getIssuerDN())) {
            List<Certificate> allCertificates = certificateRepository.findByStatus(
                org.wyman.types.enums.CertificateStatus.ACTIVE);

            for (Certificate cert : allCertificates) {
                if (cert.getSubjectDN().equals(certificate.getIssuerDN())) {
                    chain.add(new CertificateChainInfo(
                        cert.getSerialNumber(),
                        cert.getSubjectDN(),
                        cert.getIssuerDN(),
                        cert.getPemEncoded(),
                        1
                    ));
                    break;
                }
            }
        }

        return chain;
    }
}
