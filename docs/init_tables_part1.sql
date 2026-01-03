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
