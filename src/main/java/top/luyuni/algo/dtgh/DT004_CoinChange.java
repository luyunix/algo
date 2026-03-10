package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.Arrays;
import java.util.List;

/**
 * DT004 - 零钱兑换
 */
public class DT004_CoinChange {

    public int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1);
        dp[0] = 0;
        for (int coin : coins) {
            for (int i = coin; i <= amount; i++) {
                dp[i] = Math.min(dp[i], dp[i - coin] + 1);
            }
        }
        return dp[amount] > amount ? -1 : dp[amount];
    }

    public static void main(String[] args) {
        DT004_CoinChange solution = new DT004_CoinChange();

        JudgeEngine<TestInput, Integer> engine = new JudgeEngine<>();
        engine.addTestCase("基本例子", new TestInput(new int[]{1, 2, 5}, 11), 3, "5+5+1")
                .addTestCase("无法凑成", new TestInput(new int[]{2}, 3), -1, "只有面额 2")
                .addTestCase("金额为 0", new TestInput(new int[]{1}, 0), 0, "不需要硬币")
                .addTestCase("单硬币", new TestInput(new int[]{1}, 5), 5, "5 个 1 元")
                .addTestCase("多种组合", new TestInput(new int[]{1, 3, 4}, 6), 2, "3+3 最优")
                .addTestCase("大金额", new TestInput(new int[]{186, 419, 83, 408}, 6249), 20, "复杂情况");

        System.out.println("=== 零钱兑换测试 ===");
        List<JudgeResult> results = engine.judge(input ->
                solution.coinChange(input.coins, input.amount)
        );
        JudgeReporter.printReport(results);

        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }

    static class TestInput {
        int[] coins;
        int amount;

        TestInput(int[] coins, int amount) {
            this.coins = coins;
            this.amount = amount;
        }
    }
}
