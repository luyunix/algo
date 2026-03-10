package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DT001 - 斐波那契数列
 */
public class DT001_Fibonacci {

    public int fib(int n) {
        if (n <= 1) return n;
        int[] dp = new int[n + 1];
        dp[0] = 0;
        dp[1] = 1;
        for (int i = 2; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        return dp[n];
    }

    public int fibOptimized(int n) {
        if (n <= 1) return n;
        int prev2 = 0, prev1 = 1;
        for (int i = 2; i <= n; i++) {
            int curr = prev1 + prev2;
            prev2 = prev1;
            prev1 = curr;
        }
        return prev1;
    }

    public static void main(String[] args) {
        DT001_Fibonacci solution = new DT001_Fibonacci();

        JudgeEngine<Integer, Integer> engine = new JudgeEngine<>();
        engine.addTestCase("fib(0)", 0, 0, "初始条件")
                .addTestCase("fib(1)", 1, 1, "初始条件")
                .addTestCase("fib(2)", 2, 1, "1+0")
                .addTestCase("fib(3)", 3, 2, "1+1")
                .addTestCase("fib(4)", 4, 3, "2+1")
                .addTestCase("fib(5)", 5, 5, "3+2")
                .addTestCase("fib(10)", 10, 55, "中等数值")
                .addTestCase("fib(30)", 30, 832040, "大数值");

        System.out.println("=== 基础 DP 方法测试 ===");
        List<JudgeResult> results1 = engine.judge(input -> solution.fib(input));
        JudgeReporter.printReport(results1);

        System.out.println("\n=== 空间优化方法测试 ===");
        List<JudgeResult> results2 = engine.judge(input -> solution.fibOptimized(input));
        JudgeReporter.printReport(results2);

        boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted) &&
                results2.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
}
