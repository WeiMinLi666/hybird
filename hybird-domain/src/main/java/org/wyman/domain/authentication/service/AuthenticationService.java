package org.wyman.domain.authentication.service;

import org.springframework.stereotype.Service;
import org.wyman.domain.authentication.adapter.port.ICSRParser;
import org.wyman.domain.authentication.adapter.port.IAuthenticationRequestRepository;
import org.wyman.domain.authentication.adapter.port.IIdentityProvider;
import org.wyman.domain.authentication.model.aggregate.AuthenticationRequest;
import org.wyman.domain.authentication.model.entity.Applicant;
import org.wyman.domain.authentication.valobj.CertificateSigningRequest;
import org.wyman.types.exception.AppException;

/**
 * 身份验证领域服务
 */
@Service
public class AuthenticationService {

    private final ICSRParser csrParser;
    private final IAuthenticationRequestRepository repository;
    private final IIdentityProvider identityProvider;

    public AuthenticationService(ICSRParser csrParser,
                                  IAuthenticationRequestRepository repository,
                                  IIdentityProvider identityProvider) {
        this.csrParser = csrParser;
        this.repository = repository;
        this.identityProvider = identityProvider;
    }

    /**
     * 处理认证请求
     */
    public AuthenticationRequest processAuthentication(String requestId,
                                                       String applicantId,
                                                       String idToken,
                                                       String csrPemData) {
        // 验证IdP令牌
        if (!identityProvider.verifyToken(idToken)) {
            throw new AppException("IdP令牌验证失败");
        }

        String idpSubject = identityProvider.extractSubject(idToken);
        Applicant applicant = identityProvider.getApplicantInfo(applicantId);

        // 解析CSR
        CertificateSigningRequest csr = csrParser.parsePEM(csrPemData);
        if (!csrParser.verifySignature(csr)) {
            throw new AppException("CSR签名验证失败");
        }

        // 创建认证请求
        AuthenticationRequest authRequest = new AuthenticationRequest(
            requestId,
            applicant,
            csr
        );

        // 执行验证
        authRequest.startValidation();

        if (!authRequest.validateCSRSignature()) {
            authRequest.failValidation("CSR签名验证失败");
            repository.save(authRequest);
            return authRequest;
        }

        if (!authRequest.validateApplicantIdentity(idpSubject)) {
            authRequest.failValidation("申请者身份验证失败");
            repository.save(authRequest);
            return authRequest;
        }

        if (!authRequest.validateKeyAlgorithm()) {
            authRequest.failValidation("密钥算法不合规");
            repository.save(authRequest);
            return authRequest;
        }

        // 验证成功
        authRequest.completeValidation();
        repository.save(authRequest);

        return authRequest;
    }
}
