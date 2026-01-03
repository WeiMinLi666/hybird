package org.wyman.infrastructure.adapter.repository;

import org.springframework.stereotype.Repository;
import org.wyman.domain.lifecycle.adapter.port.ICertificateRepository;
import org.wyman.domain.lifecycle.model.aggregate.Certificate;
import org.wyman.types.enums.CertificateStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 证书仓储内存实现
 */
@Repository
public class InMemoryCertificateRepository implements ICertificateRepository {

    private final Map<String, Certificate> storage = new HashMap<>();

    @Override
    public void save(Certificate certificate) {
        storage.put(certificate.getSerialNumber(), certificate);
    }

    @Override
    public Certificate findBySerialNumber(String serialNumber) {
        return storage.get(serialNumber);
    }

    @Override
    public List<Certificate> findByApplicantId(String applicantId) {
        return storage.values().stream()
            .filter(cert -> applicantId.equals(cert.getApplicantId()))
            .collect(Collectors.toList());
    }

    @Override
    public List<Certificate> findByStatus(CertificateStatus status) {
        return storage.values().stream()
            .filter(cert -> status == cert.getStatus())
            .collect(Collectors.toList());
    }

    @Override
    public List<Certificate> findExpiringCertificates(int days) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(days);
        return storage.values().stream()
            .filter(cert -> cert.getStatus() == CertificateStatus.ACTIVE &&
                         cert.getNotAfter().isBefore(threshold) &&
                         !cert.isExpired())
            .collect(Collectors.toList());
    }

    @Override
    public List<Certificate> findRevokedCertificates() {
        return storage.values().stream()
            .filter(cert -> cert.getStatus() == CertificateStatus.REVOKED)
            .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(String serialNumber, CertificateStatus status) {
        Certificate cert = storage.get(serialNumber);
        if (cert != null) {
            cert.setStatus(status);
        }
    }
}
