package org.wyman.infrastructure.adapter.port;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.wyman.domain.signing.adapter.port.IObjectStorageGateway;
import org.wyman.infrastructure.config.MinioConfig;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * MinIO对象存储网关实现
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "object-storage.provider", havingValue = "minio", matchIfMissing = true)
public class MinioObjectStorageGateway implements IObjectStorageGateway {

    private final MinioClient minioClient;
    private final String bucketName;
    private final String crlPath;
    private final String certPath;

    public MinioObjectStorageGateway(MinioConfig config) {
        try {
            this.minioClient = MinioClient.builder()
                .endpoint(config.getEndpoint())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .build();
            this.bucketName = config.getBucketName();
            this.crlPath = config.getCrlPath();
            this.certPath = config.getCertPath();
            ensureBucketExists();
            log.info("MinIO对象存储网关初始化成功: endpoint={}, bucket={}", config.getEndpoint(), bucketName);
        } catch (Exception e) {
            throw new RuntimeException("初始化MinIO客户端失败", e);
        }
    }

    /**
     * 确保存储桶存在
     */
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());

            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
                log.info("创建MinIO存储桶: {}", bucketName);
            }
        } catch (Exception e) {
            throw new RuntimeException("检查或创建MinIO存储桶失败", e);
        }
    }

    @Override
    public String uploadCRL(String crlNumber, String crlPem) {
        try {
            String objectName = crlPath + "crl-" + crlNumber + ".crl";

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .contentType("application/pkix-crl")
                    .stream(
                        new ByteArrayInputStream(crlPem.getBytes(StandardCharsets.UTF_8)),
                        crlPem.getBytes(StandardCharsets.UTF_8).length,
                        -1
                    )
                    .build()
            );

            // 生成预签名URL
            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(7, TimeUnit.DAYS)
                    .build()
            );

            log.info("上传CRL到MinIO成功: objectName={}", objectName);
            return url;
        } catch (Exception e) {
            log.error("上传CRL到MinIO失败: crlNumber={}", crlNumber, e);
            throw new RuntimeException("上传CRL到MinIO失败", e);
        }
    }

    @Override
    public String downloadCRL(String crlUrl) {
        try {
            // 从URL中提取对象名称
            String objectName = extractObjectNameFromUrl(crlUrl);

            try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            )) {
                String content = IOUtils.toString(stream, StandardCharsets.UTF_8);
                log.info("从MinIO下载CRL成功: objectName={}", objectName);
                return content;
            }
        } catch (Exception e) {
            log.error("从MinIO下载CRL失败: crlUrl={}", crlUrl, e);
            throw new RuntimeException("从MinIO下载CRL失败", e);
        }
    }

    @Override
    public void deleteCRL(String crlUrl) {
        try {
            String objectName = extractObjectNameFromUrl(crlUrl);

            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );

            log.info("从MinIO删除CRL成功: objectName={}", objectName);
        } catch (Exception e) {
            log.error("从MinIO删除CRL失败: crlUrl={}", crlUrl, e);
            throw new RuntimeException("从MinIO删除CRL失败", e);
        }
    }

    /**
     * 从URL中提取对象名称
     */
    private String extractObjectNameFromUrl(String url) {
        try {
            // 简单实现:提取最后一个/之后的内容
            int lastSlashIndex = url.lastIndexOf('/');
            if (lastSlashIndex == -1) {
                throw new IllegalArgumentException("无效的URL格式: " + url);
            }
            return url.substring(lastSlashIndex + 1);
        } catch (Exception e) {
            log.error("从URL提取对象名称失败: url={}", url, e);
            throw new RuntimeException("从URL提取对象名称失败", e);
        }
    }
}
