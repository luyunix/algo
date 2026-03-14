package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DT013 - 买卖股票的最佳时机 II
 *
 * 题目：给定一个数组 prices，其中 prices[i] 表示第 i 天的股票价格。
 * 你可以尽可能地完成更多的交易（多次买卖一支股票），但不能同时参与多笔交易。
 * 设计一个算法来计算你所能获取的最大利润。
 *
 * 限制：可以买卖多次，但不能同时持有多个
 *
 * 示例：
 * 输入: prices = [7,1,5,3,6,4]
 * 输出: 7
 * 解释: (5-1) + (6-3) = 7
 *
 * 来源：LeetCode 122
 */
public class DT013_StockII {

    /**
     * DP解法
     * dp[i][0]: 第i天结束后不持有股票的最大利润
     * dp[i][1]: 第i天结束后持有股票的最大利润
     */
    public int maxProfitDP(int[] prices) {
        int n = prices.length;
        if (n <= 1) return 0;

        int[][] dp = new int[n][2];
        dp[0][1] = -prices[0]; // 第一天买入

        for (int i = 1; i < n; i++) {
            dp[i][0] = Math.max(dp[i-1][0], dp[i-1][1] + prices[i]); // 不持有：昨天就不持有 或 今天卖出
            dp[i][1] = Math.max(dp[i-1][1], dp[i-1][0] - prices[i]); // 持有：昨天就持有 或 今天买入（可多次）
        }

        return dp[n-1][0];
    }

    /**
     * 空间优化DP
     */
    public int maxProfitDPOptimized(int[] prices) {
        int n = prices.length;
        if (n <= 1) return 0;

        int notHold = 0;      // 不持有
        int hold = -prices[0]; // 持有

        for (int i = 1; i < n; i++) {
            notHold = Math.max(notHold, hold + prices[i]);
            hold = Math.max(hold, notHold - prices[i]);
        }

        return notHold;
    }

    /**
     * 贪心解法（最简）
     * 只要有涨就卖
     */
    public int maxProfit(int[] prices) {
        int profit = 0;
        for (int i = 1; i < prices.length; i++) {
            if (prices[i] > prices[i-1]) {
                profit += prices[i] - prices[i-1];
            }
        }
        return profit;
    }

    public static void main(String[] args) {
        DT013_StockII solution = new DT013_StockII();

        JudgeEngine<int[], Integer> engine = new JudgeEngine<>();
        engine.addTestCase("[7,1,5,3,6,4]", new int[]{7,1,5,3,6,4}, 7, "两次交易：4+3=7")
                .addTestCase("[1,2,3,4,5]", new int[]{1,2,3,4,5}, 4, "一直涨，持有到最后：5-1=4")
                .addTestCase("[7,6,4,3,1]", new int[]{7,6,4,3,1}, 0, "递减序列，无利润")
                .addTestCase("[1]", new int[]{1}, 0, "只有一天")
                .addTestCase("[1,2]", new int[]{1,2}, 1, "涨一天，卖")
                .addTestCase("[2,1]", new int[]{2,1}, 0, "跌一天，不买");

        System.out.println("=== DP 方法测试 ===");
        List<JudgeResult> results1 = engine.judge(input -> solution.maxProfitDP(input));
        JudgeReporter.printReport(results1);

        System.out.println("\n=== 空间优化DP测试 ===");
        List<JudgeResult> results2 = engine.judge(input -> solution.maxProfitDPOptimized(input));
        JudgeReporter.printReport(results2);

        System.out.println("\n=== 贪心方法测试 ===");
        List<JudgeResult> results3 = engine.judge(input -> solution.maxProfit(input));
        JudgeReporter.printReport(results3);

        boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted) &&
                results2.stream().allMatch(JudgeResult::isAccepted) &&
                results3.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
}
