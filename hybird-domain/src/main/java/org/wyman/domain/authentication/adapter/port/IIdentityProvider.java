package org.wyman.domain.authentication.adapter.port;

import org.wyman.domain.authentication.model.entity.Applicant;

/**
 * 身份提供者接口
 */
public interface IIdentityProvider {
    /**
     * 验证IdP令牌
     */
    boolean verifyToken(String idToken);

    /**
     * 从令牌中提取用户身份信息
     */
    String extractSubject(String idToken);

    /**
     * 获取申请者信息
     */
    Applicant getApplicantInfo(String userId);
}
