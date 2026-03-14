package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DT014 - 买卖股票的最佳时机 III
 *
 * 题目：给定一个数组 prices，其中 prices[i] 表示第 i 天的股票价格。
 * 你最多可以完成两笔交易，设计一个算法来计算你所能获取的最大利润。
 *
 * 限制：最多买卖两次
 *
 * 示例：
 * 输入: prices = [3,3,5,0,0,3,1,4]
 * 输出: 6
 * 解释: 在第 4 天买入（价格=0），在第 6 天卖出（价格=3），利润=3
 *      在第 7 天买入（价格=1），在第 8 天卖出（价格=4），利润=3
 *      总利润 = 3 + 3 = 6
 *
 * 来源：LeetCode 123
 */
public class DT014_StockIII {

    /**
     * DP解法 - 5种状态
     * dp[i][0]: 未交易，不持有
     * dp[i][1]: 第一次持有
     * dp[i][2]: 第一次交易后不持有
     * dp[i][3]: 第二次持有
     * dp[i][4]: 第二次交易后不持有
     */
    public int maxProfitDP(int[] prices) {
        int n = prices.length;
        if (n <= 1) return 0;

        int[][] dp = new int[n][5];
        dp[0][1] = -prices[0];  // 第一次买入
        dp[0][3] = -prices[0];  // 第二次买入（同一天买卖）

        // 其他状态初始为0
        for (int i = 1; i < n; i++) {
            dp[i][1] = Math.max(dp[i-1][1], dp[i-1][0] - prices[i]);  // 第一次买入
            dp[i][2] = Math.max(dp[i-1][2], dp[i-1][1] + prices[i]);  // 第一次卖出
            dp[i][3] = Math.max(dp[i-1][3], dp[i-1][2] - prices[i]);  // 第二次买入
            dp[i][4] = Math.max(dp[i-1][4], dp[i-1][3] + prices[i]);  // 第二次卖出
        }

        return dp[n-1][4];
    }

    /**
     * 空间优化DP
     */
    public int maxProfit(int[] prices) {
        int n = prices.length;
        if (n <= 1) return 0;

        // 5种状态
        int s0 = 0;              // 未交易
        int s1 = -prices[0];     // 第一次持有
        int s2 = 0;              // 第一次交易后不持有
        int s3 = -prices[0];     // 第二次持有
        int s4 = 0;              // 第二次交易后不持有

        for (int i = 1; i < n; i++) {
            s1 = Math.max(s1, s0 - prices[i]);
            s2 = Math.max(s2, s1 + prices[i]);
            s3 = Math.max(s3, s2 - prices[i]);
            s4 = Math.max(s4, s3 + prices[i]);
        }

        return s4;
    }

    public static void main(String[] args) {
        DT014_StockIII solution = new DT014_StockIII();

        JudgeEngine<int[], Integer> engine = new JudgeEngine<>();
        engine.addTestCase("[3,3,5,0,0,3,1,4]", new int[]{3,3,5,0,0,3,1,4}, 6, "两次交易：3+3=6")
                .addTestCase("[1,2,3,4,5]", new int[]{1,2,3,4,5}, 4, "只交易一次：5-1=4")
                .addTestCase("[7,6,4,3,1]", new int[]{7,6,4,3,1}, 0, "递减序列，无利润")
                .addTestCase("[1]", new int[]{1}, 0, "只有一天")
                .addTestCase("[3,2,6,5,0,3]", new int[]{3,2,6,5,0,3}, 7, "4+3=7");

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
