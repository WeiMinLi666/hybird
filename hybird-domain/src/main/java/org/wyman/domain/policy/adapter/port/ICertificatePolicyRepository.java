package org.wyman.domain.policy.adapter.port;

import org.wyman.domain.policy.model.aggregate.CertificatePolicy;
import org.wyman.types.enums.CertificateType;

import java.util.List;

/**
 * 证书策略仓储接口
 */
public interface ICertificatePolicyRepository {
    /**
     * 保存策略
     */
    void save(CertificatePolicy policy);

    /**
     * 根据策略ID查询
     */
    CertificatePolicy findById(String policyId);

    /**
     * 根据证书类型查询启用的策略
     */
    CertificatePolicy findEnabledByType(CertificateType certificateType);

    /**
     * 查询所有策略
     */
    List<CertificatePolicy> findAll();

    /**
     * 查询启用的策略
     */
    List<CertificatePolicy> findEnabled();
}
