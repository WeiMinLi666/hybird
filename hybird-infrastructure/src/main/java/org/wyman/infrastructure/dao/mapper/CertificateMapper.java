package org.wyman.infrastructure.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wyman.infrastructure.dao.po.CertificatePO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 证书Mapper接口
 */
@Mapper
public interface CertificateMapper {

    /**
     * 插入证书
     */
    int insert(CertificatePO certificate);

    /**
     * 更新证书
     */
    int update(CertificatePO certificate);

    /**
     * 根据序列号查询
     */
    CertificatePO selectBySerialNumber(@Param("serialNumber") String serialNumber);

    /**
     * 根据申请者ID查询
     */
    List<CertificatePO> selectByApplicantId(@Param("applicantId") String applicantId);

    /**
     * 根据状态查询
     */
    List<CertificatePO> selectByStatus(@Param("status") String status);

    /**
     * 查询即将过期的证书
     */
    List<CertificatePO> selectExpiringCertificates(@Param("threshold") LocalDateTime threshold);

    /**
     * 查询已吊销的证书
     */
    List<CertificatePO> selectRevokedCertificates();

    /**
     * 更新证书状态
     */
    int updateStatus(@Param("serialNumber") String serialNumber,
                     @Param("status") String status);

    /**
     * 更新吊销信息
     */
    int updateRevocationInfo(CertificatePO certificate);
}
