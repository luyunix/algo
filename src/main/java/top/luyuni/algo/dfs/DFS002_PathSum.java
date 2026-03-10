package top.luyuni.algo.dfs;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DFS002 - 路径总和
 */
public class DFS002_PathSum {
    
 static class TreeNode {
     int val;
     TreeNode left;
     TreeNode right;
   TreeNode(int x) { val = x; }
 }
    
 public boolean hasPathSum(TreeNode root, int targetSum) {
    if (root == null) return false;
    if (root.left == null && root.right == null) {
      return root.val == targetSum;
     }
    int remaining = targetSum - root.val;
   return hasPathSum(root.left, remaining) || hasPathSum(root.right, remaining);
    }
    
 public static void main(String[] args) {
    DFS002_PathSum solution = new DFS002_PathSum();
    
    JudgeEngine<TestInput, Boolean> engine = new JudgeEngine<>();
    
   TreeNode root = new TreeNode(5);
   root.left = new TreeNode(4);
   root.right = new TreeNode(8);
   root.left.left = new TreeNode(11);
   root.right.left = new TreeNode(13);
   root.right.right = new TreeNode(4);
   root.left.left.left = new TreeNode(7);
   root.left.left.right = new TreeNode(2);
   root.right.right.right = new TreeNode(1);
    
  engine.addTestCase("示例 1", new TestInput(root, 22), true, "5→4→11→2")
        .addTestCase("不存在", new TestInput(root, 100), false, "没有和为 100 的路径")
        .addTestCase("空树", new TestInput(null, 0), false, "空树没有路径")
        .addTestCase("单节点", new TestInput(new TreeNode(5), 5), true, "根节点值等于 targetSum");
    
   System.out.println("=== 路径总和测试 ===");
   List<JudgeResult> results = engine.judge(input -> 
         solution.hasPathSum(input.root, input.targetSum)
     );
    JudgeReporter.printReport(results);
    
   boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
  System.exit(allPassed ? 0 : 1);
    }
    
 static class TestInput {
     TreeNode root;
     int targetSum;
   TestInput(TreeNode root, int targetSum) {
       this.root = root;
       this.targetSum = targetSum;
     }
 }
}
