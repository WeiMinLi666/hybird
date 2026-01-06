package org.wyman.domain.authentication.adapter.port;

import org.wyman.domain.authentication.valobj.CertificateSigningRequest;

/**
 * CSR解析器接口
 */
public interface ICSRParser {
    /**
     * 解析CSR PEM格式数据
     */
    CertificateSigningRequest parsePEM(String pemData);

    /**
     * 解析CSR DER格式数据
     */
    CertificateSigningRequest parseDER(byte[] derData);

    /**
     * 验证CSR签名
     */
    boolean verifySignature(CertificateSigningRequest csr);
}
