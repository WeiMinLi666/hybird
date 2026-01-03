package org.wyman.domain.lifecycle.adapter.port;

import org.wyman.domain.lifecycle.model.aggregate.Certificate;
import org.wyman.types.enums.CertificateStatus;

import java.util.List;

/**
 * 证书仓储接口
 */
public interface ICertificateRepository {
    /**
     * 保存证书
     */
    void save(Certificate certificate);

    /**
     * 根据序列号查询
     */
    Certificate findBySerialNumber(String serialNumber);

    /**
     * 根据申请者ID查询
     */
    List<Certificate> findByApplicantId(String applicantId);

    /**
     * 根据状态查询
     */
    List<Certificate> findByStatus(CertificateStatus status);

    /**
     * 查询即将过期的证书(在指定天数内)
     */
    List<Certificate> findExpiringCertificates(int days);

    /**
     * 查询已激活的吊销证书
     */
    List<Certificate> findRevokedCertificates();

    /**
     * 更新证书状态
     */
    void updateStatus(String serialNumber, CertificateStatus status);
}
