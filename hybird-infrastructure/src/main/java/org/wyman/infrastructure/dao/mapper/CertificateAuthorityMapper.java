package org.wyman.infrastructure.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wyman.infrastructure.dao.po.CertificateAuthorityPO;

import java.util.List;

/**
 * 证书颁发机构Mapper接口
 */
@Mapper
public interface CertificateAuthorityMapper {

    /**
     * 插入CA
     */
    int insert(CertificateAuthorityPO ca);

    /**
     * 更新CA
     */
    int update(CertificateAuthorityPO ca);

    /**
     * 根据CA ID查询
     */
    CertificateAuthorityPO selectById(@Param("caId") String caId);

    /**
     * 查询所有CA
     */
    List<CertificateAuthorityPO> selectAll();

    /**
     * 查询启用的CA
     */
    List<CertificateAuthorityPO> selectEnabled();
}
