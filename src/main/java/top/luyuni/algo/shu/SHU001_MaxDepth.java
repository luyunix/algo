package top.luyuni.algo.shu;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * ============================================================
 * 【SHU001 - 二叉树的最大深度】
 * ============================================================
 * 
 * 题目描述：
 * 给定一个二叉树 root，返回其最大深度。
 * 二叉树的最大深度是指从根节点到最远叶子节点的最长路径上的节点数。
 * 
 * 示例 1：
 * 输入：root = [3,9,20,null,null,15,7]
 * 输出：3
 * 解释：
 *      3
 *      / \
 *     9  20
 *       /  \
 *    15   7
 * 最长路径：3→20→15 或 3→20→7，共 3 个节点
 * 
 * 示例 2：
 * 输入：root = [1,null,2]
 * 输出：2
 * 
 * 示例 3：
 * 输入：root = []
 * 输出：0
 * 
 * 示例 4：
 * 输入：root = [0]
 * 输出：1
 */
public class SHU001_MaxDepth {
    
    /**
     * 二叉树节点定义
     */
  public static class TreeNode {
       int val;
        TreeNode left;
        TreeNode right;
        
       TreeNode(int val) {
          this.val = val;
        }
    }
    
    /**
     * 方法 1：递归（后序遍历）
     * 
     * @param root 二叉树根节点
     * @return 最大深度
     */
  public int maxDepth(TreeNode root) {
      if (root == null) {
         return 0;
       }
       
      // 递归计算左右子树的最大深度
    int leftDepth = maxDepth(root.left);
    int rightDepth = maxDepth(root.right);
     
     // 返回较大的深度 +1（当前节点）
   return Math.max(leftDepth, rightDepth) +1;
   }
   
   /**
    * 方法2：层序遍历（BFS）
    */
 public int maxDepthBFS(TreeNode root) {
   if (root == null) {
      return 0;
    }
    
   Queue<TreeNode> queue = new LinkedList<>();
  queue.offer(root);
  
 int depth = 0;
 while (!queue.isEmpty()) {
    int levelSize = queue.size();
    
  for (int i = 0; i < levelSize; i++) {
      TreeNode node = queue.poll();
    if (node.left != null) {
         queue.offer(node.left);
      }
    if (node.right != null) {
         queue.offer(node.right);
      }
  }
  depth++;
 }
 
 return depth;
 }
 
 /**
  * 方法3：迭代（用栈模拟递归）
  */
 public int maxDepthIterative(TreeNode root) {
   if (root == null) {
     return 0;
    }
    
   Stack<TreeNode> stack = new Stack<>();
   Stack<Integer> depthStack = new Stack<>();
   stack.push(root);
   depthStack.push(1);
   
  int maxDepth = 0;
 while (!stack.isEmpty()) {
    TreeNode node = stack.pop();
  int currentDepth = depthStack.pop();
   
  maxDepth = Math.max(maxDepth, currentDepth);
  
 if (node.right != null) {
    stack.push(node.right);
   depthStack.push(currentDepth +1);
 }
 if (node.left != null) {
    stack.push(node.left);
   depthStack.push(currentDepth + 1);
 }
 }
 
 return maxDepth;
 }

    /**
     * 从数组构建二叉树（层序遍历顺序）
     * null 表示空节点
     */
  public static TreeNode buildTree(Integer[] arr) {
      if (arr == null || arr.length == 0 || arr[0] == null) return null;
        
       TreeNode root = new TreeNode(arr[0]);
       Queue<TreeNode> queue = new LinkedList<>();
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
   
   // ============ OJ 判题框架 ============
  
  /**
   * 使用 oj/core 工具进行评测
   */
 public static void main(String[] args) {
      SHU001_MaxDepth solution = new SHU001_MaxDepth();
      
      // 创建判题引擎，输入是 Integer[]（树的数组表示），输出是 int
     JudgeEngine<Integer[], Integer> engine = new JudgeEngine<>();
      
      // 添加测试用例
      engine
         .addTestCase("示例 1", new Integer[]{3, 9, 20, null, null, 15, 7}, 3, "深度 3 的二叉树")
         .addTestCase("示例 2", new Integer[]{1, null, 2}, 2, "深度 2 的链状树")
         .addTestCase("示例 3", new Integer[]{}, 0, "空树")
         .addTestCase("示例 4", new Integer[]{0}, 1, "单节点")
         .addTestCase("链状树", new Integer[]{1, 2, null, 3, null, 4, null, 5}, 5, "深度 5 的链状树")
         .addTestCase("满二叉树", new Integer[]{1, 2, 3, 4, 5, 6, 7}, 3, "深度 3 的满二叉树");
      
      // 执行判题 - 递归方法
    System.out.println("=== 递归方法测试 ===");
   List<JudgeResult> results1 = engine.judge(input -> {
         TreeNode root = buildTree(input);
      return solution.maxDepth(root);
     });
    JudgeReporter.printReport(results1);
     
    // 执行判题 - BFS方法
   System.out.println("\n=== BFS方法测试 ===");
  List<JudgeResult> results2 = engine.judge(input -> {
        TreeNode root = buildTree(input);
     return solution.maxDepthBFS(root);
    });
   JudgeReporter.printReport(results2);
    
   // 执行判题 - 迭代方法
  System.out.println("\n=== 迭代方法测试 ===");
  List<JudgeResult> results3 = engine.judge(input -> {
       TreeNode root = buildTree(input);
    return solution.maxDepthIterative(root);
   });
  JudgeReporter.printReport(results3);
   
   // 统计结果
   boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted)
       && results2.stream().allMatch(JudgeResult::isAccepted)
       && results3.stream().allMatch(JudgeResult::isAccepted);
  System.exit(allPassed ? 0 : 1);
 }
}
