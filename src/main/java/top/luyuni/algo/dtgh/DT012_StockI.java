package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DT012 - 买卖股票的最佳时机 I
 *
 * 题目：给定一个数组 prices，其中 prices[i] 表示第 i 天的股票价格。
 * 你只能选择某一天买入这只股票，并选择在未来的某一个不同的日子卖出该股票。
 * 设计一个算法来计算你所能获取的最大利润。如果你不能获取任何利润，返回 0。
 *
 * 限制：只能买卖一次
 *
 * 示例：
 * 输入: prices = [7,1,5,3,6,4]
 * 输出: 5
 * 解释: 在第 2 天买入（价格=1），在第 5 天卖出（价格=6），利润=6-1=5
 *
 * 来源：LeetCode 121
 */
public class DT012_StockI {

    /**
     * DP解法
     * dp[i][0]: 第i天结束后不持有股票的最大利润
     * dp[i][1]: 第i天结束后持有股票的最大利润
     */
    public int maxProfitDP(int[] prices) {
        int n = prices.length;
        if (n <= 1) return 0;

        // dp[i][0]: 不持有, dp[i][1]: 持有
        int[][] dp = new int[n][2];
        dp[0][1] = -prices[0]; // 第一天买入

        for (int i = 1; i < n; i++) {
            dp[i][0] = Math.max(dp[i-1][0], dp[i-1][1] + prices[i]); // 不持有：昨天就不持有 或 今天卖出
            dp[i][1] = Math.max(dp[i-1][1], -prices[i]); // 持有：昨天就持有 或 今天买入（首次）
        }

        return dp[n-1][0];
    }

    /**
     * 贪心解法（更优）
     * 维护最小价格和最大利润
     */
    public int maxProfit(int[] prices) {
        int minPrice = Integer.MAX_VALUE;
        int maxProfit = 0;

        for (int price : prices) {
            minPrice = Math.min(minPrice, price);
            maxProfit = Math.max(maxProfit, price - minPrice);
        }

        return maxProfit;
    }

    public static void main(String[] args) {
        DT012_StockI solution = new DT012_StockI();

        JudgeEngine<int[], Integer> engine = new JudgeEngine<>();
        engine.addTestCase("[7,1,5,3,6,4]", new int[]{7,1,5,3,6,4}, 5, "在第2天买第5天卖")
                .addTestCase("[7,6,4,3,1]", new int[]{7,6,4,3,1}, 0, "递减序列，无利润")
                .addTestCase("[1,2]", new int[]{1,2}, 1, "最小情况")
                .addTestCase("[1]", new int[]{1}, 0, "只有一天")
                .addTestCase("[3,2,6,5,0,3]", new int[]{3,2,6,5,0,3}, 4, "在第2天买第3天卖")
                .addTestCase("[2,4,1]", new int[]{2,4,1}, 2, "非单调序列");

        System.out.println("=== DP 方法测试 ===");
        List<JudgeResult> results1 = engine.judge(input -> solution.maxProfitDP(input));
        JudgeReporter.printReport(results1);

        System.out.println("\n=== 贪心方法测试 ===");
        List<JudgeResult> results2 = engine.judge(input -> solution.maxProfit(input));
        JudgeReporter.printReport(results2);

        boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted) &&
                results2.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
}
