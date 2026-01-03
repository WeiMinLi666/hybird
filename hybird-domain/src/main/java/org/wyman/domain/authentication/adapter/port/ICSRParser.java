package org.wyman.domain.authentication.adapter.port;

/**
 * CSR解析器接口
 */
public interface ICSRParser {
    /**
     * 解析CSR PEM格式数据
     */
    org.wyman.domain.authentication.valobj.CertificateSigningRequest parsePEM(String pemData);

    /**
     * 解析CSR DER格式数据
     */
    org.wyman.domain.authentication.valobj.CertificateSigningRequest parseDER(byte[] derData);

    /**
     * 验证CSR签名
     */
    boolean verifySignature(org.wyman.domain.authentication.valobj.CertificateSigningRequest csr);
}
