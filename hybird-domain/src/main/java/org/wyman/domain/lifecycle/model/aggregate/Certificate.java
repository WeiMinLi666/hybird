package org.wyman.domain.lifecycle.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wyman.domain.lifecycle.valobj.NotificationPolicy;
import org.wyman.domain.lifecycle.valobj.RevocationInfo;
import org.wyman.types.enums.CertificateStatus;
import org.wyman.types.enums.CertificateType;
import org.wyman.types.enums.RevocationReason;
import org.wyman.types.event.CertificateIssuedEvent;
import org.wyman.types.event.CertificateRevokedEvent;
import org.wyman.types.event.RenewalNoticeDueEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 证书聚合根
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {
    /**
     * 证书序列号
     */
    private String serialNumber;

    /**
     * 证书类型
     */
    private CertificateType certificateType;

    /**
     * 主题DN
     */
    private String subjectDN;

    /**
     * 颁发者DN
     */
    private String issuerDN;

    /**
     * 状态
     */
    private CertificateStatus status;

    /**
     * 有效期开始
     */
    private LocalDateTime notBefore;

    /**
     * 有效期结束
     */
    private LocalDateTime notAfter;

    /**
     * 申请者ID
     */
    private String applicantId;

    /**
     * 签发请求ID
     */
    private String issuanceRequestId;

    /**
     * 证书PEM编码
     */
    private String pemEncoded;

    /**
     * 吊销信息
     */
    private RevocationInfo revocationInfo;

    /**
     * 通知策略
     */
    private NotificationPolicy notificationPolicy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 领域事件集合
     */
    private transient List<Object> domainEvents = new ArrayList<>();

    public Certificate(String serialNumber,
                       CertificateType certificateType,
                       String subjectDN,
                       String issuerDN,
                       LocalDateTime notBefore,
                       LocalDateTime notAfter,
                       String applicantId) {
        this.serialNumber = serialNumber;
        this.certificateType = certificateType;
        this.subjectDN = subjectDN;
        this.issuerDN = issuerDN;
        this.status = CertificateStatus.PENDING_ISSUANCE;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.applicantId = applicantId;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.notificationPolicy = new NotificationPolicy(30); // 默认30天临期通知
    }

    /**
     * 激活证书
     */
    public void activate() {
        if (this.status != CertificateStatus.PENDING_ISSUANCE) {
            throw new IllegalStateException("只有待签发状态的证书才能激活");
        }
        this.status = CertificateStatus.ACTIVE;
        this.updateTime = LocalDateTime.now();
        addDomainEvent(new CertificateIssuedEvent(serialNumber, subjectDN, issuanceRequestId));
    }

    /**
     * 吊销证书
     */
    public void revoke(RevocationReason reason, String revokedBy, String comments) {
        if (this.status != CertificateStatus.ACTIVE) {
            throw new IllegalStateException("只有激活状态的证书才能吊销");
        }

        this.status = CertificateStatus.REVOKED;
        this.revocationInfo = new RevocationInfo(
            reason.getDesc(),
            revokedBy
        );
        this.revocationInfo.setComments(comments);
        this.updateTime = LocalDateTime.now();

        addDomainEvent(new CertificateRevokedEvent(
            serialNumber,
            reason.getDesc(),
            LocalDateTime.now()
        ));
    }

    /**
     * 过期证书
     */
    public void expire() {
        if (this.status != CertificateStatus.ACTIVE) {
            throw new IllegalStateException("只有激活状态的证书才能过期");
        }

        this.status = CertificateStatus.EXPIRED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记为待续期
     */
    public void markForRenewal() {
        if (this.status != CertificateStatus.ACTIVE) {
            throw new IllegalStateException("只有激活状态的证书才能标记续期");
        }

        this.status = CertificateStatus.RENEWAL_DUE;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 检查证书是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(notAfter);
    }

    /**
     * 获取距离过期的天数
     */
    public long getDaysUntilExpiry() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(notAfter)) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(now, notAfter);
    }

    /**
     * 检查是否需要发送临期通知
     */
    public boolean needsRenewalNotice() {
        if (notificationPolicy == null || status != CertificateStatus.ACTIVE) {
            return false;
        }
        int daysUntilExpiry = (int) getDaysUntilExpiry();
        return notificationPolicy.shouldNotifyExpiry(daysUntilExpiry);
    }

    /**
     * 生成临期通知事件
     */
    public void generateRenewalNotice() {
        if (needsRenewalNotice()) {
            addDomainEvent(new RenewalNoticeDueEvent(
                serialNumber,
                subjectDN,
                (int) getDaysUntilExpiry()
            ));
        }
    }

    /**
     * 验证证书状态
     */
    public boolean isValid() {
        return status == CertificateStatus.ACTIVE && !isExpired();
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
