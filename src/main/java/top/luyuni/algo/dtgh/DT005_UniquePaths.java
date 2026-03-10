package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DT005 - 不同路径
 */
public class DT005_UniquePaths {

    public int uniquePaths(int m, int n) {
        int[][] dp = new int[m][n];
        for (int i = 0; i < m; i++) dp[i][0] = 1;
        for (int j = 0; j < n; j++) dp[0][j] = 1;
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[i][j] = dp[i - 1][j] + dp[i][j - 1];
            }
        }
        return dp[m - 1][n - 1];
    }

    public int uniquePathsOptimized(int m, int n) {
        int[] dp = new int[n];
        for (int j = 0; j < n; j++) dp[j] = 1;
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[j] += dp[j - 1];
            }
        }
        return dp[n - 1];
    }

    public int uniquePathsMath(int m, int n) {
        long result = 1;
        for (int i = 0; i < n - 1; i++) {
            result = result * (m + i) / (i + 1);
        }
        return (int) result;
    }

    public static void main(String[] args) {
        DT005_UniquePaths solution = new DT005_UniquePaths();

        JudgeEngine<TestInput, Integer> engine = new JudgeEngine<>();
        engine.addTestCase("3x7", new TestInput(3, 7), 28, "3 行 7 列")
                .addTestCase("3x2", new TestInput(3, 2), 3, "3 行 2 列")
                .addTestCase("1x1", new TestInput(1, 1), 1, "单个格子")
                .addTestCase("1x5", new TestInput(1, 5), 1, "只有一行")
                .addTestCase("5x1", new TestInput(5, 1), 1, "只有一列")
                .addTestCase("10x10", new TestInput(10, 10), 48620, "大网格");

        System.out.println("=== 二维 DP 方法测试 ===");
        List<JudgeResult> results1 = engine.judge(input ->
                solution.uniquePaths(input.m, input.n)
        );
        JudgeReporter.printReport(results1);

        System.out.println("\n=== 空间优化版测试 ===");
        List<JudgeResult> results2 = engine.judge(input ->
                solution.uniquePathsOptimized(input.m, input.n)
        );
        JudgeReporter.printReport(results2);

        System.out.println("\n=== 数学解法测试 ===");
        List<JudgeResult> results3 = engine.judge(input ->
                solution.uniquePathsMath(input.m, input.n)
        );
        JudgeReporter.printReport(results3);

        boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted) &&
                results2.stream().allMatch(JudgeResult::isAccepted) &&
                results3.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }

    static class TestInput {
        int m, n;

        TestInput(int m, int n) {
            this.m = m;
            this.n = n;
        }
    }
}
