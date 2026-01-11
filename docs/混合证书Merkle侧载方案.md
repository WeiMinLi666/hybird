### 混合证书 Merkle 承诺 + Sidecar 侧载方案（落地版，无模糊表述）

#### 1. 固定目标
- 生成一张混合证书，经典链签名保持不变；PQC 公钥、PQC KEM 公钥、备用签名值不再直接放入证书扩展，而是放入 sidecar。
- 证书内仅放两件事：**Merkle 根承诺**（对全部 PQC 数据的哈希承诺）和 **sidecar URL**。
- 验证方如需 PQC/备用能力，必须获取 sidecar，验证 Merkle 路径，再验 PQC/备用签名；无法获取 sidecar 时，PQC/备用验证直接失败（不得降级）。

#### 2. 固定算法与格式
- **哈希算法**：SHA-256（固定，不做协商）。
- **Merkle 构造**：二叉树，自底向上，用 SHA-256；偶数个叶子正常配对，奇数补自身副本。
- **叶子编码**：`leaf = SHA256(label || 0x00 || len(value 4-byte big-endian) || value)`。
- **根编码**：32 字节 SHA-256 值，写入扩展原始 OCTET STRING（不再含其他元数据）。
- **sidecar 格式**：JSON（UTF-8），再整体 Base64；字段固定如下：
  ```json
  {
    "version": 1,
    "hashAlg": "SHA256",
    "serialNumber": "<hex>",
    "merkleRoot": "<base64 32-byte>",
    "leaves": [
      {"label": "pqSigPub", "valueB64": "..."},
      {"label": "pqKekPub", "valueB64": "..."},
      {"label": "altSigValue", "valueB64": "..."},
      {"label": "altSigAlg", "valueB64": "..."}
    ],
    "proofs": [
      {"label": "pqSigPub", "pathB64": ["..."]},
      {"label": "pqKekPub", "pathB64": ["..."]},
      {"label": "altSigValue", "pathB64": ["..."]},
      {"label": "altSigAlg", "pathB64": ["..."]}
    ],
    "issuedAt": "<RFC3339 UTC>",
    "sidecarSignatureB64": "<Base64 of signature over canonical JSON>",
    "sidecarSigningCertPem": "<PEM of signer>"
  }
  ```
- **sidecar 签名算法**：默认 SHA256withRSA（使用 CA 的 OCSP/SCVP/sidecar 签发证书私钥）。

#### 3. 证书扩展（固定 OID 约定）
- `1.3.6.1.4.1.56546.500.1.10` (`extHybridMerkleRoot`): OCTET STRING，内容为 32 字节 merkleRoot。
- `1.3.6.1.4.1.56546.500.1.11` (`extHybridSidecarUrl`): IA5String，内容为 HTTPS URL，示例 `https://ca.example.com/sidecar/<serial>.json`。

#### 4. 签发流程（精确步骤）
1) **构建 TBS-Template（不含 altSignatureValue）**：
   - 包含常规字段、CRL DP、basicConstraints、keyUsage、备用签名算法扩展（已有）。
   - 新增扩展：`extHybridMerkleRoot` 先放占位 32 字节零值；`extHybridSidecarUrl` 放最终 sidecar URL。
2) **生成备用签名值**：用 PQC/备用私钥对 TBS-Template 的 DER 做签名，得到 `altSigValue`（字节）。
3) **准备叶子**：
   - L1: `label="pqSigPub"`, `value = pqSignaturePublicKeyPem.getBytes(UTF-8)`
   - L2: `label="pqKekPub"`, `value = pqKekPublicKeyPem.getBytes(UTF-8)`
   - L3: `label="altSigValue"`, `value = altSigValue`
   - L4: `label="altSigAlg"`, `value = altSignatureJcaName.getBytes(UTF-8)`
4) **计算 Merkle 根**：按第 2 节算法得到 32 字节 merkleRoot。
5) **重建最终 TBS**：用真实 merkleRoot 写入 `extHybridMerkleRoot`，保持同一 sidecar URL；再用经典私钥完成最终证书签名，产出证书。
6) **生成 sidecar**：
   - 填写 JSON 结构（第 2 节），对 canonical JSON（UTF-8，无多余空格，字段按上面顺序）做 SHA256withRSA 签名，放入 `sidecarSignatureB64`。
   - 生成 Merkle 认证路径：对每个叶子给出兄弟节点哈希列表，按自底向上顺序编码为 base64 字符串数组。
   - 发布到 `extHybridSidecarUrl` 指定的 HTTPS 位置。

