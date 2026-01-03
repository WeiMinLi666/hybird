package org.wyman.infrastructure.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wyman.infrastructure.dao.po.CertificatePolicyPO;

import java.util.List;

/**
 * 证书策略Mapper接口
 */
@Mapper
public interface CertificatePolicyMapper {

    /**
     * 插入策略
     */
    int insert(CertificatePolicyPO policy);

    /**
     * 更新策略
     */
    int update(CertificatePolicyPO policy);

    /**
     * 根据策略ID查询
     */
    CertificatePolicyPO selectById(@Param("policyId") String policyId);

    /**
     * 根据证书类型查询启用的策略
     */
    CertificatePolicyPO selectEnabledByType(@Param("certificateType") String certificateType);

    /**
     * 查询所有策略
     */
    List<CertificatePolicyPO> selectAll();

    /**
     * 查询启用的策略
     */
    List<CertificatePolicyPO> selectEnabled();
}
