package org.wyman.infrastructure.adapter.port;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.wyman.domain.signing.adapter.port.IObjectStorageGateway;

/**
 * 模拟对象存储网关实现
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "object-storage.provider", havingValue = "mock")
public class MockObjectStorageGateway implements IObjectStorageGateway {

    @Override
    public String uploadCRL(String crlNumber, String crlPem) {
        // 简化实现:返回模拟的URL
        String url = "http://mock-storage.example.com/crl-" + crlNumber + ".crl";
        log.info("上传CRL到: {}", url);
        return url;
    }

    @Override
    public String downloadCRL(String crlUrl) {
        // 简化实现:返回模拟的CRL内容
        log.info("下载CRL从: {}", crlUrl);
        return "-----BEGIN X509 CRL-----\nMOCK_CRL_DATA\n-----END X509 CRL-----";
    }

    @Override
    public void deleteCRL(String crlUrl) {
        // 简化实现:打印删除日志
        log.info("删除CRL: {}", crlUrl);
    }
}
