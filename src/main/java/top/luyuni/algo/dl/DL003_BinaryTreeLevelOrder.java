package top.luyuni.algo.dl;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * DL003 - 二叉树的层序遍历
 */
public class DL003_BinaryTreeLevelOrder {

 static class TreeNode {
     int val;
     TreeNode left;
     TreeNode right;
   TreeNode(int x) { val = x; }
 }

 public List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;

   Queue<TreeNode> queue = new LinkedList<>();
   queue.offer(root);

   while (!queue.isEmpty()) {
     int size = queue.size();
     List<Integer> level = new ArrayList<>();

    for (int i = 0; i < size; i++) {
       TreeNode node = queue.poll();
      level.add(node.val);

      if (node.left != null) {
        queue.offer(node.left);
       }
      if (node.right != null) {
        queue.offer(node.right);
       }
     }
    result.add(level);
   }
 return result;
    }

 public static void main(String[] args) {
    DL003_BinaryTreeLevelOrder solution = new DL003_BinaryTreeLevelOrder();

    JudgeEngine<TreeNode, List<List<Integer>>> engine = new JudgeEngine<>();

   TreeNode root1 = new TreeNode(3);
   root1.left = new TreeNode(9);
   root1.right = new TreeNode(20);
   root1.right.left = new TreeNode(15);
   root1.right.right = new TreeNode(7);

  engine.addTestCase("示例 1", root1,
        Arrays.asList(Arrays.asList(3), Arrays.asList(9,20), Arrays.asList(15,7)),
        "层序遍历",
        (expected, actual) -> listsEqual(expected, actual))
        .addTestCase("空树", null, new ArrayList<>(), "空树返回空列表",
        (expected, actual) -> listsEqual(expected, actual))
        .addTestCase("单节点", new TreeNode(1),
        Arrays.asList(Arrays.asList(1)), "只有一个节点",
        (expected, actual) -> listsEqual(expected, actual));

   System.out.println("=== 二叉树层序遍历测试 ===");
   List<JudgeResult> results = engine.judge(input -> solution.levelOrder(input));
    JudgeReporter.printReport(results);

   boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
  System.exit(allPassed ? 0 : 1);
    }

 private static boolean listsEqual(List<List<Integer>> a, List<List<Integer>> b) {
   if (a.size() != b.size()) return false;
   for (int i = 0; i < a.size(); i++) {
    if (!a.get(i).equals(b.get(i))) return false;
   }
 return true;
   }
}
