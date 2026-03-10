package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * ============================================================
 * 【DT011 - 爬楼梯变种：每次最多 K 级】
 * ============================================================
 * <p>
 * 题目描述：
 * 有 N 级的台阶，你一开始在底部（第 0 级），每次可以向上迈最多 K 级台阶
 * （最少 1 级），问到达第 N 级台阶有多少种不同方式。
 * <p>
 * 示例 1：
 * 输入：N = 3, K = 2
 * 输出：3
 * 解释：
 * 1. 1 级 +1 级 +1 级
 * 2. 1 级 + 2 级
 * 3. 2 级 +1 级
 * <p>
 * 示例 2：
 * 输入：N = 4, K = 3
 * 输出：7
 * 解释：
 * 1. 1+1+1+1
 * 2. 1+1+2
 * 3. 1+2+1
 * 4. 2+1+1
 * 5. 2+2
 * 6. 1+3
 * 7. 3+1
 * <p>
 * 示例 3：
 * 输入：N = 5, K = 5
 * 输出：16
 */
public class DT011_ClimbStairsK {

    // 方法 1：基础 DP
    public int climbStairsK(int N, int K) {
        int[] dp = new int[N + 1];

        dp[0] = 1;
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= K; j++) {
                if (i - j >= 0) {
                    dp[i] += dp[i - j];
                }
            }
        }
        return dp[N];
    }

    // 方法 2：空间优化（滑动窗口）
    public int climbStairsKOptimized(int N, int K) {
        if (N == 0) return 1;
        int[] dp = new int[N + 1];
        dp[0] = 1;

        // 维护一个滑动窗口的和
        int windowSum = 0;
        for (int i = 1; i <= N; i++) {
            // 加入新的元素
            windowSum += dp[i - 1];
            // 移除超出窗口大小的元素
            if (i > K) {
                windowSum -= dp[i - K - 1];
            }
            dp[i] = windowSum;
        }
        return dp[N];
    }

    // 方法 3：给定步长集合的版本
    public int climbStairsWithSet(int N, int[] steps) {
        int[] dp = new int[N + 1];
        dp[0] = 1;

        for (int i = 1; i <= N; i++) {
            for (int step : steps) {
                if (i - step >= 0) {
                    dp[i] += dp[i - step];
                }
            }
        }
        return dp[N];
    }

    // ============ OJ 判题框架 ============

    /**
     * 使用 oj/core 工具进行评测
     */
    public static void main(String[] args) {
        DT011_ClimbStairsK solution = new DT011_ClimbStairsK();

        // 创建判题引擎，输入是 int[]（N 和 K），输出是 int
        JudgeEngine<int[], Integer> engine = new JudgeEngine<>();

        // 添加测试用例
        engine
                .addTestCase("N=3,K=2", new int[]{3, 2}, 3, "1+1+1, 1+2, 2+1")
                .addTestCase("N=4,K=3", new int[]{4, 3}, 7, "最多爬 3 阶")
                .addTestCase("N=5,K=5", new int[]{5, 5}, 16, "可以直接爬到顶")
                .addTestCase("N=1,K=2", new int[]{1, 2}, 1, "只有 1 阶")
                .addTestCase("N=0,K=2", new int[]{0, 2}, 1, "地面不动")
                .addTestCase("N=10,K=2", new int[]{10, 2}, 89, "斐波那契数列");

        // 执行判题 - 基础 DP
        System.out.println("=== 基础 DP 方法测试 ===");
        List<JudgeResult> results1 = engine.judge(input -> solution.climbStairsK(input[0], input[1]));
        JudgeReporter.printReport(results1);

        // 执行判题 - 空间优化版
        System.out.println("\n=== 空间优化版测试 ===");
        List<JudgeResult> results2 = engine.judge(input -> solution.climbStairsKOptimized(input[0], input[1]));
        JudgeReporter.printReport(results2);

        // 统计结果
        boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted)
                && results2.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
}
