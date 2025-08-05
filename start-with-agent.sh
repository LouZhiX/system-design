#!/bin/bash

# SQL监控演示启动脚本
echo "=== SQL监控演示启动脚本 ==="

# 编译项目
echo "1. 编译项目..."
mvn clean package

# 检查编译是否成功
if [ $? -ne 0 ]; then
    echo "编译失败，请检查错误信息"
    exit 1
fi

echo "2. 启动应用（带Java Agent）..."
echo "注意：这将启动SQL监控功能，所有SQL执行都会被监控和打印"

# 启动应用，使用Java Agent
java -javaagent:target/system-design-0.0.1-SNAPSHOT.jar \
     -jar target/system-design-0.0.1-SNAPSHOT.jar

echo "应用已启动，访问以下地址："
echo "- 应用主页: http://localhost:8080"
echo "- H2数据库控制台: http://localhost:8080/h2-console"
echo "- API文档: http://localhost:8080/api/users"
echo ""
echo "测试API示例："
echo "curl http://localhost:8080/api/users"
echo "curl http://localhost:8080/api/users/1"
echo "curl http://localhost:8080/api/users/demo"