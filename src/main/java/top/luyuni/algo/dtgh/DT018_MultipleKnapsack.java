package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DT018 - 多重背包问题（一和零）
 *
 * 题目：给你一个二进制字符串数组 strs 和两个整数 m 和 n。
 * 请你找出并返回 strs 的最大子集的长度，该子集中 最多 有 m 个 0 和 n 个 1。
 * 如果 x 的所有元素也是 y 的元素，集合 x 是集合 y 的 子集。
 *
 * 本题可转化为二维费用的多重背包问题：
 * - 每个字符串视为一个物品
 * - 0的个数视为重量1，1的个数视为重量2
 * - 背包容量为 (m, n)
 * - 求最多能装多少个物品
 *
 * 示例：
 * 输入: strs = ["10", "0001", "111001", "1", "0"], m = 5, n = 3
 * 输出: 4
 * 解释: 最多有 5 个 0 和 3 个 1 的最大的子集是 {"10", "0001", "1", "0"}
 *
 * 来源：LeetCode 474
 */
public class DT018_MultipleKnapsack {

    /**
     * 二维费用背包解法
     * dp[i][j] = 最多i个0，j个1时能选取的最大子集大小
     */
    public int findMaxForm(String[] strs, int m, int n) {
        int[][] dp = new int[m + 1][n + 1];

        for (String str : strs) {
            // 统计当前字符串中0和1的个数
            int zeros = 0, ones = 0;
            for (char c : str.toCharArray()) {
                if (c == '0') zeros++;
                else ones++;
            }

            // 二维费用背包，倒序遍历
            for (int i = m; i >= zeros; i--) {
                for (int j = n; j >= ones; j--) {
                    dp[i][j] = Math.max(dp[i][j], dp[i - zeros][j - ones] + 1);
                }
            }
        }

        return dp[m][n];
    }

    public static void main(String[] args) {
        DT018_MultipleKnapsack solution = new DT018_MultipleKnapsack();

        JudgeEngine<KnapsackInput, Integer> engine = new JudgeEngine<>();
        engine.addTestCase("strs=[10,0001,111001,1,0], m=5, n=3",
                new KnapsackInput(new String[]{"10", "0001", "111001", "1", "0"}, 5, 3), 4, "经典例子")
                .addTestCase("strs=[10,0,1], m=1, n=1",
                        new KnapsackInput(new String[]{"10", "0", "1"}, 1, 1), 2, "选0和1")
                .addTestCase("strs=[0,0,0], m=1, n=1",
                        new KnapsackInput(new String[]{"0", "0", "0"}, 1, 1), 1, "只能选一个0")
                .addTestCase("strs=[0,0,0], m=3, n=0",
                        new KnapsackInput(new String[]{"0", "0", "0"}, 3, 0), 3, "三个0都能选")
                .addTestCase("strs=[], m=0, n=0",
                        new KnapsackInput(new String[]{}, 0, 0), 0, "空数组");

        System.out.println("=== 二维费用背包解法测试 ===");
        List<JudgeResult> results = engine.judge(input ->
                solution.findMaxForm(input.strs, input.m, input.n));
        JudgeReporter.printReport(results);

        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }

    static class KnapsackInput {
        String[] strs;
        int m;
        int n;

        KnapsackInput(String[] strs, int m, int n) {
            this.strs = strs;
            this.m = m;
            this.n = n;
        }
    }
}
