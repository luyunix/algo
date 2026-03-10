#!/usr/bin/env python3
"""
Java 代码格式化脚本
修复缩进、空格、换行等格式问题
"""

import os
import re

def format_java_file(filepath):
    """格式化单个 Java 文件"""
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    formatted_lines = []
    indent_level = 0
    in_comment = False
    
    for line in lines:
        # 移除行尾空格
        line = line.rstrip()
        
        # 跳过空行
       if not line.strip():
            formatted_lines.append('')
            continue
        
        # 处理多行注释
       if '/*' in line and '*/' not in line:
            in_comment= True
        
       if in_comment:
            formatted_lines.append(line)
           if '*/' in line:
                in_comment = False
            continue
        
        # 计算当前行的实际缩进级别
        stripped = line.lstrip()
        
        # 如果一行以 } 开头，减少缩进
       if stripped.startswith('}'):
            indent_level = max(0, indent_level - 1)
        
        # 添加标准缩进（4 个空格）
        formatted_line = '    ' * indent_level + stripped
        
        # 如果行以 { 结尾，增加缩进
       if stripped.endswith('{'):
            indent_level += 1
        
        # 如果单独一行的 }，调整缩进
       if stripped == '}':
            pass  # 已经在前面处理过了
        
        formatted_lines.append(formatted_line)
    
    # 写回文件
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write('\n'.join(formatted_lines))
       if not formatted_lines[-1].endswith('\n'):
            f.write('\n')

def main():
    algo_dir = '/Users/lyn/work/algo'
    count = 0
    
    for root, dirs, files in os.walk(algo_dir):
        # 跳过 target 和 .git 目录
       if '/target/' in root or '/.git/' in root:
            continue
        
        for file in files:
           if file.endswith('.java'):
                filepath = os.path.join(root, file)
                print(f"格式化：{filepath}")
                format_java_file(filepath)
                count += 1
    
    print(f"\n完成！共格式化 {count} 个 Java 文件")

if __name__ == '__main__':
    main()
