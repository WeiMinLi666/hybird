package org.wyman.config;

import org.springframework.context.annotation.Configuration;
import org.wyman.domain.authentication.service.AuthenticationService;
import org.wyman.domain.audit.service.AuditService;
import org.wyman.domain.lifecycle.service.CertificateLifecycleService;
import org.wyman.domain.policy.service.PolicyService;
import org.wyman.domain.signing.service.SigningService;
import org.wyman.domain.status.service.RevocationStatusService;

/**
 * 领域服务配置
 */
@Configuration
public class DomainConfig {

    public DomainConfig(AuthenticationService authenticationService,
                        PolicyService policyService,
                        SigningService signingService,
                        CertificateLifecycleService lifecycleService,
                        RevocationStatusService revocationStatusService,
                        AuditService auditService) {
        // 领域服务已通过Spring自动装配注入
    }
}
