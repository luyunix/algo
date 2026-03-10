package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * ============================================================
 * 【DT007 - 编辑距离】双串 DP 经典
 * ============================================================
 * 
 * 题目链接：https://leetcode.cn/problems/edit-distance/
 * 
 * 题目描述：
 * 给你两个单词 word1 和 word2，请返回将 word1 转换成 word2 所使用的
 * 最少操作数 。
 * 
 * 你可以对一个单词进行如下三种操作：
 * - 插入一个字符
 * - 删除一个字符
 * - 替换一个字符
 * 
 * 示例 1：
 * 输入：word1 = "horse", word2 = "ros"
 * 输出：3
 * 解释：
 * horse -> rorse (将 'h' 替换为 'r')
 * rorse -> rose (删除 'r')
 * rose -> ros (删除 'e')
 * 
 * 示例 2：
 * 输入：word1 = "intention", word2 = "execution"
 * 输出：5
 * 解释：
 * intention -> inention (删除 't')
 * inention -> enention (将 'i' 替换为 'e')
 * enention -> exention (将 'n' 替换为 'x')
 * exention -> exection (将 'n' 替换为 'c')
 * exection -> execution (插入 'u')
 */
public class DT007_EditDistance {
    
    /**
     * 编辑距离（双串 DP）
     * 
     * 思路：
     * 1. dp[i][j] 表示 word1 前 i 个字符和 word2 前 j 个字符的最小编辑距离
     * 2. 三种操作：
     *    - 替换：dp[i-1][j-1] + 1（如果 word1[i-1] != word2[j-1]）
     *    - 删除：dp[i-1][j] + 1（删除 word1 的字符）
     *    - 插入：dp[i][j-1] + 1（在 word1 插入字符）
     * 3. 状态转移方程：
     *    dp[i][j] = min(
     *        dp[i-1][j-1] + (word1[i-1] == word2[j-1] ? 0 : 1),  // 替换
     *        dp[i-1][j] + 1,  // 删除
     *        dp[i][j-1] + 1   // 插入
     *    )
     * 
     * @param word1 源字符串
     * @param word2 目标字符串
     * @return 最小编辑距离
     */
    public int minDistance(String word1, String word2) {
     int m = word1.length();
      int n = word2.length();
       
      // dp[i][j] 表示 word1 前 i 个字符和 word2 前 j 个字符的最小编辑距离
      int[][] dp = new int[m +1][n + 1];
       
      // 初始化边界条件
      for (int i = 0; i <= m; i++) dp[i][0] = i;  // 删除 i 个字符
      for (int j = 0; j <= n; j++) dp[0][j] = j;  // 插入 j 个字符
       
      // 填充 dp 表
      for (int i = 1; i <= m; i++) {
         for (int j = 1; j <= n; j++) {
          if (word1.charAt(i - 1) == word2.charAt(j -1)) {
               dp[i][j] = dp[i -1][j - 1];  // 字符相同，不需要操作
            } else {
               dp[i][j] = Math.min(
                  Math.min(dp[i - 1][j -1], dp[i - 1][j]),
                  dp[i][j - 1]
               ) +1;
            }
         }
      }
       
    return dp[m][n];
   }
    
    // ============ OJ 判题框架 ============
    
    /**
     * 使用 oj/core 工具进行评测
     */
 public static void main(String[] args) {
        DT007_EditDistance solution = new DT007_EditDistance();
        
        // 创建判题引擎，输入是 String[]（两个单词），输出是 int
        JudgeEngine<String[], Integer> engine = new JudgeEngine<>();
        
        // 添加测试用例
        engine
            .addTestCase("horse->ros", new String[]{"horse", "ros"}, 3, "删除 h，替换 r->o，删除 e")
            .addTestCase("intention->execution", new String[]{"intention", "execution"}, 5, "多次操作")
            .addTestCase("相同字符串", new String[]{"abc", "abc"}, 0, "不需要操作")
            .addTestCase("空字符串", new String[]{"", "abc"}, 3, "插入 3 个字符")
            .addTestCase("空字符串 2", new String[]{"abc", ""}, 3, "删除 3 个字符")
            .addTestCase("都为空", new String[]{"", ""}, 0, "两个空串")
            .addTestCase("单字符替换", new String[]{"a", "b"}, 1, "替换一次");
        
        // 执行判题
        System.out.println("=== 编辑距离测试 ===");
     List<JudgeResult> results = engine.judge(input -> solution.minDistance(input[0], input[1]));
        JudgeReporter.printReport(results);
        
        // 统计结果
        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
}
