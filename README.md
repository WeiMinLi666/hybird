# 混合证书管理系统 (Hybrid Certificate Management System)

基于DDD框架和Spring Boot 3.x实现的后量子密码学混合证书管理系统。

## 技术栈

- JDK 17
- Spring Boot 3.4.3
- Bouncy Castle 1.78.1
- MySQL 8.0
- MyBatis
- Lombok

## 领域模型

### 1. 身份验证域 (Authentication Domain)
**职责**: 验证CSR的完整性、申请者身份真实性及请求参数合规性

**核心组件**:
- `AuthenticationRequest` (聚合根): 身份验证请求
- `Applicant` (实体): 申请者信息
- `CertificateSigningRequest` (值对象): CSR数据
- `AuthenticationService`: 身份验证领域服务

**端口接口**:
- `ICSRParser`: CSR解析器
- `IIdentityProvider`: 身份提供者
- `IAuthenticationRequestRepository`: 仓储接口

### 2. 证书策略域 (Policy Domain)
**职责**: 定义和执行证书签发策略,确保后量子迁移合规性

**核心组件**:
- `CertificatePolicy` (聚合根): 证书策略
- `CryptographicRule` (值对象): 密码学规则
- `ValidityPeriodRule` (值对象): 有效期规则
- `SubjectDNRule` (值对象): 主题DN规则
- `PolicyService`: 策略领域服务

**端口接口**:
- `ICertificatePolicyRepository`: 仓储接口

### 3. 签名域 (Signing Domain)
**职责**: 执行数字签名操作,签发证书和CRL

**核心组件**:
- `CertificateAuthority` (聚合根): 证书颁发机构
- `Certificate` (值对象): 证书数据
- `CRL` (值对象): 证书吊销列表
- `SigningService`: 签名领域服务

**端口接口**:
- `IPrivateKeyProvider`: 私钥提供者
- `ICertificateAuthorityRepository`: 仓储接口
- `IObjectStorageGateway`: 对象存储网关

### 4. 证书生命周期管理域 (Lifecycle Domain)
**职责**: 管理证书签发后的动态状态

**核心组件**:
- `Certificate` (聚合根): 证书
- `RevocationInfo` (值对象): 吊销信息
- `NotificationPolicy` (值对象): 通知策略
- `CertificateLifecycleService`: 生命周期领域服务

**端口接口**:
- `ICertificateRepository`: 仓储接口

### 5. 在线证书状态查询域 (Status Domain)
**职责**: 提供高性能的证书吊销状态查询

**核心组件**:
- `RevocationStatusCache` (聚合根): 吊销状态缓存
- `CRLMetadata` (值对象): CRL元数据
- `RevocationStatusService`: 状态查询领域服务

**端口接口**:
- `IRevocationStatusCacheRepository`: 仓储接口

### 6. 日志域 (Audit Domain)
**职责**: 记录系统关键操作日志

**核心组件**:
- `AuditEvent` (聚合根): 审计事件
- `TimestampValueObject` (值对象): 时间戳
- `AuditService`: 审计领域服务

**端口接口**:
- `IAuditEventRepository`: 仓储接口

## API接口

### 身份验证
- `POST /api/authentication/validate` - 处理身份验证请求

### 证书管理
- `POST /api/certificate/issue` - 签发证书
- `POST /api/certificate/revoke` - 吊销证书
- `POST /api/certificate/status` - 查询证书状态
- `POST /api/certificate/crl/generate` - 生成CRL

## 定时任务

- **每日01:00** - 检查过期证书
- **每日08:00** - 扫描临期证书并发送通知
- **每日02:00** - 更新CRL

## 配置说明

### 数据库配置
当前配置为本地MySQL连接:
```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/xfg_frame_archetype
    username: root
    password: 123456
```

### Redis配置 (TODO)
Redis配置已添加注释,需要时取消注释并填写实际连接信息:
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
```

## 模块说明

- `hybird-api`: API层,包含DTO和响应对象
- `hybird-app`: 应用层,Spring Boot启动入口
- `hybird-domain`: 领域层,包含所有领域模型和服务
- `hybird-infrastructure`: 基础设施层,包含仓储和外部接口实现
- `hybird-trigger`: 触发层,包含HTTP控制器和定时任务
- `hybird-types`: 类型层,包含枚举、异常和领域事件

## 启动说明

```bash
# 编译项目
mvn clean install

# 启动应用
cd hybird-app
java -jar target/hybird-app.jar
```

默认端口: 8091

## 注意事项

1. 当前使用内存仓储实现,生产环境需要替换为数据库实现
2. 私钥提供者使用模拟实现,生产环境需要集成HSM
3. 对象存储使用模拟实现,生产环境需要对接云存储
4. Redis缓存配置已标记为TODO,需要时启用
