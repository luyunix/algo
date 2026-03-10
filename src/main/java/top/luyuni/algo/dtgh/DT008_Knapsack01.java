package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * ============================================================
 * 【DT008 - 01 背包】背包问题基础
 * ============================================================
 * 
 * 题目描述：
 * 有 n 个物品和一个容量为 W 的背包。第 i 个物品的重量是 weight[i]，
 * 价值是 value[i]。每个物品只能使用一次，求解将哪些物品装入背包，
 * 可使这些物品的总重量不超过背包容量，且总价值最大。
 * 
 * 示例：
 * weight = [1, 3, 4], value = [15, 20, 30], W = 4
 * 
 * 输出：35
 * 解释：选物品 0 和物品 1，重量 1+3=4，价值 15+20=35
 */
public class DT008_Knapsack01 {
    
    // 方法 1：二维 DP
  public int knapsack(int[] weight, int[] value, int W) {
        int n = weight.length;
        int[][] dp = new int[n + 1][W + 1];
        
        for (int i = 1; i <= n; i++) {
            for (int w = 1; w <= W; w++) {
                if (weight[i -1] <= w) {
                    dp[i][w] = Math.max(dp[i -1][w], dp[i -1][w - weight[i -1]] + value[i - 1]);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }
        return dp[n][W];
    }
    
    // 方法 2：空间优化（一维 DP）
  public int knapsackOptimized(int[] weight, int[] value, int W) {
        int n = weight.length;
        int[] dp = new int[W + 1];
        
        for (int i = 0; i < n; i++) {
            // 倒序遍历，防止重复使用同一物品
            for (int w = W; w >= weight[i]; w--) {
                dp[w] = Math.max(dp[w], dp[w - weight[i]] + value[i]);
            }
        }
        return dp[W];
    }
    
    // ============ OJ 判题框架 ============
    
    /**
     * 使用 oj/core 工具进行评测
     */
 public static void main(String[] args) {
        DT008_Knapsack01 solution = new DT008_Knapsack01();
        
        // 创建判题引擎，输入是 Object[]（weight[], value[], W），输出是 int
        JudgeEngine<Object[], Integer> engine = new JudgeEngine<>();
        
        // 添加测试用例
        engine
            .addTestCase("基本例子", new Object[]{new int[]{1,3,4}, new int[]{15,20,30}, 4}, 35, "选物品 1 和 3")
            .addTestCase("全装下", new Object[]{new int[]{1,1,1}, new int[]{10,20,30}, 5}, 60, "都可以装下")
            .addTestCase("容量为 0", new Object[]{new int[]{1,2,3}, new int[]{10,20,30}, 0}, 0, "无法装任何东西");
        
        // 执行判题 - 基础 DP
       System.out.println("=== 基础 DP 方法测试 ===");
    List<JudgeResult> results1 = engine.judge(input -> {
            int[] weight = (int[]) input[0];
            int[] value = (int[]) input[1];
            int W = (Integer) input[2];
          return solution.knapsack(weight, value, W);
        });
        JudgeReporter.printReport(results1);
        
        // 执行判题 - 空间优化版
       System.out.println("\n=== 空间优化版测试 ===");
    List<JudgeResult> results2 = engine.judge(input -> {
            int[] weight = (int[]) input[0];
            int[] value = (int[]) input[1];
            int W = (Integer) input[2];
          return solution.knapsackOptimized(weight, value, W);
        });
        JudgeReporter.printReport(results2);
        
        // 统计结果
        boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted)
            && results2.stream().allMatch(JudgeResult::isAccepted);
       System.exit(allPassed ? 0 : 1);
    }
}
