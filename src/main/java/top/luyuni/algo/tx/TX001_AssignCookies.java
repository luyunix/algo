package top.luyuni.algo.tx;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * ============================================================
 * 【TX001- 分发饼干】贪心算法入门
 * ============================================================
 *
 * 题目链接：https://leetcode.cn/problems/assign-cookies/
 *
 * 题目描述：
 * 假设你是一位很棒的家长，想要给你的孩子们一些小饼干。但是，每个孩子最多只能给一块饼干。
 * 对每个孩子 i，都有一个胃口值 g[i]，这是能让孩子们满足胃口的饼干的最小尺寸；
 * 并且每块饼干 j，都有一个尺寸 s[j] 。
 * 如果 s[j] >= g[i]，我们可以将这个饼干 j 分配给孩子 i ，这个孩子会得到满足。
 * 你的目标是尽可能满足越多数量的孩子，并输出这个最大数值。
 *
 * 示例 1：
 * 输入：g = [1,2,3], s = [1,1]
 * 输出：1
 * 解释：你有三个孩子和两块小饼干，3 个孩子的胃口值分别是：1,2,3。
 * 虽然你有两块小饼干，由于他们的尺寸都是 1，你只能让胃口值是 1 的孩子满足。
 *
 * 示例 2：
 * 输入：g = [1,2], s = [1,2,3]
 * 输出：2
 * 解释：你有两个孩子和三块小饼干，2 个孩子的胃口值分别是 1,2。
 * 你拥有的小饼干足够大，能够使得所有孩子都满足。
 */
public class TX001_AssignCookies {

    public int findContentChildren(int[] g, int[] s) {
        // 贪心策略：排序后，用最小的饼干满足最小的胃口
        java.util.Arrays.sort(g); // 孩子胃口排序
        java.util.Arrays.sort(s); // 饼干尺寸排序

        int childIndex = 0; // 当前满足的孩子索引
        int cookieIndex = 0; // 当前饼干索引

        while (childIndex < g.length && cookieIndex < s.length) {
           if (s[cookieIndex] >= g[childIndex]) {
                // 当前饼干可以满足当前孩子
                childIndex++;
            }
            // 无论是否满足，都尝试下一个饼干
            cookieIndex++;
        }

       return childIndex; // 返回满足的孩子数量
    }

    // ============ OJ 判题框架 ============

   /**
    * 使用 oj/core 工具进行评测
    */
   public static void main(String[] args) {
       TX001_AssignCookies solution= new TX001_AssignCookies();

       // 创建判题引擎，输入是 int[][]（g 数组和 s 数组），输出是 int
       JudgeEngine<int[][], Integer> engine = new JudgeEngine<>();

       // 添加测试用例
       engine
           .addTestCase("示例 1", new int[][]{{1,2,3}, {1,1}}, 1, "只有胃口 1 的孩子被满足")
           .addTestCase("示例 2", new int[][]{{1,2}, {1,2,3}}, 2, "所有孩子都被满足")
           .addTestCase("胃口都很大", new int[][]{{10,9,8,7}, {5,6,7,8}}, 2, "只能满足 2 个孩子")
           .addTestCase("没有饼干", new int[][]{{1,2,3}, {}}, 0, "没有饼干，无法满足")
           .addTestCase("没有孩子", new int[][]{{}, {1,2,3}}, 0, "没有孩子，无需分配")
           .addTestCase("单个孩子", new int[][]{{5}, {5}}, 1, "刚好满足");

       // 执行判题
       System.out.println("=== 分发饼干测试 ===");
       List<JudgeResult> results = engine.judge(input -> {
           int[] g = input[0];
           int[] s = input[1];
         return solution.findContentChildren(g, s);
       });

       JudgeReporter.printReport(results);

       // 统计结果
       boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
       System.exit(allPassed ? 0 : 1);
   }
}
