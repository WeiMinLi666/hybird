-- 清理RootCA记录,让应用重新创建
DELETE FROM certificate_authority WHERE ca_name = 'RootCA';
