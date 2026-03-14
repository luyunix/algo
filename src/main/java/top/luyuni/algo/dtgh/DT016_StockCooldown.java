package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DT016 - 最佳买卖股票时机含冷冻期
 *
 * 题目：给定一个整数数组 prices，其中 prices[i] 表示第 i 天的股票价格。
 * 卖出股票后，你无法在第二天买入股票（冷冻期为1天）。
 * 设计一个算法计算出最大利润。
 *
 * 限制：卖出后冷冻1天
 *
 * 示例：
 * 输入: prices = [1,2,3,0,2]
 * 输出: 3
 * 解释: (3-0) + (2-1) = 3
 * 第1天买，第2天卖，冷冻期（第3天），第4天买，第5天卖
 *
 * 来源：LeetCode 309
 */
public class DT016_StockCooldown {

    /**
     * DP解法 - 3种状态
     * dp[i][0]: 不持有股票，不在冷冻期
     * dp[i][1]: 持有股票
     * dp[i][2]: 不持有股票，在冷冻期（今天刚卖出）
     */
    public int maxProfitDP(int[] prices) {
        int n = prices.length;
        if (n <= 1) return 0;

        int[][] dp = new int[n][3];
        dp[0][1] = -prices[0];  // 第一天买入

        for (int i = 1; i < n; i++) {
            // 不持有，不在冷冻期：昨天就不持有且非冷冻 或 昨天是冷冻期
            dp[i][0] = Math.max(dp[i-1][0], dp[i-1][2]);
            // 持有：昨天就持有 或 今天买入（昨天不持有且非冷冻）
            dp[i][1] = Math.max(dp[i-1][1], dp[i-1][0] - prices[i]);
            // 不持有，在冷冻期：今天卖出（昨天必须持有）
            dp[i][2] = dp[i-1][1] + prices[i];
        }

        return Math.max(dp[n-1][0], dp[n-1][2]);
    }

    /**
     * 空间优化DP
     */
    public int maxProfit(int[] prices) {
        int n = prices.length;
        if (n <= 1) return 0;

        int notHold = 0;      // 不持有，不在冷冻期
        int hold = -prices[0]; // 持有
        int cooldown = 0;     // 不持有，在冷冻期

        for (int i = 1; i < n; i++) {
            int prevNotHold = notHold;
            int prevHold = hold;
            int prevCooldown = cooldown;

            notHold = Math.max(prevNotHold, prevCooldown);
            hold = Math.max(prevHold, prevNotHold - prices[i]);
            cooldown = prevHold + prices[i];
        }

        return Math.max(notHold, cooldown);
    }

    public static void main(String[] args) {
        DT016_StockCooldown solution = new DT016_StockCooldown();

        JudgeEngine<int[], Integer> engine = new JudgeEngine<>();
        engine.addTestCase("[1,2,3,0,2]", new int[]{1,2,3,0,2}, 3, "经典例子：3")
                .addTestCase("[1]", new int[]{1}, 0, "只有一天")
                .addTestCase("[1,2]", new int[]{1,2}, 1, "涨一天，卖")
                .addTestCase("[6,1,3,2,4,7]", new int[]{6,1,3,2,4,7}, 6, "考虑冷冻期")
                .addTestCase("[1,2,3,0,2,5]", new int[]{1,2,3,0,2,5}, 6, "多次交易");

        System.out.println("=== DP 方法测试 ===");
        List<JudgeResult> results1 = engine.judge(input -> solution.maxProfitDP(input));
        JudgeReporter.printReport(results1);

        System.out.println("\n=== 空间优化DP测试 ===");
        List<JudgeResult> results2 = engine.judge(input -> solution.maxProfit(input));
        JudgeReporter.printReport(results2);

        boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted) &&
                results2.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
}
