package top.luyuni.algo.dtgh;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DT020 - 树形DP（打家劫舍 III）
 *
 * 题目：小偷又发现了一个新的可行窃的地区。这个地区只有一个入口，我们称之为 root 。
 * 除了 root 之外，每栋房子有且只有一个“父“房子与之相连。一番侦察之后，聪明的小偷意识到
 * “这个地方的所有房屋的排列类似于一棵二叉树”。如果两个直接相连的房子在同一天晚上被打劫，房屋将自动报警。
 * 给定二叉树的 root 。返回在不触动警报的情况下 ，小偷能够盗取的最高金额 。
 *
 * 本题是经典的树形DP问题：
 * - 每个节点选或不选
 * - 选了当前节点，子节点不能选
 * - 不选当前节点，子节点可选可不选
 *
 * 示例：
 *      3
 *     / \
 *    2   3
 *     \   \
 *      3   1
 * 输入: root = [3,2,3,null,3,null,1]
 * 输出: 7
 * 解释: 选中节点 3 + 3 + 1 = 7
 *
 * 来源：LeetCode 337
 */
public class DT020_TreeDP {

    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int val) {
            this.val = val;
        }
    }

    /**
     * DP解法
     * 返回数组res[2]:
     * res[0] = 不选当前节点的最大金额
     * res[1] = 选当前节点的最大金额
     */
    public int rob(TreeNode root) {
        int[] result = robDFS(root);
        return Math.max(result[0], result[1]);
    }

    private int[] robDFS(TreeNode node) {
        if (node == null) {
            return new int[]{0, 0};
        }

        int[] left = robDFS(node.left);
        int[] right = robDFS(node.right);

        // 不选当前节点：子节点可选可不选
        int notRob = Math.max(left[0], left[1]) + Math.max(right[0], right[1]);
        // 选当前节点：子节点不能选
        int rob = node.val + left[0] + right[0];

        return new int[]{notRob, rob};
    }

    // 辅助方法：从数组构建二叉树
    public static TreeNode buildTree(Integer[] arr) {
        if (arr == null || arr.length == 0 || arr[0] == null) return null;

        TreeNode root = new TreeNode(arr[0]);
        java.util.Queue<TreeNode> queue = new java.util.LinkedList<>();
        queue.offer(root);

        int i = 1;
        while (!queue.isEmpty() && i < arr.length) {
            TreeNode node = queue.poll();

            // 左子节点
            if (i < arr.length && arr[i] != null) {
                node.left = new TreeNode(arr[i]);
                queue.offer(node.left);
            }
            i++;

            // 右子节点
            if (i < arr.length && arr[i] != null) {
                node.right = new TreeNode(arr[i]);
                queue.offer(node.right);
            }
            i++;
        }

        return root;
    }

    public static void main(String[] args) {
        DT020_TreeDP solution = new DT020_TreeDP();

        JudgeEngine<TreeNode, Integer> engine = new JudgeEngine<>();
        engine.addTestCase("[3,2,3,null,3,null,1]",
                buildTree(new Integer[]{3, 2, 3, null, 3, null, 1}), 7, "3+3+1=7")
                .addTestCase("[3,4,5,1,3,null,1]",
                        buildTree(new Integer[]{3, 4, 5, 1, 3, null, 1}), 9, "4+5=9")
                .addTestCase("[1]",
                        buildTree(new Integer[]{1}), 1, "单节点")
                .addTestCase("[2,1,3,null,4]",
                        buildTree(new Integer[]{2, 1, 3, null, 4}), 7, "4+3=7")
                .addTestCase("[]",
                        buildTree(new Integer[]{}), 0, "空树");

        System.out.println("=== 树形DP解法测试 ===");
        List<JudgeResult> results = engine.judge(input -> solution.rob(input));
        JudgeReporter.printReport(results);

        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
}
