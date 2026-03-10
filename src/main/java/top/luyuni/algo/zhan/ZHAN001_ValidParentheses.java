package top.luyuni.algo.zhan;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * ============================================================
 * 【ZHAN001- 有效的括号】栈的经典应用
 * ============================================================
 * 
 * 题目链接：https://leetcode.cn/problems/valid-parentheses/
 * 
 * 题目描述：
 * 给定一个只包括 '('，')'，'{'，'}'，'['，']' 的字符串 s ，判断字符串是否有效。
 * 有效字符串需满足：
 * 1. 左括号必须用相同类型的右括号闭合
 * 2. 左括号必须以正确的顺序闭合
 * 
 * 示例 1：
 * 输入：s = "()"
 * 输出：true
 * 
 * 示例 2：
 * 输入：s = "()[]{}"
 * 输出：true
 * 
 * 示例 3：
 * 输入：s = "(]"
 * 输出：false
 */
public class ZHAN001_ValidParentheses {
    
    /**
     * 解法：栈匹配
     * 时间复杂度：O(n)
     * 空间复杂度：O(n)
     */
    public boolean isValid(String s) {
        // 空字符串认为有效
      if (s.isEmpty()) {
         return true;
       }
       
      Stack<Character> stack = new Stack<>();
      
     for (char c : s.toCharArray()) {
          // 左括号入栈，对应的右括号也会入栈
        if (c == '(') {
             stack.push(')');
         } else if (c == '{') {
             stack.push('}');
         } else if (c == '[') {
             stack.push(']');
         } else if (stack.isEmpty() || c != stack.pop()) {
             // 右括号匹配失败
           return false;
         }
     }
     
     // 最后栈应该为空
   return stack.isEmpty();
    }
    
    // ============ OJ 判题框架 ============
   
   /**
    * 使用 oj/core 工具进行评测
    */
   public static void main(String[] args) {
       ZHAN001_ValidParentheses solution= new ZHAN001_ValidParentheses();
       
       // 创建判题引擎，输入是 String，输出是 boolean
       JudgeEngine<String, Boolean> engine = new JudgeEngine<>();
       
       // 添加测试用例
       engine
           .addTestCase("示例 1", "()", true, "基本括号匹配")
           .addTestCase("示例 2", "()[]{}", true, "多种括号匹配")
           .addTestCase("示例 3", "(]", false, "类型不匹配")
           .addTestCase("复杂情况", "([)]", false, "嵌套顺序错误")
           .addTestCase("正确嵌套", "{[]}", true, "正确嵌套")
           .addTestCase("空字符串", "", true, "边界情况");
       
       // 执行判题
       System.out.println("=== 有效的括号测试 ===");
       List<JudgeResult> results = engine.judge(input -> solution.isValid(input));
       
       JudgeReporter.printReport(results);
       
       // 统计结果
       boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
       System.exit(allPassed ? 0 : 1);
   }
}
