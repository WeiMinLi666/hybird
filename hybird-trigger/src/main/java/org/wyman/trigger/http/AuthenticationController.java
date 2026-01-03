package org.wyman.trigger.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.wyman.api.dto.AuthenticationRequest;
import org.wyman.api.dto.AuthenticationResponse;
import org.wyman.api.response.Response;
import org.wyman.domain.authentication.service.AuthenticationService;

import java.util.UUID;

/**
 * 身份验证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/authentication")
@CrossOrigin("*")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * 处理身份验证请求
     */
    @PostMapping("/validate")
    public Response<AuthenticationResponse> validateAuthentication(
        @RequestBody AuthenticationRequest request) {
        try {
            String requestId = UUID.randomUUID().toString();
            var authRequest = authenticationService.processAuthentication(
                requestId,
                request.getApplicantId(),
                request.getIdToken(),
                request.getCsrPemData()
            );

            AuthenticationResponse response = new AuthenticationResponse();
            response.setRequestId(authRequest.getRequestId());
            response.setStatus(authRequest.getStatus().getCode());
            response.setSuccess(authRequest.getStatus().getCode().equals("VALIDATION_SUCCESSFUL"));
            response.setFailureReason(authRequest.getFailureReason());
            response.setValidationTime(authRequest.getUpdateTime().toString());

            return Response.success(response);
        } catch (Exception e) {
            log.error("身份验证失败", e);
            return Response.fail(e.getMessage());
        }
    }
}
