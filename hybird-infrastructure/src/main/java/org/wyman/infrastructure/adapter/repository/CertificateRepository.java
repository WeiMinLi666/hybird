package org.wyman.infrastructure.adapter.repository;

import org.springframework.stereotype.Repository;
import org.wyman.domain.lifecycle.adapter.port.ICertificateRepository;
import org.wyman.domain.lifecycle.model.aggregate.Certificate;
import org.wyman.domain.lifecycle.valobj.NotificationPolicy;
import org.wyman.domain.lifecycle.valobj.RevocationInfo;
import org.wyman.infrastructure.dao.mapper.CertificateMapper;
import org.wyman.infrastructure.dao.po.CertificatePO;
import org.wyman.types.enums.CertificateStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 证书仓储MyBatis实现
 */
@Repository
public class CertificateRepository implements ICertificateRepository {

    private final CertificateMapper certificateMapper;

    public CertificateRepository(CertificateMapper certificateMapper) {
        this.certificateMapper = certificateMapper;
    }

    @Override
    public void save(Certificate certificate) {
        CertificatePO po = toPO(certificate);
        CertificatePO existing = certificateMapper.selectBySerialNumber(certificate.getSerialNumber());
        if (existing == null) {
            certificateMapper.insert(po);
        } else {
            certificateMapper.update(po);
        }
    }

    @Override
    public Certificate findBySerialNumber(String serialNumber) {
        CertificatePO po = certificateMapper.selectBySerialNumber(serialNumber);
        return toDomain(po);
    }

    @Override
    public List<Certificate> findByApplicantId(String applicantId) {
        List<CertificatePO> poList = certificateMapper.selectByApplicantId(applicantId);
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Certificate> findByStatus(CertificateStatus status) {
        List<CertificatePO> poList = certificateMapper.selectByStatus(status.name());
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Certificate> findExpiringCertificates(int days) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(days);
        List<CertificatePO> poList = certificateMapper.selectExpiringCertificates(threshold);
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Certificate> findRevokedCertificates() {
        List<CertificatePO> poList = certificateMapper.selectRevokedCertificates();
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void updateStatus(String serialNumber, CertificateStatus status) {
        certificateMapper.updateStatus(serialNumber, status.name());
    }

    private CertificatePO toPO(Certificate certificate) {
        CertificatePO po = CertificatePO.builder()
                .serialNumber(certificate.getSerialNumber())
                .certificateType(certificate.getCertificateType().name())
                .subjectDn(certificate.getSubjectDN())
                .issuerDn(certificate.getIssuerDN())
                .status(certificate.getStatus().name())
                .notBefore(certificate.getNotBefore())
                .notAfter(certificate.getNotAfter())
                .applicantId(certificate.getApplicantId())
                .issuanceRequestId(certificate.getIssuanceRequestId())
                .pemEncoded(certificate.getPemEncoded())
                .postQuantumCsrPem(certificate.getPostQuantumCsrPem())
                .postQuantumPublicKeyPem(certificate.getPostQuantumPublicKeyPem())
                .renewalNoticeDays(certificate.getNotificationPolicy() != null ?
                        certificate.getNotificationPolicy().getExpiryNotificationDays() : 30)
                .createTime(certificate.getCreateTime() != null ? certificate.getCreateTime() : LocalDateTime.now())
                .updateTime(certificate.getUpdateTime() != null ? certificate.getUpdateTime() : LocalDateTime.now())
                .build();

        if (certificate.getRevocationInfo() != null) {
            po.setRevocationReason(certificate.getRevocationInfo().getRevocationReason());
            po.setRevokedBy(certificate.getRevocationInfo().getRevokedBy());
            po.setRevocationComments(certificate.getRevocationInfo().getComments());
        }

        return po;
    }

    private Certificate toDomain(CertificatePO po) {
        if (po == null) {
            return null;
        }

        Certificate certificate = new Certificate();
        certificate.setSerialNumber(po.getSerialNumber());
        certificate.setCertificateType(org.wyman.types.enums.CertificateType.valueOf(po.getCertificateType()));
        certificate.setSubjectDN(po.getSubjectDn());
        certificate.setIssuerDN(po.getIssuerDn());
        certificate.setStatus(CertificateStatus.valueOf(po.getStatus()));
        certificate.setNotBefore(po.getNotBefore());
        certificate.setNotAfter(po.getNotAfter());
        certificate.setApplicantId(po.getApplicantId());
        certificate.setIssuanceRequestId(po.getIssuanceRequestId());
        certificate.setPemEncoded(po.getPemEncoded());
        certificate.setPostQuantumCsrPem(po.getPostQuantumCsrPem());
        certificate.setPostQuantumPublicKeyPem(po.getPostQuantumPublicKeyPem());
        certificate.setCreateTime(po.getCreateTime());
        certificate.setUpdateTime(po.getUpdateTime());

        if (po.getRevocationReason() != null) {
            RevocationInfo revocationInfo = new RevocationInfo(po.getRevocationReason(), po.getRevokedBy());
            revocationInfo.setComments(po.getRevocationComments());
            certificate.setRevocationInfo(revocationInfo);
        }

        if (po.getRenewalNoticeDays() != null) {
            NotificationPolicy notificationPolicy = new NotificationPolicy();
            notificationPolicy.setExpiryNotificationDays(po.getRenewalNoticeDays());
            certificate.setNotificationPolicy(notificationPolicy);
        } else {
            NotificationPolicy notificationPolicy = new NotificationPolicy();
            notificationPolicy.setExpiryNotificationDays(30);
            certificate.setNotificationPolicy(notificationPolicy);
        }

        return certificate;
    }
}
