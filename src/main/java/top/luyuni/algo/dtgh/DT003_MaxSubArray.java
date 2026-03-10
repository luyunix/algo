package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DT003 - 最大子数组和
 */
public class DT003_MaxSubArray {
    
 public int maxSubArray(int[] nums) {
     int n = nums.length;
     int[] dp = new int[n];
     dp[0] = nums[0];
     int maxSum = dp[0];
     for (int i = 1; i < n; i++) {
         dp[i] = Math.max(dp[i - 1] + nums[i], nums[i]);
         maxSum = Math.max(maxSum, dp[i]);
     }
   return maxSum;
    }
    
 public int maxSubArrayKadane(int[] nums) {
     int maxSum = nums[0];
     int currentSum = nums[0];
     for (int i = 1; i < nums.length; i++) {
         currentSum = Math.max(currentSum + nums[i], nums[i]);
         maxSum = Math.max(maxSum, currentSum);
     }
   return maxSum;
    }
    
 public static void main(String[] args) {
        DT003_MaxSubArray solution = new DT003_MaxSubArray();
        
        JudgeEngine<int[], Integer> engine = new JudgeEngine<>();
      engine.addTestCase("经典例子", new int[]{-2,1,-3,4,-1,2,1,-5,4}, 6, "子数组 [4,-1,2,1]")
            .addTestCase("单元素", new int[]{1}, 1, "只有一个元素")
            .addTestCase("全正数", new int[]{5,4,-1,7,8}, 23, "整个数组")
            .addTestCase("全负数", new int[]{-5,-4,-3,-2,-1}, -1, "最大的单个元素")
            .addTestCase("混合", new int[]{-2,-1}, -1, "两个元素")
            .addTestCase("大正数", new int[]{1,2,3,4,5}, 15, "整个数组");
        
    System.out.println("=== 基础 DP 方法测试 ===");
    List<JudgeResult> results1 = engine.judge(input -> solution.maxSubArray(input));
      JudgeReporter.printReport(results1);
      
   System.out.println("\n=== Kadane 算法测试 ===");
   List<JudgeResult> results2 = engine.judge(input -> solution.maxSubArrayKadane(input));
     JudgeReporter.printReport(results2);
      
     boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted) &&
                      results2.stream().allMatch(JudgeResult::isAccepted);
   System.exit(allPassed ? 0 : 1);
    }
}
