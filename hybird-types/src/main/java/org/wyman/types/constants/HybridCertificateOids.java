package org.wyman.types.constants;

/**
 * 自定义混合证书相关OID常量定义。
 * 该私有OID空间以 1.3.6.1.4.1.56546.500 为根，用于携带PQC扩展和属性。
 */
public final class HybridCertificateOids {

    private static final String BASE = "1.3.6.1.4.1.56546.500";

    /** 扩展：PQC签名公钥信息（SubjectPublicKeyInfo结构或PEM文本） */
    public static final String EXT_PQC_SIGNATURE_PUBLIC_KEY_INFO = BASE + ".1.1";

    /** 扩展：PQC KEM分发公钥信息 */
    public static final String EXT_PQC_KEK_DISTRIBUTION_KEY_INFO = BASE + ".1.2";

    /** 扩展：Catalyst模型的备用签名算法描述 */
    public static final String EXT_ALT_SIGNATURE_ALGORITHM = BASE + ".1.3";

    /** 扩展：Catalyst模型的备用签名值 */
    public static final String EXT_ALT_SIGNATURE_VALUE = BASE + ".1.4";

    /** 扩展：Merkle 根承诺（混合材料哈希承诺） */
    public static final String EXT_HYBRID_MERKLE_ROOT = BASE + ".1.10";

    /** 扩展：Sidecar 分发 URL（HTTPS/OCSP/SCVP） */
    public static final String EXT_HYBRID_SIDECAR_URL = BASE + ".1.11";

    /** CSR属性：PQC签名公钥信息 */
    public static final String ATTR_PQC_SIGNATURE_PUBLIC_KEY_INFO = BASE + ".2.1";

    /** CSR属性：PQC签名值（申请者PoP） */
    public static final String ATTR_PQC_SIGNATURE_VALUE = BASE + ".2.2";

    /** CSR属性：PQC KEM 公钥信息 */
    public static final String ATTR_PQC_KEK_PUBLIC_KEY_INFO = BASE + ".2.3";

    /** CSR属性：PQC KEM PoP 数据 */
    public static final String ATTR_PQC_KEK_POP_PROOF = BASE + ".2.4";

    private HybridCertificateOids() {
    }
}
