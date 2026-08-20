#!/bin/bash
# Sales Quest 环境初始化脚本
# 解决沙箱环境每次新会话丢失 Java 17 的问题
# 用法: bash setup-env.sh [gradle 参数...]
# 例:   bash setup-env.sh assembleDebug

set -e

JAVA17_HOME="/usr/lib/jvm/java-17-openjdk-amd64"

if [ ! -d "$JAVA17_HOME" ]; then
    echo ">>> 检测到 Java 17 未安装, 正在安装..."
    apt-get update -qq
    apt-get install -y -qq openjdk-17-jdk >/dev/null 2>&1
    echo ">>> Java 17 安装完成"
else
    echo ">>> Java 17 已就绪, 跳过安装"
fi

cd "$(dirname "$0")"

if [ $# -gt 0 ]; then
    echo ">>> 执行: ./gradlew $@"
    ./gradlew "$@"
else
    echo ">>> 环境已就绪. 用法: bash setup-env.sh [gradle 参数, 如 assembleDebug]"
fi
