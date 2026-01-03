package org.wyman.domain.authentication.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wyman.domain.authentication.model.entity.Applicant;
import org.wyman.domain.authentication.valobj.CertificateSigningRequest;
import org.wyman.types.enums.AuthRequestStatus;
import org.wyman.types.event.AuthenticationCompletedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 身份验证请求聚合根
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {
    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 申请者
     */
    private Applicant applicant;

    /**
     * 证书签名请求
     */
    private CertificateSigningRequest csr;

    /**
     * 验证状态
     */
    private AuthRequestStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 验证失败原因
     */
    private String failureReason;

    /**
     * 领域事件集合
     */
    private transient List<Object> domainEvents = new ArrayList<>();

    public AuthenticationRequest(String requestId, Applicant applicant, CertificateSigningRequest csr) {
        this.requestId = requestId;
        this.applicant = applicant;
        this.csr = csr;
        this.status = AuthRequestStatus.PENDING_VALIDATION;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 启动验证流程
     */
    public void startValidation() {
        if (this.status != AuthRequestStatus.PENDING_VALIDATION) {
            throw new IllegalStateException("只有待验证状态的请求才能启动验证");
        }
    }

    /**
     * 完成验证(成功)
     */
    public void completeValidation() {
        this.status = AuthRequestStatus.VALIDATION_SUCCESSFUL;
        this.updateTime = LocalDateTime.now();
        addDomainEvent(new AuthenticationCompletedEvent(
            requestId,
            applicant.getApplicantId(),
            true,
            null
        ));
    }

    /**
     * 完成验证(失败)
     */
    public void failValidation(String reason) {
        this.status = AuthRequestStatus.VALIDATION_FAILED;
        this.failureReason = reason;
        this.updateTime = LocalDateTime.now();
        addDomainEvent(new AuthenticationCompletedEvent(
            requestId,
            applicant.getApplicantId(),
            false,
            reason
        ));
    }

    /**
     * 验证CSR签名
     */
    public boolean validateCSRSignature() {
        return csr != null && csr.isSignatureValid();
    }

    /**
     * 验证申请者身份
     * 比较CSR主题与IdP数据
     */
    public boolean validateApplicantIdentity(String idpSubject) {
        if (applicant == null || csr == null) {
            return false;
        }
        String csrSubject = csr.getSubjectDN();
        return csrSubject != null && csrSubject.contains(idpSubject);
    }

    /**
     * 验证密钥算法合规性
     */
    public boolean validateKeyAlgorithm() {
        if (csr == null || csr.getPublicKeyAlgorithm() == null) {
            return false;
        }
        String algorithm = csr.getPublicKeyAlgorithm();
        // 允许的算法: SM2, ML-DSA, ML-KEM, RSA, ECDSA
        return algorithm.matches("(?i)(SM2|ML-DSA|ML-KEM|RSA|ECDSA)");
    }

    /**
     * 添加领域事件
     */
    private void addDomainEvent(Object event) {
        if (this.domainEvents == null) {
            this.domainEvents = new ArrayList<>();
        }
        this.domainEvents.add(event);
    }

    /**
     * 获取并清空领域事件
     */
    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }
}
