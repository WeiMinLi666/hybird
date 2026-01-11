-- =============================================
-- 证书管理系统数据库表结构
-- =============================================
-- 证书表
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
  `post_quantum_public_key_pem` longtext DEFAULT NULL COMMENT '后量子公钥PEM（签名）',
  `post_quantum_kek_public_key_pem` longtext DEFAULT NULL COMMENT '后量子KEM公钥PEM（加密）',
  `revocation_reason` varchar(255) DEFAULT NULL COMMENT '吊销原因',
  `revoked_by` varchar(64) DEFAULT NULL COMMENT '吊销操作人',
  `revocation_comments` varchar(512) DEFAULT NULL COMMENT '吊销备注',
  `renewal_notice_days` int DEFAULT 30 COMMENT '续期通知天数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP '更新时间',
  PRIMARY KEY (`serial_number`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_not_after` (`not_after`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证书表';


