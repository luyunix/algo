package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DT002 - 爬楼梯
 */
public class DT002_ClimbStairs {

 public int climbStairs(int n) {
    if (n <= 2) return n;
      int[] dp = new int[n + 1];
      dp[1] = 1;
      dp[2] = 2;
      for (int i = 3; i <= n; i++) {
          dp[i] = dp[i - 1] + dp[i - 2];
      }
   return dp[n];
    }

 public int climbStairsOptimized(int n) {
    if (n <= 2) return n;
       int prev2 = 1, prev1 = 2;
       for (int i = 3; i <= n; i++) {
           int curr = prev1 + prev2;
         prev2 = prev1;
         prev1 = curr;
       }
    return prev1;
    }

 public static void main(String[] args) {
        DT002_ClimbStairs solution = new DT002_ClimbStairs();

        JudgeEngine<Integer, Integer> engine = new JudgeEngine<>();
      engine.addTestCase("n=1", 1, 1, "只有 1 阶")
            .addTestCase("n=2", 2,2, "2 阶楼梯")
            .addTestCase("n=3", 3, 3, "3 阶楼梯")
            .addTestCase("n=4", 4, 5, "4 阶楼梯")
            .addTestCase("n=5", 5, 8, "5 阶楼梯")
            .addTestCase("n=10", 10, 89, "10 阶楼梯")
            .addTestCase("n=45", 45, 1836311903, "大数值测试");

    System.out.println("=== 基础 DP 方法测试 ===");
    List<JudgeResult> results1 = engine.judge(input -> solution.climbStairs(input));
      JudgeReporter.printReport(results1);

   System.out.println("\n=== 空间优化方法测试 ===");
   List<JudgeResult> results2 = engine.judge(input -> solution.climbStairsOptimized(input));
     JudgeReporter.printReport(results2);

     boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted) &&
                      results2.stream().allMatch(JudgeResult::isAccepted);
   System.exit(allPassed ? 0 : 1);
    }
}
