-- =============================================
-- 证书管理系统数据库完整表结构
-- =============================================
-- 初始化数据库
CREATE DATABASE IF NOT EXISTS `hybird` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `hybird`;

-- =============================================
-- 证书表
-- =============================================
CREATE TABLE IF NOT EXISTS `certificate` (
  `serial_number` varchar(64) NOT NULL COMMENT '证书序列号',
  `certificate_type` varchar(32) NOT NULL COMMENT '证书类型',
  `subject_dn` varchar(512) NOT NULL COMMENT '主题DN',
  `issuer_dn` varchar(512) NOT NULL COMMENT '颁发者DN',
  `status` varchar(32) NOT NULL COMMENT '状态',
  `not_before` datetime NOT NULL COMMENT '有效期开始',
  `not_after` datetime NOT NULL COMMENT '有效期结束',
  `applicant_id` varchar(64) NOT NULL COMMENT '申请者ID',
  `issuance_request_id` varchar(64) DEFAULT NULL COMMENT '签发请求ID',
  `pem_encoded` longtext COMMENT '证书PEM编码',
  `post_quantum_csr_pem` longtext DEFAULT NULL COMMENT '后量子CSR（PEM）',
  `post_quantum_public_key_pem` longtext DEFAULT NULL COMMENT '后量子公钥PEM',
  `revocation_reason` varchar(255) DEFAULT NULL COMMENT '吊销原因',
  `revoked_by` varchar(64) DEFAULT NULL COMMENT '吊销操作人',
  `revocation_comments` varchar(512) DEFAULT NULL COMMENT '吊销备注',
  `renewal_notice_days` int DEFAULT 30 COMMENT '续期通知天数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`serial_number`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_not_after` (`not_after`),
  KEY `idx_issuer_dn` (`issuer_dn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证书表';

-- =============================================
-- 证书颁发机构表
-- =============================================
CREATE TABLE IF NOT EXISTS `certificate_authority` (
  `ca_id` varchar(64) NOT NULL COMMENT 'CA ID',
  `ca_name` varchar(128) NOT NULL COMMENT 'CA名称',
  `ca_dn` varchar(512) NOT NULL COMMENT 'CA DN',
  `certificate_pem` longtext COMMENT 'CA证书PEM',
  `private_key_alias` varchar(128) DEFAULT NULL COMMENT '私钥别名',
  `key_type` varchar(32) DEFAULT 'RSA' COMMENT '密钥类型',
  `key_size` int DEFAULT 2048 COMMENT '密钥大小',
  `enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`ca_id`),
  UNIQUE KEY `uk_ca_name` (`ca_name`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证书颁发机构表';

-- =============================================
-- 证书策略表
-- =============================================
CREATE TABLE IF NOT EXISTS `certificate_policy` (
  `policy_id` varchar(64) NOT NULL COMMENT '策略ID',
  `certificate_type` varchar(32) NOT NULL COMMENT '证书类型',
  `policy_name` varchar(128) NOT NULL COMMENT '策略名称',
  `signature_algorithms` varchar(512) DEFAULT NULL COMMENT '允许的签名算法,逗号分隔',
  `kem_algorithms` varchar(512) DEFAULT NULL COMMENT '允许的KEM算法,逗号分隔',
  `require_hybrid_signature` tinyint(1) DEFAULT 0 COMMENT '是否需要混合签名',
  `min_validity_days` int DEFAULT 1 COMMENT '最小有效期(天)',
  `max_validity_days` int DEFAULT 365 COMMENT '最大有效期(天)',
  `subject_dn_pattern` varchar(512) DEFAULT NULL COMMENT '主题DN正则表达式',
  `subject_dn_required_fields` varchar(256) DEFAULT NULL COMMENT '主题DN必填字段,逗号分隔',
  `enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `version` varchar(16) DEFAULT '1.0' COMMENT '策略版本',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`policy_id`),
  KEY `idx_certificate_type` (`certificate_type`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证书策略表';
-- =============================================
-- 身份验证请求表
-- =============================================
CREATE TABLE IF NOT EXISTS `authentication_request` (
  `request_id` varchar(64) NOT NULL COMMENT '请求ID',
  `applicant_id` varchar(64) NOT NULL COMMENT '申请者ID',
  `applicant_name` varchar(128) NOT NULL COMMENT '申请者名称',
  `applicant_email` varchar(256) DEFAULT NULL COMMENT '申请者邮箱',
  `csr_content` longtext COMMENT 'CSR内容',
  `csr_subject_dn` varchar(512) DEFAULT NULL COMMENT 'CSR主题DN',
  `public_key_algorithm` varchar(32) DEFAULT NULL COMMENT '公钥算法',
  `status` varchar(32) NOT NULL COMMENT '状态',
  `failure_reason` varchar(512) DEFAULT NULL COMMENT '验证失败原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`request_id`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='身份验证请求表';

-- =============================================
-- 审计日志表
-- =============================================
CREATE TABLE IF NOT EXISTS `audit_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `event_type` varchar(64) NOT NULL COMMENT '事件类型',
  `event_data` longtext COMMENT '事件数据(JSON)',
  `operator` varchar(128) DEFAULT NULL COMMENT '操作人',
  `ip_address` varchar(64) DEFAULT NULL COMMENT 'IP地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_event_type` (`event_type`),
  KEY `idx_operator` (`operator`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- =============================================
-- 证书链表
-- =============================================
CREATE TABLE IF NOT EXISTS `certificate_chain` (
  `chain_id` bigint NOT NULL AUTO_INCREMENT COMMENT '链ID',
  `certificate_serial_number` varchar(64) NOT NULL COMMENT '证书序列号',
  `parent_serial_number` varchar(64) DEFAULT NULL COMMENT '父证书序列号',
  `level` int NOT NULL DEFAULT 0 COMMENT '层级(0为叶子证书)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`chain_id`),
  KEY `idx_certificate_serial` (`certificate_serial_number`),
  KEY `idx_parent_serial` (`parent_serial_number`),
  KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证书链表';

-- =============================================
-- 吊销状态缓存表
-- =============================================
CREATE TABLE IF NOT EXISTS `revocation_status_cache` (
  `cache_id` varchar(64) NOT NULL COMMENT '缓存ID',
  `cache_data` longtext NOT NULL COMMENT '缓存数据(JSON)',
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号',
  `is_latest` tinyint(1) DEFAULT 1 COMMENT '是否最新',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`cache_id`),
  KEY `idx_is_latest` (`is_latest`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='吊销状态缓存表';

-- =============================================
-- 插入初始数据
-- =============================================

-- 插入默认CA
INSERT INTO `certificate_authority` (`ca_id`, `ca_name`, `ca_dn`, `enabled`, `key_type`, `key_size`) VALUES
('CA001', 'RootCA', 'CN=RootCA,O=Hybird,C=CN', 1, 'RSA', 2048)
ON DUPLICATE KEY UPDATE `ca_name`=VALUES(`ca_name`);

-- 插入默认证书策略
INSERT INTO `certificate_policy` (`policy_id`, `certificate_type`, `policy_name`, `signature_algorithms`, `require_hybrid_signature`, `min_validity_days`, `max_validity_days`, `enabled`) VALUES
('POLICY001', 'DEVICE_CERT', '设备证书策略', 'SM2,RSA-SHA256,ECDSA-SHA256', 0, 30, 365, 1),
('POLICY002', 'PLATFORM_CERT', '平台证书策略', 'SM2,RSA-SHA256,ECDSA-SHA256', 0, 90, 730, 1)
ON DUPLICATE KEY UPDATE `policy_name`=VALUES(`policy_name`);
