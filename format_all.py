#!/usr/bin/env python3
import subprocess
import os
from pathlib import Path

algo_dir = Path('/Users/lyn/work/algo')
google_format_jar = algo_dir / 'google-java-format.jar'

# 查找所有 Java 文件
java_files = []
for root, dirs, files in os.walk(algo_dir / 'src'):
   if '/target/' in root:
       continue
   for file in files:
       if file.endswith('.java'):
            java_files.append(os.path.join(root, file))

print(f"找到 {len(java_files)} 个 Java 文件")
print("开始格式化...")

count = 0
for file_path in java_files:
    try:
        print(f"格式化：{file_path}")
       result = subprocess.run([
            'java', '-jar', str(google_format_jar),
            '--replace', file_path
        ], capture_output=True, text=True)
        
       if result.returncode == 0:
           count += 1
        else:
            print(f"  ❌ 失败：{result.stderr}")
    except Exception as e:
        print(f"  ❌ 错误：{e}")

print(f"\n✅ 完成！成功格式化 {count}/{len(java_files)} 个文件")
