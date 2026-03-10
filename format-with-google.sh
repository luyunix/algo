#!/bin/bash
# 使用 Google Java Format 格式化所有 Java 文件

cd /Users/lyn/work/algo

echo "开始格式化 Java 代码..."

count=0
for file in $(find. -name "*.java" -type f | grep -v "/target/"); do
    echo "格式化：$file"
    java-jar google-java-format.jar --replace "$file"
   count=$((count + 1))
done

echo ""
echo "完成！共格式化 $count 个文件"
