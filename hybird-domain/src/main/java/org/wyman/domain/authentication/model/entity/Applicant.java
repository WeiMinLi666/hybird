package org.wyman.domain.authentication.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 申请者实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Applicant {
    /**
     * 申请者ID
     */
    private String applicantId;

    /**
     * 申请者姓名
     */
    private String name;

    /**
     * 组织
     */
    private String organization;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 认证方式(OAuth, SAML等)
     */
    private String authMethod;

    /**
     * IdP提供的身份令牌
     */
    private String idToken;

    public Applicant(String applicantId, String name) {
        this.applicantId = applicantId;
        this.name = name;
    }
}
