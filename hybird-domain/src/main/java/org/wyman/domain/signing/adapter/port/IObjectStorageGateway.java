package org.wyman.domain.signing.adapter.port;

/**
 * 对象存储网关接口(用于存储CRL)
 */
public interface IObjectStorageGateway {
    /**
     * 上传CRL
     */
    String uploadCRL(String crlNumber, String crlPem);

    /**
     * 下载CRL
     */
    String downloadCRL(String crlUrl);

    /**
     * 删除CRL
     */
    void deleteCRL(String crlUrl);
}
