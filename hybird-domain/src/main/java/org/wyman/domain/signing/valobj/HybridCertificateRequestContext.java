package org.wyman.domain.signing.valobj;

import lombok.Builder;
import lombok.Data;

import java.security.PrivateKey;

/**
 * 混合证书扩展上下文，用于在签发阶段向证书生成器传递PQC相关信息。
 */
@Data
@Builder
public class HybridCertificateRequestContext {

    /** 是否启用混合扩展 */
    @Builder.Default
    private boolean hybridEnabled = false;

    /** PQC签名公钥（PEM或Base64表示） */
    private String pqSignaturePublicKeyPem;

    /** PQC KEM公钥（PEM或Base64表示） */
    private String pqKekPublicKeyPem;

    /** 申请者提供的PQC签名值（PoP） */
    private byte[] applicantPqSignatureValue;

    /** 申请者提供的KEM密钥持有证明 */
    private byte[] applicantKekProof;

    /** Catalyst模型备用签名算法OID（字符串表示） */
    private String altSignatureAlgorithmOid;

    /** JCA名称，用于计算备用签名值（如 SM3withSM2、SHA256withRSA） */
    private String altSignatureJcaName;

    /** 是否需要生成备用签名值 */
    @Builder.Default
    private boolean altSignatureRequired = false;

    /** Merkle 根承诺（32字节 SHA-256） */
    private byte[] merkleRoot;

    /** Sidecar URL（HTTPS/OCSP/SCVP） */
    private String sidecarUrl;

    /** CA 侧用于 PQ/备用签名的私钥（可由上层设置，避免再次获取） */
    private transient PrivateKey altSignaturePrivateKey;
}
