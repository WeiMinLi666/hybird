# 证书管理系统 API 接口文档

## 接口列表

### 1. 证书管理

#### 1.1 申请证书
- **接口**: `POST /api/certificate/apply`
- **描述**: 申请数字证书
- **请求体** (CertificateApplyRequest):
```json
{
  "applicantId": "string",      // 申请者ID
  "applicantName": "string",    // 申请者名称
  "applicantEmail": "string",    // 申请者邮箱
  "idToken": "string",          // 身份验证令牌
  "csrPemData": "string",       // CSR PEM数据
  "certificateType": "string",   // 证书类型: DEVICE_CERT, PLATFORM_CERT, CA_CERT, INTERMEDIATE_CERT
  "notAfter": "2025-01-01T00:00:00",  // 有效期结束时间
  "caName": "string"            // CA名称
}
```
- **响应** (CertificateIssuanceResponse):
```json
{
  "serialNumber": "string",      // 证书序列号
  "certificatePem": "string",    // 证书PEM编码
  "subjectDN": "string",        // 主题DN
  "issuerDN": "string",         // 颁发者DN
  "notBefore": "string",        // 有效期开始
  "notAfter": "string",         // 有效期结束
  "signatureAlgorithm": "string", // 签名算法
  "crlDistributionPoint": "string" // CRL分发点
}
```

#### 1.2 申请混合证书
- **接口**: `POST /api/certificate/apply/hybrid`
- **描述**: 申请混合签名证书(传统算法+后量子算法)
- **请求体** (HybridCertificateApplyRequest):
```json
{
  "applicantId": "string",
  "applicantName": "string",
  "applicantEmail": "string",
  "idToken": "string",
  "classicalCsrPemData": "string",    // 传统算法CSR PEM数据
  "postQuantumCsrPemData": "string",  // 后量子算法CSR PEM数据
  "signatureAlgorithm": "string",      // 签名算法
  "kemAlgorithm": "string",            // 密钥封装算法
  "notAfter": "2025-01-01T00:00:00",
  "caName": "string"
}
```
- **响应**: 同申请证书

#### 1.3 查询证书
- **接口**: `GET /api/certificate/query?serialNumber={serialNumber}`
- **描述**: 根据序列号查询证书信息
- **响应** (CertificateQueryResponse):
```json
{
  "serialNumber": "string",
  "certificateType": "string",
  "status": "string",              // 证书状态
  "subjectDN": "string",
  "issuerDN": "string",
  "notBefore": "string",
  "notAfter": "string",
  "applicantId": "string",
  "certificatePem": "string"
}
```

#### 1.4 查询证书链
- **接口**: `GET /api/certificate/chain?serialNumber={serialNumber}`
- **描述**: 查询证书的完整证书链
- **响应** (CertificateChainResponse):
```json
{
  "serialNumber": "string",
  "chain": [
    {
      "serialNumber": "string",
      "subjectDN": "string",
      "issuerDN": "string",
      "certificatePem": "string",
      "level": 0
    }
  ]
}
```

#### 1.5 查询设备证书
- **接口**: `GET /api/certificate/device?applicantId={applicantId}`
- **描述**: 查询申请者的所有设备证书
- **响应**: CertificateQueryResponse 数组

#### 1.6 吊销证书
- **接口**: `POST /api/certificate/revoke`
- **描述**: 吊销指定证书
- **请求体** (CertificateRevocationRequest):
```json
{
  "serialNumber": "string",    // 证书序列号
  "reasonCode": 0,            // 吊销原因代码: 0=UNSPECIFIED, 1=KEY_COMPROMISE, 2=CA_COMPROMISE, 3=AFFILIATION_CHANGED, 4=SUPERCEDED, 5=CESSATION_OF_OPERATION, 6=CERTIFICATE_HOLD
  "operator": "string",       // 操作人
  "comments": "string"         // 备注信息
}
```
- **响应**: 无数据，仅返回成功/失败状态

#### 1.7 续期证书
- **接口**: `POST /api/certificate/renew`
- **描述**: 续期即将过期的证书
- **请求体** (CertificateRenewalRequest):
```json
{
  "oldSerialNumber": "string",  // 原证书序列号
  "csrPemData": "string",       // 新CSR PEM数据
  "notAfter": "2025-01-01T00:00:00"  // 新证书有效期结束时间
}
```
- **响应**: 同申请证书

### 2. 签发管理

#### 2.1 签发证书
- **接口**: `POST /api/certificate/issue`
- **描述**: 直接签发证书(内部接口)
- **请求体** (CertificateIssuanceRequest):
```json
{
  "caName": "string",
  "applicantId": "string",
  "subjectDN": "string",
  "notBefore": "2025-01-01T00:00:00",
  "notAfter": "2025-12-31T23:59:59",
  "signatureAlgorithm": "string",
  "kemAlgorithm": "string"
}
```
- **响应**: 同申请证书

### 3. 状态查询

#### 3.1 查询证书吊销状态
- **接口**: `POST /api/certificate/status`
- **描述**: 查询证书吊销状态(支持单个和批量查询)
- **请求体** (CertificateStatusCheckRequest):
```json
{
  "queryType": "single",  // single 或 batch
  "serialNumber": "string",
  "serialNumbers": ["string1", "string2"]
}
```
- **响应** (CertificateStatusCheckResponse):
```json
{
  "serialNumber": "string",
  "revoked": true,
  "revocationDate": "2025-01-01T00:00:00",
  "revocationReason": "string",
  "batchResults": {
    "serial1": true,
    "serial2": false
  }
}
```

#### 3.2 生成CRL
- **接口**: `POST /api/certificate/crl/generate`
- **描述**: 生成证书吊销列表(CRL)
- **请求体** (CRLGenerateRequest):
```json
{
  "caName": "string",
  "signatureAlgorithm": "string"
}
```
- **响应** (CRLGenerateResponse):
```json
{
  "crlNumber": 1,
  "crlPem": "string",
  "crlUrl": "http://crl.example.com/crl-1.crl",
  "thisUpdate": "2025-01-01T00:00:00",
  "nextUpdate": "2025-01-02T00:00:00",
  "revokedCount": 5
}
```

### 4. 身份验证

#### 4.1 验证身份
- **接口**: `POST /api/authentication/validate`
- **描述**: 验证申请者身份
- **请求体**:
```json
{
  "applicantId": "string",
  "idToken": "string",
  "csrPemData": "string"
}
```
- **响应** (AuthenticationResponse):
```json
{
  "requestId": "string",
  "status": "string",
  "message": "string"
}
```

## 统一响应格式

所有接口统一返回以下格式：

```json
{
  "code": "0000",      // "0000" 成功, "9999" 失败
  "info": "string",    // 响应信息
  "data": {}           // 响应数据
}
```

## 数据库连接配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://49.233.215.82:3306/hybird
    username: admin
    password: wyman1112
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## Redis连接配置

```yaml
spring:
  data:
    redis:
      host: 49.233.215.82
      port: 6379
      password: wyman1112
      database: 0
```

## 已实现的数据库表

1. certificate - 证书表
2. certificate_authority - 证书颁发机构表
3. certificate_policy - 证书策略表
4. authentication_request - 身份验证请求表
5. audit_log - 审计日志表
6. certificate_chain - 证书链表
7. revocation_status_cache - 吊销状态缓存表
