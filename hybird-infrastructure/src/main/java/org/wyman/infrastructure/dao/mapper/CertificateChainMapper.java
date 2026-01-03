package org.wyman.infrastructure.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wyman.infrastructure.dao.po.CertificateChainPO;

import java.util.List;

/**
 * 证书链Mapper接口
 */
@Mapper
public interface CertificateChainMapper {

    /**
     * 插入证书链记录
     */
    int insert(CertificateChainPO chain);

    /**
     * 根据证书序列号查询完整证书链
     */
    List<CertificateChainPO> selectByCertificateSerialNumber(@Param("serialNumber") String serialNumber);

    /**
     * 根据父证书序列号查询
     */
    List<CertificateChainPO> selectByParentSerialNumber(@Param("parentSerialNumber") String parentSerialNumber);
}
