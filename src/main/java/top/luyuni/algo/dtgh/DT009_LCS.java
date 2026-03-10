package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * ============================================================
 * 【DT009 - 最长公共子序列】双串 DP 经典
 * ============================================================
 *
 * 题目链接：https://leetcode.cn/problems/longest-common-subsequence/
 *
 * 题目描述：
 * 给定两个字符串 text1 和 text2，返回这两个字符串的最长公共子序列的长度。
 * 如果不存在公共子序列，返回 0 。
 *
 * 一个字符串的子序列是指这样一个新的字符串：它是由原字符串在不改变字符的
 * 相对顺序的情况下删除某些字符（也可以不删除任何字符）后组成的新字符串。
 *
 * 例如，"ace" 是 "abcde" 的子序列，但 "aec" 不是 "abcde" 的子序列。
 * 两个字符串的公共子序列是这两个字符串所共同拥有的子序列。
 *
 * 示例 1：
 * 输入：text1 = "abcde", text2 = "ace"
 * 输出：3
 * 解释：最长公共子序列是 "ace"，它的长度为 3。
 *
 * 示例 2：
 * 输入：text1 = "abc", text2 = "abc"
 * 输出：3
 * 解释：最长公共子序列是 "abc"，它的长度为 3。
 *
 * 示例 3：
 * 输入：text1 = "abc", text2 = "def"
 * 输出：0
 * 解释：两个字符串没有公共子序列，返回 0 。
 */
public class DT009_LCS {

    public int longestCommonSubsequence(String text1, String text2) {
        int m = text1.length();
        int n = text2.length();

        // dp[i][j] 表示 text1 前 i 个字符和 text2 前 j 个字符的 LCS 长度
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
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
        DT009_LCS solution = new DT009_LCS();

        // 创建判题引擎，输入是 String[]（两个字符串），输出是 int
        JudgeEngine<String[], Integer> engine = new JudgeEngine<>();

        // 添加测试用例
        engine
            .addTestCase("abcde-ace", new String[]{"abcde", "ace"}, 3, "最长公共子序列是 ace")
            .addTestCase("abc-abc", new String[]{"abc", "abc"}, 3, "完全相同")
            .addTestCase("abc-def", new String[]{"abc", "def"}, 0, "没有公共子序列")
            .addTestCase("空字符串", new String[]{"", "abc"}, 0, "一个为空")
            .addTestCase("都为空", new String[]{"", ""}, 0, "两个都为空")
            .addTestCase("单字符相同", new String[]{"a", "a"}, 1, "单个相同字符")
            .addTestCase("单字符不同", new String[]{"a", "b"}, 0, "单个不同字符");

        // 执行判题
      System.out.println("=== 最长公共子序列测试 ===");
   List<JudgeResult> results = engine.judge(input -> solution.longestCommonSubsequence(input[0], input[1]));
        JudgeReporter.printReport(results);

        // 统计结果
        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
      System.exit(allPassed ? 0 : 1);
    }
}
