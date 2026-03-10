#!/bin/bash
# 使用 IntelliJ IDEA 内置格式化器格式化代码

IDEA_PATH="/Applications/IntelliJ IDEA.app"
PROJECT_PATH="/Users/lyn/work/algo"

echo "正在使用 IntelliJ IDEA 格式化代码..."

# 使用 IDEA 的 format 命令
"$IDEA_PATH/Contents/MacOS/idea" format "$PROJECT_PATH/src"

echo "格式化完成！"
