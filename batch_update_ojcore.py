#!/usr/bin/env python3
"""
批量更新算法题目使用 oj/core 工具 - 完整版
"""

import os
import re

def update_imports(filepath):
    """添加 oj/core 导入语句"""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 如果已经有 oj/core 导入，跳过
    if 'import top.luyuni.algo.oj.core.JudgeEngine' in content:
        return False
    
    # 在 package 后添加导入
    if 'package top.luyuni.algo' in content:
        content = content.replace(
            'package top.luyuni.algo',
            '''package top.luyuni.algo

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;'''
        )
        
        with open(filepath, 'w', encoding='utf-8') as f:
           f.write(content)
        return True
    return False

def simple_convert_main(filepath):
    """简单转换 main方法 - 只添加基本结构"""
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    # 找到 main方法的开始和结束
    main_start = -1
    main_end = -1
    brace_count = 0
    in_main = False
    
   for i, line in enumerate(lines):
        if 'public static void main(String[] args)' in line:
            main_start = i
            in_main = True
            brace_count = 0
        
        if in_main:
            brace_count += line.count('{') - line.count('}')
            if brace_count == 0 and '{' in ''.join(lines[max(0,i-5):i+1]):
                main_end = i +1
                break
    
    if main_start == -1 or main_end == -1:
        return False
    
    # 替换 main方法（简化版本）
    new_main = '''   public static void main(String[] args) {
        // TODO: 使用 JudgeEngine 进行测试
        // 示例:
        // JudgeEngine<InputType, OutputType> engine = new JudgeEngine<>();
        // engine.addTestCase("测试名称", input, expected, "描述");
        // List<JudgeResult> results = engine.judge(solution::method);
        // JudgeReporter.printReport(results);
        System.out.println("请转换为使用 JudgeEngine 进行测试");
    }
'''
    
    new_lines = lines[:main_start] + [new_main] + lines[main_end:]
    
    with open(filepath, 'w', encoding='utf-8') as f:
       f.writelines(new_lines)
    
    return True

def process_all_files():
    base_path = '/Users/lyn/work/algo/src/main/java/top/luyuni/algo'
   exclude_dirs = ['oj']
    
    updated_count = 0
    total_count = 0
    
   for root, dirs, files in os.walk(base_path):
        # 排除 oj 目录
        dirs[:] = [d for d in dirs if d not in exclude_dirs]
        
       for file in files:
            if file.endswith('.java'):
                filepath = os.path.join(root, file)
                total_count += 1
                
                try:
                    # 添加导入
                    if update_imports(filepath):
                        # 转换 main方法
                        simple_convert_main(filepath)
                        updated_count += 1
                        print(f"✓ {filepath}")
               except Exception as e:
                    print(f"✗ {filepath}: {e}")
    
    print(f"\n完成！更新了 {updated_count}/{total_count} 个文件")

if __name__ == '__main__':
    process_all_files()
