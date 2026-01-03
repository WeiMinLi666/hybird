package org.wyman.trigger.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.wyman.api.dto.PolicyCreateRequest;
import org.wyman.api.dto.PolicyQueryResponse;
import org.wyman.api.dto.PolicyValidationRequest;
import org.wyman.api.dto.PolicyValidationResponse;
import org.wyman.api.response.Response;
import org.wyman.domain.policy.service.PolicyService;
import org.wyman.domain.policy.model.aggregate.CertificatePolicy;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 证书策略控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/policy")
@CrossOrigin("*")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    /**
     * 创建证书策略
     */
    @PostMapping("/create")
    public Response<PolicyQueryResponse> createPolicy(@RequestBody PolicyCreateRequest request) {
        try {
            CertificatePolicy policy = policyService.createPolicy(
                request.getPolicyName(),
                request.getDescription(),
                request.getAllowedAlgorithms(),
                request.getValidityPeriod(),
                request.getKeySize()
            );

            PolicyQueryResponse response = new PolicyQueryResponse();
            response.setPolicyId(policy.getPolicyId());
            response.setPolicyName(policy.getPolicyName());
            response.setVersion(Integer.parseInt(policy.getVersion()));
            response.setIsActive(policy.isEnabled());
            response.setCreateTime(policy.getCreateTime().toString());

            // 从密码学规则中提取算法
            if (policy.getCryptographicRule() != null) {
                response.setAllowedAlgorithms(policy.getCryptographicRule().getAllowedSignatureAlgorithms().toArray(new String[0]));
                response.setKeySize(policy.getCryptographicRule().getMinKeyLength());
            }

            // 从有效期规则中提取有效期
            if (policy.getValidityPeriodRule() != null) {
                response.setValidityPeriod(policy.getValidityPeriodRule().getMaxDays());
            }

            return Response.success(response);
        } catch (Exception e) {
            log.error("创建证书策略失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询所有策略
     */
    @GetMapping("/list")
    public Response<List<PolicyQueryResponse>> listPolicies() {
        try {
            List<CertificatePolicy> policies = policyService.getAllPolicies();

            List<PolicyQueryResponse> responses = policies.stream()
                .map(policy -> {
                    PolicyQueryResponse response = new PolicyQueryResponse();
                    response.setPolicyId(policy.getPolicyId());
                    response.setPolicyName(policy.getPolicyName());
                    response.setVersion(Integer.parseInt(policy.getVersion()));
                    response.setIsActive(policy.isEnabled());
                    response.setCreateTime(policy.getCreateTime().toString());

                    // 从密码学规则中提取算法
                    if (policy.getCryptographicRule() != null) {
                        response.setAllowedAlgorithms(policy.getCryptographicRule().getAllowedSignatureAlgorithms().toArray(new String[0]));
                        response.setKeySize(policy.getCryptographicRule().getMinKeyLength());
                    }

                    // 从有效期规则中提取有效期
                    if (policy.getValidityPeriodRule() != null) {
                        response.setValidityPeriod(policy.getValidityPeriodRule().getMaxDays());
                    }

                    return response;
                })
                .collect(Collectors.toList());

            return Response.success(responses);
        } catch (Exception e) {
            log.error("查询策略列表失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 激活/停用策略
     */
    @PostMapping("/{policyId}/activate")
    public Response<Void> activatePolicy(
            @PathVariable String policyId,
            @RequestParam boolean active) {
        try {
            policyService.activatePolicy(policyId, active);
            return Response.success();
        } catch (Exception e) {
            log.error("更新策略状态失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 验证CSR是否符合策略
     */
    @PostMapping("/validate")
    public Response<PolicyValidationResponse> validateCSR(
            @RequestBody PolicyValidationRequest request) {
        try {
            boolean isValid = policyService.validateCSR(
                request.getPolicyId(),
                request.getCsrPemData()
            );

            PolicyValidationResponse response = new PolicyValidationResponse();
            response.setIsValid(isValid);
            response.setPolicyId(request.getPolicyId());
            response.setMessage(isValid ? "CSR符合策略要求" : "CSR不符合策略要求");

            return Response.success(response);
        } catch (Exception e) {
            log.error("验证CSR失败", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询策略详情
     */
    @GetMapping("/{policyId}")
    public Response<PolicyQueryResponse> getPolicy(@PathVariable String policyId) {
        try {
            CertificatePolicy policy = policyService.getPolicyById(policyId);
            if (policy == null) {
                return Response.fail("策略不存在");
            }

            PolicyQueryResponse response = new PolicyQueryResponse();
            response.setPolicyId(policy.getPolicyId());
            response.setPolicyName(policy.getPolicyName());
            response.setVersion(Integer.parseInt(policy.getVersion()));
            response.setIsActive(policy.isEnabled());
            response.setCreateTime(policy.getCreateTime().toString());

            // 从密码学规则中提取算法
            if (policy.getCryptographicRule() != null) {
                response.setAllowedAlgorithms(policy.getCryptographicRule().getAllowedSignatureAlgorithms().toArray(new String[0]));
                response.setKeySize(policy.getCryptographicRule().getMinKeyLength());
            }

            // 从有效期规则中提取有效期
            if (policy.getValidityPeriodRule() != null) {
                response.setValidityPeriod(policy.getValidityPeriodRule().getMaxDays());
            }

            return Response.success(response);
        } catch (Exception e) {
            log.error("查询策略详情失败", e);
            return Response.fail(e.getMessage());
        }
    }
}
