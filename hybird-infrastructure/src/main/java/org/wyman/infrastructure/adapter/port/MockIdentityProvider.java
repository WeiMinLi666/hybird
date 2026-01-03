package org.wyman.infrastructure.adapter.port;

import org.springframework.stereotype.Component;
import org.wyman.domain.authentication.adapter.port.IIdentityProvider;
import org.wyman.domain.authentication.model.entity.Applicant;

/**
 * 模拟身份提供者实现
 */
@Component
public class MockIdentityProvider implements IIdentityProvider {

    @Override
    public boolean verifyToken(String idToken) {
        // 简化实现:所有token都验证通过
        return idToken != null && !idToken.isEmpty();
    }

    @Override
    public String extractSubject(String idToken) {
        // 简化实现:从token中提取subject
        if (idToken.contains("subject=")) {
            int start = idToken.indexOf("subject=") + 8;
            int end = idToken.indexOf(",", start);
            if (end == -1) end = idToken.length();
            return idToken.substring(start, end).trim();
        }
        return "default-user";
    }

    @Override
    public Applicant getApplicantInfo(String userId) {
        // 简化实现:返回模拟的申请者信息
        return new Applicant(
            userId,
            "User-" + userId
        );
    }
}
