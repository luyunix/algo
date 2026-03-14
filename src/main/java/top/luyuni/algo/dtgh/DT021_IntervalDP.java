package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DT021 - 区间DP（最长回文子序列）
 *
 * 题目：给你一个字符串 s ，找出其中最长的回文子序列，并返回该序列的长度。
 * 子序列定义为：不改变剩余字符顺序的情况下，删除某些字符或者不删除任何字符形成的一个序列。
 *
 * 本题是经典的区间DP问题：
 * - 状态定义在区间[i,j]上
 * - 大区间依赖小区间的解
 * - 按区间长度从小到大枚举
 *
 * 示例：
 * 输入: s = "bbbab"
 * 输出: 4
 * 解释: 一个可能的最长回文子序列为 "bbbb"
 *
 * 来源：LeetCode 516
 */
public class DT021_IntervalDP {

    /**
     * DP解法 - 区间DP
     * dp[i][j] = 区间[i,j]的最长回文子序列长度
     */
    public int longestPalindromeSubseqDP(String s) {
        int n = s.length();
        if (n <= 1) return n;

        // dp[i][j] = s[i..j]的最长回文子序列长度
        int[][] dp = new int[n][n];

        // 单个字符是回文，长度为1
        for (int i = 0; i < n; i++) {
            dp[i][i] = 1;
        }

        // 按区间长度从小到大枚举
        for (int len = 2; len <= n; len++) {
            for (int i = 0; i + len - 1 < n; i++) {
                int j = i + len - 1;

                if (s.charAt(i) == s.charAt(j)) {
                    // 两端相同，都可以选
                    dp[i][j] = dp[i + 1][j - 1] + 2;
                } else {
                    // 两端不同，选较大的一边
                    dp[i][j] = Math.max(dp[i + 1][j], dp[i][j - 1]);
                }
            }
        }

        return dp[0][n - 1];
    }

    /**
     * 优化：只存储两行
     */
    public int longestPalindromeSubseq(String s) {
        int n = s.length();
        if (n <= 1) return n;

        // 只保留两行
        int[] prev = new int[n];
        int[] curr = new int[n];

        // 最后一行（i = n-1）
        for (int j = 0; j < n; j++) {
            prev[j] = 1; // dp[n-1][j] = 1 (当n-1==j) 或需要从后往前计算
        }

        for (int i = n - 2; i >= 0; i--) {
            curr[i] = 1; // dp[i][i] = 1
            for (int j = i + 1; j < n; j++) {
                if (s.charAt(i) == s.charAt(j)) {
                    curr[j] = prev[j - 1] + 2;
                } else {
                    curr[j] = Math.max(prev[j], curr[j - 1]);
                }
            }
            // 交换
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }

        return prev[n - 1];
    }

    public static void main(String[] args) {
        DT021_IntervalDP solution = new DT021_IntervalDP();

        JudgeEngine<String, Integer> engine = new JudgeEngine<>();
        engine.addTestCase("bbbab", "bbbab", 4, "bbbb")
                .addTestCase("cbbd", "cbbd", 2, "bb")
                .addTestCase("a", "a", 1, "单字符")
                .addTestCase("aaaa", "aaaa", 4, "全部相同")
                .addTestCase("abcdef", "abcdef", 1, "无回文，单字符")
                .addTestCase("abcba", "abcba", 5, "本身就是回文")
                .addTestCase("character", "character", 5, "carac");

        System.out.println("=== 区间DP解法测试 ===");
        List<JudgeResult> results1 = engine.judge(input -> solution.longestPalindromeSubseqDP(input));
        JudgeReporter.printReport(results1);

        System.out.println("\n=== 空间优化解法测试 ===");
        List<JudgeResult> results2 = engine.judge(input -> solution.longestPalindromeSubseq(input));
        JudgeReporter.printReport(results2);

        boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted) &&
                results2.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
}
