#!/bin/bash

# 格式化所有 Java 文件的脚本
echo "开始格式化 Java 代码..."

cd /Users/lyn/work/algo

# 统计文件数量
count=0

# 遍历所有 Java 文件
for file in $(find . -name "*.java" -type f | grep -v "/target/"); do
    echo "格式化：$file"
    
    # 使用 sed 修复常见的格式问题
    # 1. 替换制表符为 4 个空格
    sed -i '' 's/\t/    /g' "$file"
    
    # 2. 移除行尾多余的空格
    sed -i '' 's/[[:space:]]*$//' "$file"
    
    count=$((count + 1))
done

echo ""
echo "完成！共格式化 $count 个 Java 文件"
