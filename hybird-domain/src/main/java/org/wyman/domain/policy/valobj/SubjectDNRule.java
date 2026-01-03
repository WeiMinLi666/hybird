package org.wyman.domain.policy.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 主题DN规则值对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDNRule {
    /**
     * 必须包含的属性
     */
    private List<String> requiredAttributes;

    /**
     * CommonName正则模式
     */
    private Pattern commonNamePattern;

    /**
     * 组织单位格式要求
     */
    private String organizationUnitPattern;

    /**
     * 验证主题DN
     */
    public boolean validateSubjectDN(String subjectDN) {
        if (subjectDN == null || subjectDN.isEmpty()) {
            return false;
        }

        // 检查必须的属性
        if (requiredAttributes != null && !requiredAttributes.isEmpty()) {
            for (String attr : requiredAttributes) {
                if (!subjectDN.contains(attr + "=")) {
                    return false;
                }
            }
        }

        // 检查CommonName格式
        if (commonNamePattern != null) {
            java.util.regex.Matcher matcher = commonNamePattern.matcher(subjectDN);
            if (!matcher.find()) {
                return false;
            }
        }

        return true;
    }
}