#### 5. 验证流程（必须全部满足）
1) 常规链验证：验证证书链与经典签名。
2) 读取扩展：取得 `merkleRoot`（32 字节）和 `sidecarUrl`。
3) 获取 sidecar：用 HTTPS 下载；验证 `sidecarSignatureB64`，公钥为 `sidecarSigningCertPem`（该证书需链到受信 CA 或预配）。
4) Merkle 验证：
   - 对 sidecar 中每个叶子，按第 2 节叶子编码规则计算叶子哈希。
   - 使用 `proofs` 的 `pathB64` 逐级合并，必须得到与证书扩展中 `merkleRoot` 完全一致。
5) PQC/备用验证：
   - 从叶子提取 PQC 公钥、KEM 公钥、altSigValue、altSigAlg。
   - 重新生成 TBS-Template（不含 altSignatureValue，含 merkleRoot/sidecarUrl 扩展），用 PQC/备用公钥和 altSigAlg 验签 altSigValue。
   - 经典验签已在步骤 1 完成。
6) 若任一步失败，PQC/备用验证失败；客户端不得因 sidecar 不可得而静默降级。

#### 6. 与当前项目的改造清单
- **新增常量**：在 `HybridCertificateOids` 中添加 `EXT_HYBRID_MERKLE_ROOT = "1.3.6.1.4.1.56546.500.1.10"`，`EXT_HYBRID_SIDECAR_URL = "1.3.6.1.4.1.56546.500.1.11"`。
- **上下文字段**：`HybridCertificateRequestContext` 增加 `merkleRoot`、`sidecarUrl` 字段；签发前由应用层计算后传入生成器。
- **证书生成器**：
  - 在 `BouncyCastleCertificateGenerator` 中，第一次构建 TBS-Template（root=0），计算 altSigValue；
  - 由上层提供 merkleRoot/sidecarUrl；第二次构建最终证书时写入两个新扩展，完成经典签名。
- **sidecar 生成**：在触发层（Controller）或应用层增加生成 JSON + 签名的逻辑，落盘/上传到 HTTPS，可按 `https://ca.example.com/sidecar/<serial>.json` 命名。
- **响应增强**：接口可返回 sidecarUrl，方便客户端立即获取。
- **验证端（如 SDK）**：按第 5 节实现完整验证流程。

#### 7. 伪代码（精确字段名）
```java
// 1) 构建 TBS-Template，不含 altSignatureValue，extHybridMerkleRoot=32字节零
byte[] tbsTemplate = buildTbs(subject, issuer, extensionsWithZeroRootAndUrl);

// 2) 备用签名
byte[] altSigValue = signWithPqc(tbsTemplate, altPrivateKey, altSigAlgJca);

// 3) 叶子与 Merkle 根
List<Leaf> leaves = List.of(
  leaf("pqSigPub", pqSigPem.getBytes(UTF_8)),
  leaf("pqKekPub", pqKekPem.getBytes(UTF_8)),
  leaf("altSigValue", altSigValue),
  leaf("altSigAlg", altSigAlgJca.getBytes(UTF_8))
);
byte[] merkleRoot = merkle256(leaves);
String sidecarUrl = "https://ca.example.com/sidecar/" + serial + ".json";

// 4) 最终证书
X509Certificate cert = buildFinalAndSign(
  subject, issuer, pubKey, caPrivKey,
  extensionsWith(merkleRoot, sidecarUrl, altSigAlgExt, altSigValueExt?)
);

// 5) sidecar
Sidecar sc = new Sidecar();
sc.version = 1;
sc.hashAlg = "SHA256";
sc.serialNumber = cert.getSerialNumber().toString(16);
sc.merkleRoot = base64(merkleRoot);
sc.leaves = leavesToJson(leaves);
sc.proofs = proofsToJson(buildProofs(leaves));
sc.issuedAt = nowUtcRfc3339();
String canonical = canonicalJson(sc);
sc.sidecarSignatureB64 = base64(sign(canonical.getBytes(UTF_8), sidecarPrivKey, "SHA256withRSA"));
sc.sidecarSigningCertPem = sidecarSignerPem;
writeToUrl(sc, sidecarUrl);
```

#### 8. 对比价值（定性 + 可测）
- 证书体积：去掉大体积 PQC 扩展，体积接近经典证书；收益可通过样例对比（如 >3KB 降到 ~1.5KB，取决于原 PQC 公钥大小）。
- 兼容性：经典客户端无改动；PQC 客户端按需获取 sidecar。
- 安全性：在完整验证流程下，与内嵌扩展的绑定性等价；差异仅在 sidecar 可用性，已通过多源/缓存可提升。
