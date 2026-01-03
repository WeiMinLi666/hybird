package org.wyman.domain.authentication.adapter.port;

import org.wyman.domain.authentication.model.aggregate.AuthenticationRequest;

/**
 * 身份验证仓储接口
 */
public interface IAuthenticationRequestRepository {
    /**
     * 保存认证请求
     */
    void save(AuthenticationRequest request);

    /**
     * 根据请求ID查询
     */
    AuthenticationRequest findById(String requestId);

    /**
     * 根据申请者ID查询
     */
    java.util.List<AuthenticationRequest> findByApplicantId(String applicantId);
}
