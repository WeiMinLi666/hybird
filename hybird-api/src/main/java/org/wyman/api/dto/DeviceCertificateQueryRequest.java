package org.wyman.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询设备证书请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCertificateQueryRequest {
    /**
     * 申请者ID
     */
    @NotBlank(message = "申请者ID不能为空")
    private String applicantId;
}
