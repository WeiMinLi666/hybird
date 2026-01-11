#!/bin/bash

# 证书签发和查询API测试脚本(简化版)

BASE_URL="http://localhost:8091/api/certificate"

echo "========================================="
echo "  证书管理系统API测试"
echo "========================================="
echo ""

# 检查应用是否运行
echo "检查应用是否运行..."
if ! curl -s --connect-timeout 3 "http://localhost:8091" > /dev/null 2>&1; then
    echo "❌ 应用未运行,请先启动应用"
    exit 1
fi
echo "✓ 应用运行正常"
echo ""

# 生成测试CSR(使用OpenSSL)
echo "生成测试密钥对和CSR..."
OPENSSL_TEMP=$(mktemp -d)
cd "$OPENSSL_TEMP"

# 生成密钥对
openssl ecparam -name prime256v1 -genkey -out test_device.key 2>/dev/null

# 生成CSR
openssl req -new -key test_device.key -out test_device.csr \
  -subj "/CN=device-test-001/O=TestOrganization/OU=Device/C=CN" 2>/dev/null

# 读取CSR内容
CSR_DATA=$(cat test_device.csr)
cd - > /dev/null
rm -rf "$OPENSSL_TEMP"

echo "✓ CSR已生成"
echo ""

# 测试1: 签发证书(直接方式)
echo "========================================="
echo "测试1: 直接签发证书"
echo "========================================="
echo ""

ISSUE_RESPONSE=$(curl -s -X POST "${BASE_URL}/issue" \
  -H "Content-Type: application/json" \
  -d "{
    \"csrPemData\": $(echo "$CSR_DATA" | jq -Rs .),
    \"notBefore\": \"2025-01-01T00:00:00\",
    \"notAfter\": \"2026-12-31T23:59:59\",
    \"signatureAlgorithm\": \"SHA256withECDSA\",
    \"caName\": \"RootCA\"
  }")

echo "响应:"
echo "$ISSUE_RESPONSE" | jq '.'

# 检查是否成功
SUCCESS=$(echo "$ISSUE_RESPONSE" | jq -r '.code')
if [ "$SUCCESS" = "0000" ]; then
    echo ""
    echo "✓ 证书签发成功!"
    SERIAL_NUMBER=$(echo "$ISSUE_RESPONSE" | jq -r '.data.serialNumber')
    echo "证书序列号: $SERIAL_NUMBER"
else
    echo ""
    echo "❌ 证书签发失败"
    SERIAL_NUMBER=""
fi
echo ""

# 测试2: 查询证书(如果有成功签发的证书)
if [ -n "$SERIAL_NUMBER" ] && [ "$SERIAL_NUMBER" != "null" ]; then
    echo "========================================="
    echo "测试2: 查询证书"
    echo "========================================="
    echo ""

    QUERY_RESPONSE=$(curl -s "${BASE_URL}/query?serialNumber=$SERIAL_NUMBER")

    echo "响应:"
    echo "$QUERY_RESPONSE" | jq '.'

    QUERY_SUCCESS=$(echo "$QUERY_RESPONSE" | jq -r '.code')
    if [ "$QUERY_SUCCESS" = "0000" ]; then
        echo ""
        echo "✓ 证书查询成功!"
    else
        echo ""
        echo "❌ 证书查询失败"
    fi
    echo ""

    # 测试3: 查询证书链
    echo "========================================="
    echo "测试3: 查询证书链"
    echo "========================================="
    echo ""

    CHAIN_RESPONSE=$(curl -s "${BASE_URL}/chain?serialNumber=$SERIAL_NUMBER")

    echo "响应:"
    echo "$CHAIN_RESPONSE" | jq '.'

    CHAIN_SUCCESS=$(echo "$CHAIN_RESPONSE" | jq -r '.code')
    if [ "$CHAIN_SUCCESS" = "0000" ]; then
        CHAIN_COUNT=$(echo "$CHAIN_RESPONSE" | jq -r '.data.chain | length')
        echo ""
        echo "✓ 证书链查询成功! 链长度: $CHAIN_COUNT"
    else
        echo ""
        echo "❌ 证书链查询失败"
    fi
    echo ""

    # 测试4: 查询证书状态
    echo "========================================="
    echo "测试4: 查询证书状态"
    echo "========================================="
    echo ""

    STATUS_RESPONSE=$(curl -s -X POST "${BASE_URL}/status" \
      -H "Content-Type: application/json" \
      -d "{
        \"serialNumber\": \"$SERIAL_NUMBER\",
        \"queryType\": \"single\"
      }")

    echo "响应:"
    echo "$STATUS_RESPONSE" | jq '.'

    STATUS_SUCCESS=$(echo "$STATUS_RESPONSE" | jq -r '.code')
    if [ "$STATUS_SUCCESS" = "0000" ]; then
        echo ""
        echo "✓ 证书状态查询成功!"
    else
        echo ""
        echo "❌ 证书状态查询失败"
    fi
    echo ""
fi

# 测试5: 生成CRL
echo "========================================="
echo "测试5: 生成CRL"
echo "========================================="
echo ""

CRL_RESPONSE=$(curl -s -X POST "${BASE_URL}/crl/generate" \
  -H "Content-Type: application/json" \
  -d "{
    \"caName\": \"RootCA\",
    \"signatureAlgorithm\": \"SHA256withECDSA\"
  }")

echo "响应:"
echo "$CRL_RESPONSE" | jq '.'

CRL_SUCCESS=$(echo "$CRL_RESPONSE" | jq -r '.code')
if [ "$CRL_SUCCESS" = "0000" ]; then
    echo ""
    echo "✓ CRL生成成功!"
    CRL_NUMBER=$(echo "$CRL_RESPONSE" | jq -r '.data.crlNumber')
    REVOKED_COUNT=$(echo "$CRL_RESPONSE" | jq -r '.data.revokedCount')
    echo "CRL编号: $CRL_NUMBER"
    echo "吊销证书数: $REVOKED_COUNT"
else
    echo ""
    echo "❌ CRL生成失败"
fi
echo ""

# 测试6: 查询不存在的证书
echo "========================================="
echo "测试6: 查询不存在的证书"
echo "========================================="
echo ""

NOT_FOUND_RESPONSE=$(curl -s "${BASE_URL}/query?serialNumber=non-existent-cert")

echo "响应:"
echo "$NOT_FOUND_RESPONSE" | jq '.'

NOT_FOUND_CODE=$(echo "$NOT_FOUND_RESPONSE" | jq -r '.code')
if [ "$NOT_FOUND_CODE" = "9999" ]; then
    echo ""
    echo "✓ 正确处理了不存在的证书查询!"
else
    echo ""
    echo "⚠ 查询行为可能不符合预期"
fi
echo ""

echo "========================================="
echo "  测试完成"
echo "========================================="
