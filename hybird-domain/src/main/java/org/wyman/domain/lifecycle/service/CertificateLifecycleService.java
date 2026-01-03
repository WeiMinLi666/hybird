package org.wyman.domain.lifecycle.service;

import org.springframework.stereotype.Service;
import org.wyman.domain.lifecycle.adapter.port.ICertificateRepository;
import org.wyman.domain.lifecycle.model.aggregate.Certificate;
import org.wyman.types.enums.CertificateStatus;
import org.wyman.types.enums.RevocationReason;

import java.util.List;

/**
 * 证书生命周期领域服务
 */
@Service
public class CertificateLifecycleService {

    private final ICertificateRepository certificateRepository;

    public CertificateLifecycleService(ICertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    /**
     * 激活证书
     */
    public void activateCertificate(String serialNumber) {
        Certificate certificate = certificateRepository.findBySerialNumber(serialNumber);
        if (certificate == null) {
            throw new RuntimeException("证书不存在: " + serialNumber);
        }
        certificate.activate();
        certificateRepository.save(certificate);
    }

    /**
     * 吊销证书
     */
    public void revokeCertificate(String serialNumber,
                                  RevocationReason reason,
                                  String revokedBy,
                                  String comments) {
        Certificate certificate = certificateRepository.findBySerialNumber(serialNumber);
        if (certificate == null) {
            throw new RuntimeException("证书不存在: " + serialNumber);
        }
        certificate.revoke(reason, revokedBy, comments);
        certificateRepository.save(certificate);
    }

    /**
     * 批量吊销证书
     */
    public void revokeCertificates(List<String> serialNumbers,
                                  RevocationReason reason,
                                  String revokedBy,
                                  String comments) {
        for (String serialNumber : serialNumbers) {
            try {
                revokeCertificate(serialNumber, reason, revokedBy, comments);
            } catch (Exception e) {
                // 记录错误,继续处理其他证书
                System.err.println("吊销证书失败: " + serialNumber + ", 错误: " + e.getMessage());
            }
        }
    }

    /**
     * 过期证书检查
     */
    public void checkExpiredCertificates() {
        List<Certificate> activeCertificates = certificateRepository.findByStatus(CertificateStatus.ACTIVE);
        for (Certificate cert : activeCertificates) {
            if (cert.isExpired()) {
                cert.expire();
                certificateRepository.save(cert);
            }
        }
    }

    /**
     * 扫描即将过期的证书并发送通知
     */
    public List<Certificate> scanAndNotifyExpiringCertificates() {
        List<Certificate> expiringCertificates = certificateRepository.findExpiringCertificates(30);
        for (Certificate cert : expiringCertificates) {
            cert.generateRenewalNotice();
            certificateRepository.save(cert);
        }
        return expiringCertificates;
    }

    /**
     * 续期准备
     */
    public void prepareForRenewal(String serialNumber) {
        Certificate certificate = certificateRepository.findBySerialNumber(serialNumber);
        if (certificate == null) {
            throw new RuntimeException("证书不存在: " + serialNumber);
        }
        certificate.markForRenewal();
        certificateRepository.save(certificate);
    }

    /**
     * 查询吊销证书(用于生成CRL)
     */
    public List<Certificate> getRevokedCertificates() {
        return certificateRepository.findRevokedCertificates();
    }

    /**
     * 验证证书有效性
     */
    public boolean validateCertificate(String serialNumber) {
        Certificate certificate = certificateRepository.findBySerialNumber(serialNumber);
        return certificate != null && certificate.isValid();
    }

    /**
     * 根据申请者ID查询证书
     */
    public List<Certificate> getCertificatesByApplicantId(String applicantId) {
        return certificateRepository.findByApplicantId(applicantId);
    }

    /**
     * 根据序列号查询证书
     */
    public Certificate getCertificateBySerialNumber(String serialNumber) {
        return certificateRepository.findBySerialNumber(serialNumber);
    }

    /**
     * 保存证书
     */
    public void saveCertificate(Certificate certificate) {
        certificateRepository.save(certificate);
    }

    /**
     * 为续期标记证书
     */
    public void markCertificateForRenewal(String serialNumber) {
        Certificate certificate = certificateRepository.findBySerialNumber(serialNumber);
        if (certificate == null) {
            throw new RuntimeException("证书不存在: " + serialNumber);
        }
        certificate.markForRenewal();
        certificateRepository.save(certificate);
    }
}
