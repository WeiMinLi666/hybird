#!/bin/bash

BASE_URL="http://localhost:8091/api/certificate"

echo "========================================="
echo "  证书管理系统API测试(最终版)"
echo "========================================="
echo ""

# 检查应用
echo "1. 检查应用状态..."
if curl -s --connect-timeout 3 "http://localhost:8091" > /dev/null 2>&1; then
    echo "✓ 应用运行正常"
else
    echo "❌ 应用未运行"
    exit 1
fi
echo ""

# 查询CA列表
echo "2. 查询CA列表..."
CA_LIST=$(curl -s "http://localhost:8091/api/ca/list")
echo "$CA_LIST"
echo ""

# 生成测试CSR
echo "3. 生成测试CSR..."
openssl ecparam -name prime256v1 -genkey -out /tmp/test-final-key.pem 2>/dev/null
openssl req -new -key /tmp/test-final-key.pem -out /tmp/test-final.csr -subj "/CN=device-final-001/O=TestOrganization/C=CN" 2>/dev/null
CSR=$(cat /tmp/test-final.csr)
echo "✓ CSR已生成"
echo ""

# 测试申请证书(会有身份验证失败,但可以验证CSR解析)
echo "4. 申请证书(验证CSR解析)..."
APPLY_RESPONSE=$(curl -s -X POST "${BASE_URL}/apply" \
  -H "Content-Type: application/json" \
  -d "{
    \"applicantId\": \"test-user\",
    \"applicantName\": \"Test\",
    \"applicantEmail\": \"test@test.com\",
    \"idToken\": \"token\",
    \"csrPemData\": $(echo "$CSR" | sed 's/"/\\"/g' | sed ':a;N;$!ba;s/\n/\\n/g'),
    \"certificateType\": \"DEVICE\",
    \"notAfter\": \"2026-12-31T23:59:59\",
    \"caName\": \"RootCA\"
  }")

echo "响应: $APPLY_RESPONSE"
echo ""

# 测试查询CA详情
echo "5. 查询CA详情..."
CA_DETAIL=$(curl -s "http://localhost:8091/api/ca/detail?caId=CA001")
echo "响应: $CA_DETAIL"
echo ""

# 测试查询不存在的证书
echo "6. 查询不存在的证书..."
QUERY_NONEXIST=$(curl -s "${BASE_URL}/query?serialNumber=non-existent-cert-12345")
echo "响应: $QUERY_NONEXIST"
echo ""

# 测试生成CRL
echo "7. 生成CRL..."
CRL_RESPONSE=$(curl -s -X POST "${BASE_URL}/crl/generate" \
  -H "Content-Type: application/json" \
  -d '{
    "caName": "RootCA",
    "signatureAlgorithm": "SM2"
  }')
echo "响应: $CRL_RESPONSE"
echo ""

echo "========================================="
echo "  测试完成"
echo "========================================="
