package org.wyman.infrastructure.adapter.repository;

import org.springframework.stereotype.Repository;
import org.wyman.domain.authentication.adapter.port.IAuthenticationRequestRepository;
import org.wyman.domain.authentication.model.aggregate.AuthenticationRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 身份验证请求仓储内存实现
 */
@Repository
public class InMemoryAuthenticationRequestRepository implements IAuthenticationRequestRepository {

    private final Map<String, AuthenticationRequest> storage = new HashMap<>();

    @Override
    public void save(AuthenticationRequest request) {
        storage.put(request.getRequestId(), request);
    }

    @Override
    public AuthenticationRequest findById(String requestId) {
        return storage.get(requestId);
    }

    @Override
    public List<AuthenticationRequest> findByApplicantId(String applicantId) {
        return storage.values().stream()
            .filter(req -> req.getApplicant() != null &&
                         applicantId.equals(req.getApplicant().getApplicantId()))
            .collect(Collectors.toList());
    }
}
