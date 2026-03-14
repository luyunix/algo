package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DT017 - 买卖股票的最佳时机含手续费
 *
 * 题目：给定一个整数数组 prices，其中 prices[i] 表示第 i 天的股票价格，整数 fee 表示交易手续费。
 * 你可以无限次地完成交易，但是你每笔交易都需要付手续费。如果你已经购买了一个股票，在卖出它之前你就不能再继续购买股票了。
 * 返回获得利润的最大值。
 *
 * 限制：每笔交易需付手续费
 *
 * 示例：
 * 输入: prices = [1,3,2,8,4,9], fee = 2
 * 输出: 8
 * 解释: (8-1) - 2 + (9-4) - 2 = 8
 * 第1天买，第4天卖，第5天买，第6天卖
 *
 * 来源：LeetCode 714
 */
public class DT017_StockFee {

    /**
     * DP解法
     * dp[i][0]: 第i天结束后不持有股票的最大利润
     * dp[i][1]: 第i天结束后持有股票的最大利润
     */
    public int maxProfitDP(int[] prices, int fee) {
        int n = prices.length;
        if (n <= 1) return 0;

        int[][] dp = new int[n][2];
        dp[0][1] = -prices[0]; // 第一天买入

        for (int i = 1; i < n; i++) {
            // 不持有：昨天就不持有 或 今天卖出（扣除手续费）
            dp[i][0] = Math.max(dp[i-1][0], dp[i-1][1] + prices[i] - fee);
            // 持有：昨天就持有 或 今天买入
            dp[i][1] = Math.max(dp[i-1][1], dp[i-1][0] - prices[i]);
        }

        return dp[n-1][0];
    }

    /**
     * 空间优化DP
     */
    public int maxProfit(int[] prices, int fee) {
        int n = prices.length;
        if (n <= 1) return 0;

        int notHold = 0;      // 不持有
        int hold = -prices[0]; // 持有

        for (int i = 1; i < n; i++) {
            notHold = Math.max(notHold, hold + prices[i] - fee);
            hold = Math.max(hold, notHold - prices[i]);
        }

        return notHold;
    }

    public static void main(String[] args) {
        DT017_StockFee solution = new DT017_StockFee();

        JudgeEngine<StockInput, Integer> engine = new JudgeEngine<>();
        engine.addTestCase("[1,3,2,8,4,9], fee=2", new StockInput(new int[]{1,3,2,8,4,9}, 2), 8, "两次交易：5+5-2=8")
                .addTestCase("[1,3,7,5,10,3], fee=3", new StockInput(new int[]{1,3,7,5,10,3}, 3), 6, "(10-1)-3=6")
                .addTestCase("[1,2], fee=1", new StockInput(new int[]{1,2}, 1), 0, "利润不够手续费")
                .addTestCase("[1], fee=0", new StockInput(new int[]{1}, 0), 0, "只有一天");

        System.out.println("=== DP 方法测试 ===");
        List<JudgeResult> results1 = engine.judge(input -> solution.maxProfitDP(input.prices, input.fee));
        JudgeReporter.printReport(results1);

        System.out.println("\n=== 空间优化DP测试 ===");
        List<JudgeResult> results2 = engine.judge(input -> solution.maxProfit(input.prices, input.fee));
        JudgeReporter.printReport(results2);

        boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted) &&
                results2.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }

    static class StockInput {
        int[] prices;
        int fee;

        StockInput(int[] prices, int fee) {
            this.prices = prices;
            this.fee = fee;
        }
    }
}
