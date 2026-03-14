package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DT015 - 买卖股票的最佳时机 IV
 *
 * 题目：给定一个整数 k 和一个数组 prices，其中 prices[i] 表示第 i 天的股票价格。
 * 你最多可以完成 k 笔交易，设计一个算法来计算你所能获取的最大利润。
 *
 * 限制：最多买卖 k 次
 *
 * 示例：
 * 输入: k = 2, prices = [3,2,6,5,0,3]
 * 输出: 7
 * 解释: (6-2) + (3-0) = 7
 *
 * 来源：LeetCode 188
 */
public class DT015_StockIV {

    /**
     * DP解法 - 通用k次交易
     */
    public int maxProfit(int k, int[] prices) {
        int n = prices.length;
        if (n <= 1 || k == 0) return 0;

        // 如果k很大，相当于无限次交易
        if (k >= n / 2) {
            int profit = 0;
            for (int i = 1; i < n; i++) {
                if (prices[i] > prices[i - 1]) {
                    profit += prices[i] - prices[i - 1];
                }
            }
            return profit;
        }

        // dp[j][0]: 第j次交易后不持有
        // dp[j][1]: 第j次交易中持有
        int[][] dp = new int[k + 1][2];
        for (int j = 0; j <= k; j++) {
            dp[j][1] = Integer.MIN_VALUE;  // 初始化为负无穷
        }

        for (int price : prices) {
            for (int j = 1; j <= k; j++) {
                dp[j][0] = Math.max(dp[j][0], dp[j][1] + price);   // 卖出
                dp[j][1] = Math.max(dp[j][1], dp[j - 1][0] - price); // 买入
            }
        }

        return dp[k][0];
    }

    public static void main(String[] args) {
        DT015_StockIV solution = new DT015_StockIV();

        JudgeEngine<StockInput, Integer> engine = new JudgeEngine<>();
        engine.addTestCase("k=2, [3,2,6,5,0,3]", new StockInput(2, new int[]{3,2,6,5,0,3}), 7, "两次交易")
                .addTestCase("k=2, [3,3,5,0,0,3,1,4]", new StockInput(2, new int[]{3,3,5,0,0,3,1,4}), 6, "两次交易")
                .addTestCase("k=1, [3,2,6,5,0,3]", new StockInput(1, new int[]{3,2,6,5,0,3}), 4, "一次交易")
                .addTestCase("k=0, [3,2,6,5,0,3]", new StockInput(0, new int[]{3,2,6,5,0,3}), 0, "不能交易")
                .addTestCase("k=100, [3,2,6,5,0,3]", new StockInput(100, new int[]{3,2,6,5,0,3}), 7, "k很大，等价无限次");

        System.out.println("=== DP 方法测试 ===");
        List<JudgeResult> results = engine.judge(input -> solution.maxProfit(input.k, input.prices));
        JudgeReporter.printReport(results);

        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }

    static class StockInput {
        int k;
        int[] prices;

        StockInput(int k, int[] prices) {
            this.k = k;
            this.prices = prices;
        }
    }
}
