package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * ============================================================
 * 【DT010 - 打家劫舍】线性 DP 经典
 * ============================================================
 * 
 * 题目链接：https://leetcode.cn/problems/house-robber/
 * 
 * 题目描述：
 * 你是一个专业的小偷，计划偷窃沿街的房屋。每间房内都藏有一定的现金，
 * 影响你偷窃的唯一制约因素就是相邻的房屋装有相互连通的防盗系统，
 * 如果两间相邻的房屋在同一晚上被小偷闯入，系统会自动报警。
 * 
 * 给定一个代表每个房屋存放金额的非负整数数组，计算你 不触动警报装置的情况下 ，
 * 今晚能够偷窃到的最高金额。
 * 
 * 示例 1：
 * 输入：[1,2,3,1]
 * 输出：4
 * 解释：偷窃 1 号房屋 (金额 = 1) ，然后偷窃 3 号房屋 (金额 = 3)。
 *      偷窃到的最高金额 = 1 + 3 = 4 。
 * 
 * 示例 2：
 * 输入：[2,7,9,3,1]
 * 输出：12
 * 解释：偷窃 1 号房屋 (金额 = 2), 偷窃 3 号房屋 (金额 = 9)，
 *      接着偷窃 5 号房屋 (金额 = 1)。
 *      偷窃到的最高金额 = 2 + 9 +1 = 12 。
 */
public class DT010_HouseRobber {
    
    // 方法 1：基础 DP
  public int rob(int[] nums) {
        if (nums == null || nums.length == 0) return 0;
        int n = nums.length;
        int[] dp = new int[n + 1];
        dp[0] = 0;
        dp[1] = nums[0];
        
        for (int i = 2; i <= n; i++) {
            dp[i] = Math.max(dp[i -1], dp[i - 2] + nums[i - 1]);
        }
        return dp[n];
    }
    
    // 方法 2：空间优化
  public int robOptimized(int[] nums) {
        if (nums == null || nums.length == 0) return 0;
        int prev2 = 0, prev1 = 0;
        
        for (int num : nums) {
            int curr = Math.max(prev1, prev2 + num);
           prev2 = prev1;
           prev1 = curr;
        }
        return prev1;
    }
    
    // 方法 3：环形版本（打家劫舍 II）
  public int robCircular(int[] nums) {
        if (nums == null || nums.length == 0) return 0;
        if (nums.length == 1) return nums[0];
        
        // 分两种情况：不偷第一家或不偷最后一家
        return Math.max(robRange(nums, 0, nums.length - 2),
                       robRange(nums, 1, nums.length -1));
    }
    
  private int robRange(int[] nums, int start, int end) {
        int prev2 = 0, prev1 = 0;
        for (int i = start; i <= end; i++) {
            int curr = Math.max(prev1, prev2 + nums[i]);
           prev2 = prev1;
           prev1 = curr;
        }
        return prev1;
    }

    // ============ OJ 判题框架 ============
    
    /**
     * 使用 oj/core 工具进行评测
     */
 public static void main(String[] args) {
        DT010_HouseRobber solution= new DT010_HouseRobber();
        
        // 创建判题引擎，输入是 int[]，输出是 int
        JudgeEngine<int[], Integer> engine = new JudgeEngine<>();
        
        // 添加测试用例
        engine
            .addTestCase("示例 1", new int[]{1,2,3,1}, 4, "偷房子 1 和 3")
            .addTestCase("示例 2", new int[]{2,7,9,3,1}, 12, "偷房子 1、3、5")
            .addTestCase("单个元素", new int[]{5}, 5, "只有一间房子")
            .addTestCase("两间房子", new int[]{2,3}, 3, "偷金额大的")
            .addTestCase("全相同", new int[]{5,5,5,5}, 10, "偷不相邻的两间");
        
        // 执行判题 - 基础 DP
      System.out.println("=== 基础 DP 方法测试 ===");
   List<JudgeResult> results1 = engine.judge(input -> solution.rob(input));
        JudgeReporter.printReport(results1);
        
        // 执行判题 - 空间优化版
      System.out.println("\n=== 空间优化版测试 ===");
   List<JudgeResult> results2 = engine.judge(input -> solution.robOptimized(input));
        JudgeReporter.printReport(results2);
        
        // 统计结果
        boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted)
            && results2.stream().allMatch(JudgeResult::isAccepted);
     System.exit(allPassed ? 0 : 1);
    }
}
