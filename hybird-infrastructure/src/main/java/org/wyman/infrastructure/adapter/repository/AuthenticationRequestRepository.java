package org.wyman.infrastructure.adapter.repository;

import org.springframework.stereotype.Repository;
import org.wyman.domain.authentication.adapter.port.IAuthenticationRequestRepository;
import org.wyman.domain.authentication.model.aggregate.AuthenticationRequest;
import org.wyman.domain.authentication.model.entity.Applicant;
import org.wyman.domain.authentication.valobj.CertificateSigningRequest;
import org.wyman.infrastructure.dao.mapper.AuthenticationRequestMapper;
import org.wyman.infrastructure.dao.po.AuthenticationRequestPO;
import org.wyman.types.enums.AuthRequestStatus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 身份验证请求仓储MyBatis实现
 */
@Repository
public class AuthenticationRequestRepository implements IAuthenticationRequestRepository {

    private final AuthenticationRequestMapper authenticationRequestMapper;

    public AuthenticationRequestRepository(AuthenticationRequestMapper authenticationRequestMapper) {
        this.authenticationRequestMapper = authenticationRequestMapper;
    }

    @Override
    public void save(AuthenticationRequest request) {
        AuthenticationRequestPO po = toPO(request);
        AuthenticationRequestPO existing = authenticationRequestMapper.selectById(request.getRequestId());
        if (existing == null) {
            authenticationRequestMapper.insert(po);
        } else {
            authenticationRequestMapper.update(po);
        }
    }

    @Override
    public AuthenticationRequest findById(String requestId) {
        AuthenticationRequestPO po = authenticationRequestMapper.selectById(requestId);
        return toDomain(po);
    }

    @Override
    public List<AuthenticationRequest> findByApplicantId(String applicantId) {
        List<AuthenticationRequestPO> poList = authenticationRequestMapper.selectByApplicantId(applicantId);
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    private AuthenticationRequestPO toPO(AuthenticationRequest request) {
        return AuthenticationRequestPO.builder()
                .requestId(request.getRequestId())
                .applicantId(request.getApplicant().getApplicantId())
                .applicantName(request.getApplicant().getName())
                .applicantEmail(request.getApplicant().getEmail())
                .csrContent(request.getCsr() != null ? request.getCsr().getCsrData() : null)
                .csrSubjectDn(request.getCsr() != null ? request.getCsr().getSubjectDN() : null)
                .publicKeyAlgorithm(request.getCsr() != null ? request.getCsr().getPublicKeyAlgorithm() : null)
                .status(request.getStatus().name())
                .failureReason(request.getFailureReason())
                .createTime(request.getCreateTime() != null ? request.getCreateTime() : java.time.LocalDateTime.now())
                .updateTime(request.getUpdateTime() != null ? request.getUpdateTime() : java.time.LocalDateTime.now())
                .build();
    }

    private AuthenticationRequest toDomain(AuthenticationRequestPO po) {
        if (po == null) {
            return null;
        }

        Applicant applicant = new Applicant(
                po.getApplicantId(),
                po.getApplicantName()
        );
        applicant.setEmail(po.getApplicantEmail());

        CertificateSigningRequest csr = new CertificateSigningRequest();
        csr.setCsrData(po.getCsrContent());
        csr.setSubjectDN(po.getCsrSubjectDn());
        csr.setPublicKeyAlgorithm(po.getPublicKeyAlgorithm());

        AuthenticationRequest request = new AuthenticationRequest(
                po.getRequestId(),
                applicant,
                csr
        );
        request.setStatus(AuthRequestStatus.valueOf(po.getStatus()));
        request.setFailureReason(po.getFailureReason());
        request.setCreateTime(po.getCreateTime());
        request.setUpdateTime(po.getUpdateTime());

        return request;
    }
}
