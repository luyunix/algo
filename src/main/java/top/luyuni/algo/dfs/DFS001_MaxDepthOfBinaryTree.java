package top.luyuni.algo.dfs;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DFS001 - 二叉树的最大深度
 */
public class DFS001_MaxDepthOfBinaryTree {

    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int x) {
            val = x;
        }
    }

    public int maxDepth(TreeNode root) {
        if (root == null) return 0;
        return Math.max(maxDepth(root.left), maxDepth(root.right)) + 1;
    }

    public static void main(String[] args) {
        DFS001_MaxDepthOfBinaryTree solution = new DFS001_MaxDepthOfBinaryTree();

        JudgeEngine<TreeNode, Integer> engine = new JudgeEngine<>();

        TreeNode root1 = new TreeNode(3);
        root1.left = new TreeNode(9);
        root1.right = new TreeNode(20);
        root1.right.left = new TreeNode(15);
        root1.right.right = new TreeNode(7);

        engine.addTestCase("示例 1", root1, 3, "深度为 3")
                .addTestCase("空树", null, 0, "空树深度为 0")
                .addTestCase("单节点", new TreeNode(1), 1, "只有一个节点");

        System.out.println("=== 二叉树最大深度测试 ===");
        List<JudgeResult> results = engine.judge(input -> solution.maxDepth(input));
        JudgeReporter.printReport(results);

        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
}
