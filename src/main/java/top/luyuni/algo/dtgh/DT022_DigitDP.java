package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DT022 - 数位DP（数字 1 的个数）
 *
 * 题目：给定一个整数 n，计算所有小于等于 n 的非负整数中数字 1 出现的个数。
 *
 * 本题是经典的数位DP问题：
 * - 统计数字中某一位的出现次数
 * - 使用记忆化搜索或递推求解
 * - 考虑每一位的贡献
 *
 * 示例：
 * 输入: n = 13
 * 输出: 6
 * 解释: 1, 10, 11, 12, 13 中数字 1 出现 6 次（11有两个1）
 *
 * 来源：LeetCode 233
 */
public class DT022_DigitDP {

    private int[] digits;
    private int[][] memo;

    /**
     * 数位DP - 记忆化搜索
     * @param pos 当前处理的位置
     * @param count 已经出现1的个数
     * @param tight 是否受限制（前面是否紧贴n的对应位）
     * @return 从pos位开始到结束，count个1的方案数
     */
    private int dfs(int pos, int count, boolean tight) {
        if (pos == digits.length) {
            return count; // 到达末尾，返回1的个数
        }

        int t = tight ? 1 : 0;
        if (memo[pos][count] != -1 && !tight) {
            return memo[pos][count];
        }

        int limit = tight ? digits[pos] : 9;
        int ans = 0;

        for (int i = 0; i <= limit; i++) {
            int newCount = count + (i == 1 ? 1 : 0);
            boolean newTight = tight && (i == limit);
            ans += dfs(pos + 1, newCount, newTight);
        }

        if (!tight) {
            memo[pos][count] = ans;
        }

        return ans;
    }

    /**
     * 数位DP解法
     */
    public int countDigitOneDP(int n) {
        if (n <= 0) return 0;

        String s = String.valueOf(n);
        digits = new int[s.length()];
        for (int i = 0; i < s.length(); i++) {
            digits[i] = s.charAt(i) - '0';
        }

        // memo[pos][count]: 在pos位置，已经有count个1的方案数
        memo = new int[digits.length][digits.length + 1];
        for (int i = 0; i < digits.length; i++) {
            for (int j = 0; j <= digits.length; j++) {
                memo[i][j] = -1;
            }
        }

        return dfs(0, 0, true);
    }

    /**
     * 数学解法 - 计算每一位的贡献
     * 对于第i位（从低位到高位）：
     * - higher = n / (digit * 10)
     * - current = (n / digit) % 10
     * - lower = n % digit
     */
    public int countDigitOne(int n) {
        long count = 0;
        long digit = 1; // 当前位（1, 10, 100, ...）

        while (digit <= n) {
            long higher = n / (digit * 10);  // 高位数字
            long current = (n / digit) % 10; // 当前位数字
            long lower = n % digit;          // 低位数字

            if (current == 0) {
                // 当前位为0，1的出现次数由高位决定
                count += higher * digit;
            } else if (current == 1) {
                // 当前位为1，1的出现次数由高位和低位共同决定
                count += higher * digit + lower + 1;
            } else {
                // 当前位大于1，1的出现次数由高位决定
                count += (higher + 1) * digit;
            }

            digit *= 10;
        }

        return (int) count;
    }

    public static void main(String[] args) {
        DT022_DigitDP solution = new DT022_DigitDP();

        JudgeEngine<Integer, Integer> engine = new JudgeEngine<>();
        engine.addTestCase("n=13", 13, 6, "1,10,11,12,13共6个1")
                .addTestCase("n=0", 0, 0, "0没有1")
                .addTestCase("n=1", 1, 1, "1有1个1")
                .addTestCase("n=10", 10, 2, "1和10")
                .addTestCase("n=100", 100, 21, "1-100有21个1")
                .addTestCase("n=999", 999, 300, "1-999有300个1");

        System.out.println("=== 数位DP解法测试 ===");
        List<JudgeResult> results1 = engine.judge(input -> solution.countDigitOneDP(input));
        JudgeReporter.printReport(results1);

        System.out.println("\n=== 数学解法测试 ===");
        List<JudgeResult> results2 = engine.judge(input -> solution.countDigitOne(input));
        JudgeReporter.printReport(results2);

        boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted) &&
                results2.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
}
