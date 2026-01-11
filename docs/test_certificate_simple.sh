#!/bin/bash

# 证书签发和查询API测试脚本(不依赖jq)

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

# 生成测试CSR
echo "生成测试密钥对和CSR..."
OPENSSL_TEMP=$(mktemp -d)
cd "$OPENSSL_TEMP"
openssl ecparam -name prime256v1 -genkey -out test_device.key 2>/dev/null
openssl req -new -key test_device.key -out test_device.csr \
  -subj "/CN=device-test-001/O=TestOrganization/OU=Device/C=CN" 2>/dev/null
CSR_DATA=$(cat test_device.csr | sed 's/"/\\"/g' | tr '\n' '\\n')
cd - > /dev/null
rm -rf "$OPENSSL_TEMP"
echo "✓ CSR已生成"
echo ""

# 测试1: 签发证书
echo "========================================="
echo "测试1: 直接签发证书"
echo "========================================="
echo ""

ISSUE_RESPONSE=$(curl -s -X POST "${BASE_URL}/issue" \
  -H "Content-Type: application/json" \
  -d "{
    \"csrPemData\": \"$CSR_DATA\",
    \"notBefore\": \"2025-01-01T00:00:00\",
    \"notAfter\": \"2026-12-31T23:59:59\",
    \"signatureAlgorithm\": \"SHA256withECDSA\",
    \"caName\": \"RootCA\"
  }")

echo "响应:"
echo "$ISSUE_RESPONSE"
echo ""

# 检查是否成功(简单的字符串检查)
if echo "$ISSUE_RESPONSE" | grep -q '"code":"0000"'; then
    echo "✓ 证书签发成功!"
    SERIAL_NUMBER=$(echo "$ISSUE_RESPONSE" | grep -o '"serialNumber":"[^"]*"' | cut -d'"' -f4)
    echo "证书序列号: $SERIAL_NUMBER"
else
    echo "❌ 证书签发失败"
    SERIAL_NUMBER=""
fi
echo ""

# 测试2: 查询证书
if [ -n "$SERIAL_NUMBER" ]; then
    echo "========================================="
    echo "测试2: 查询证书"
    echo "========================================="
    echo ""

    QUERY_RESPONSE=$(curl -s "${BASE_URL}/query?serialNumber=$SERIAL_NUMBER")

    echo "响应:"
    echo "$QUERY_RESPONSE"
    echo ""

    if echo "$QUERY_RESPONSE" | grep -q '"code":"0000"'; then
        echo "✓ 证书查询成功!"
    else
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
    echo "$CHAIN_RESPONSE"
    echo ""

    if echo "$CHAIN_RESPONSE" | grep -q '"code":"0000"'; then
        echo "✓ 证书链查询成功!"
    else
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
    echo "$STATUS_RESPONSE"
    echo ""

    if echo "$STATUS_RESPONSE" | grep -q '"code":"0000"'; then
        echo "✓ 证书状态查询成功!"
    else
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
echo "$CRL_RESPONSE"
echo ""

if echo "$CRL_RESPONSE" | grep -q '"code":"0000"'; then
    echo "✓ CRL生成成功!"
else
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
echo "$NOT_FOUND_RESPONSE"
echo ""

if echo "$NOT_FOUND_RESPONSE" | grep -q '"code":"9999"'; then
    echo "✓ 正确处理了不存在的证书查询!"
else
    echo "⚠ 查询行为可能不符合预期"
fi
echo ""

echo "========================================="
echo "  测试完成"
echo "========================================="
