package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * ============================================================
 * 【DT006 - 最长递增子序列】线性 DP 经典
 * ============================================================
 * <p>
 * 题目链接：https://leetcode.cn/problems/longest-increasing-subsequence/
 * <p>
 * 题目描述：
 * 给你一个整数数组 nums ，找到其中最长严格递增子序列的长度。
 * <p>
 * 子序列是由数组派生而来的序列，删除（或不删除）数组中的元素而不改变
 * 其余元素的顺序。例如，[3,6,2,7] 是数组 [0,3,1,6,2,2,7] 的子序列。
 * <p>
 * 示例 1：
 * 输入：nums = [10,9,2,5,3,7,101,18]
 * 输出：4
 * 解释：最长递增子序列是 [2,3,7,101]，因此长度为 4 。
 * <p>
 * 示例 2：
 * 输入：nums = [0,1,0,3,2,3]
 * 输出：4
 * <p>
 * 示例 3：
 * 输入：nums = [7,7,7,7,7,7,7]
 * 输出：1
 */
public class DT006_LIS {

    // 方法 1：基础 DP O(n²)
    public int lengthOfLIS(int[] nums) {
        if (nums == null || nums.length == 0) return 0;
        int n = nums.length;
        int[] dp = new int[n];
        Arrays.fill(dp, 1);
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[j] < nums[i]) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
        }
        int maxLen = 0;
        for (int len : dp) {
            maxLen = Math.max(maxLen, len);
        }
        return maxLen;
    }

    // 方法 2：二分优化 O(n log n)
    public int lengthOfLISBinary(int[] nums) {
        if (nums == null || nums.length == 0) return 0;
        int[] tails = new int[nums.length];
        int size = 0;
        for (int num : nums) {
            int left = 0, right = size;
            while (left < right) {
                int mid = left + (right - left) / 2;
                if (tails[mid] < num) {
                    left = mid + 1;
                } else {
                    right = mid;
                }
            }
            tails[left] = num;
            if (left == size) size++;
        }
        return size;
    }

    // ============ OJ 判题框架 ============

    /**
     * 使用 oj/core 工具进行评测
     */
    public static void main(String[] args) {
        DT006_LIS solution = new DT006_LIS();

        // 创建判题引擎，输入是 int[]，输出是 int
        JudgeEngine<int[], Integer> engine = new JudgeEngine<>();

        // 添加测试用例
        engine
                .addTestCase("示例 1", new int[]{10, 9, 2, 5, 3, 7, 101, 18}, 4, "[2,3,7,101] 或 [2,3,7,18]")
                .addTestCase("递增序列", new int[]{0, 1, 0, 3, 2, 3}, 4, "[0,1,2,3]")
                .addTestCase("递减序列", new int[]{7, 7, 7, 7, 7}, 1, "所有元素相同")
                .addTestCase("单元素", new int[]{100}, 1, "只有一个元素")
                .addTestCase("完全有序", new int[]{1, 2, 3, 4, 5}, 5, "本身就是递增的");

        // 执行判题 - 基础 DP
        System.out.println("=== 基础 DP 方法测试 ===");
        List<JudgeResult> results1 = engine.judge(input -> solution.lengthOfLIS(input));
        JudgeReporter.printReport(results1);

        // 执行判题 - 二分优化版
        System.out.println("\n=== 二分优化版测试 ===");
        List<JudgeResult> results2 = engine.judge(input -> solution.lengthOfLISBinary(input));
        JudgeReporter.printReport(results2);

        // 统计结果
        boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted)
                && results2.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
}
