package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DT019 - 分组背包问题（掷骰子目标和）
 *
 * 题目：这里有 n 个一样的骰子，每个骰子上都有 k 个面，分别标号为 1 到 k。
 * 给定三个整数 n、k 和 target，请返回投掷骰子的所有可能得到的结果（共有 k^n 种方式），
 * 使得骰子朝上的数字之和等于 target。
 * 由于答案可能很大，你需要对 10^9 + 7 取模。
 *
 * 本题是分拆背包的变体：
 * - 每个骰子视为一组，组内有 k 个物品（面值1到k）
 * - 每组必须选一个物品（骰子必须有值）
 * - 求恰好凑成target的方案数
 *
 * 示例：
 * 输入: n = 2, k = 6, target = 7
 * 输出: 6
 * 解释: (1,6), (2,5), (3,4), (4,3), (5,2), (6,1) 共6种
 *
 * 来源：LeetCode 1155
 */
public class DT019_GroupKnapsack {

    private static final int MOD = 1_000_000_007;

    /**
     * DP解法 - 分组背包
     * dp[i][j] = 用前i个骰子凑成和j的方案数
     */
    public int numRollsToTargetDP(int n, int k, int target) {
        int[][] dp = new int[n + 1][target + 1];
        dp[0][0] = 1; // 0个骰子凑成0的方案数为1

        for (int i = 1; i <= n; i++) { // 枚举每组（每个骰子）
            for (int j = i; j <= target; j++) { // 枚举目标和（至少i，每个骰子最小为1）
                // 枚举组内每个选择（骰子面值1到k）
                for (int face = 1; face <= k && face <= j; face++) {
                    dp[i][j] = (dp[i][j] + dp[i - 1][j - face]) % MOD;
                }
            }
        }

        return dp[n][target];
    }

    /**
     * 空间优化DP - 一维数组
     */
    public int numRollsToTarget(int n, int k, int target) {
        int[] dp = new int[target + 1];
        dp[0] = 1;

        for (int i = 1; i <= n; i++) {
            // 倒序遍历容量，避免重复计算
            for (int j = target; j >= i; j--) {
                dp[j] = 0; // 重置，准备重新计算
                for (int face = 1; face <= k && face <= j; face++) {
                    dp[j] = (dp[j] + dp[j - face]) % MOD;
                }
            }
            // 将小于i的位置设为0（i个骰子最小和为i）
            for (int j = 0; j < i && j <= target; j++) {
                dp[j] = 0;
            }
        }

        return dp[target];
    }

    public static void main(String[] args) {
        DT019_GroupKnapsack solution = new DT019_GroupKnapsack();

        JudgeEngine<KnapsackInput, Integer> engine = new JudgeEngine<>();
        engine.addTestCase("n=2, k=6, target=7",
                new KnapsackInput(2, 6, 7), 6, "两个骰子凑7")
                .addTestCase("n=1, k=6, target=3",
                        new KnapsackInput(1, 6, 3), 1, "一个骰子凑3")
                .addTestCase("n=30, k=30, target=500",
                        new KnapsackInput(30, 30, 500), 222616187, "大数取模")
                .addTestCase("n=3, k=6, target=8",
                        new KnapsackInput(3, 6, 8), 21, "三个骰子凑8")
                .addTestCase("n=2, k=6, target=2",
                        new KnapsackInput(2, 6, 2), 1, "两个1")
                .addTestCase("n=2, k=6, target=12",
                        new KnapsackInput(2, 6, 12), 1, "两个6");

        System.out.println("=== 分组背包解法测试 ===");
        List<JudgeResult> results1 = engine.judge(input ->
                solution.numRollsToTargetDP(input.n, input.k, input.target));
        JudgeReporter.printReport(results1);

        System.out.println("\n=== 空间优化解法测试 ===");
        List<JudgeResult> results2 = engine.judge(input ->
                solution.numRollsToTarget(input.n, input.k, input.target));
        JudgeReporter.printReport(results2);

        boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted) &&
                results2.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }

    static class KnapsackInput {
        int n;
        int k;
        int target;

        KnapsackInput(int n, int k, int target) {
            this.n = n;
            this.k = k;
            this.target = target;
        }
    }
}
