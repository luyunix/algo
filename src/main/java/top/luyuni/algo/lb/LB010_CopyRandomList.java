package top.luyuni.algo.lb;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

import java.util.HashMap;
import java.util.Map;

/**
 * LB010 - 复制带随机指针的链表
 *
 * 题目描述：
 * 给你一个长度为 n 的链表，每个节点包含一个额外增加的随机指针 random ，
 * 该指针可以指向链表中的任何节点或空节点。
 * 构造这个链表的深拷贝。
 *
 * 示例：
 * 输入：head = [[7,null],[13,0],[11,4],[10,2],[1,0]]
 * 输出：[[7,null],[13,0],[11,4],[10,2],[1,0]]
 *
 * 核心技巧：
 * 1. 哈希表法：记录原节点到新节点的映射
 * 2. 原地插入法：在每个原节点后面插入新节点
 */
public class LB010_CopyRandomList {

    // 节点定义
  static class Node {
        int val;
      Node next;
      Node random;

      public Node(int val) {
           this.val = val;
           this.next = null;
           this.random = null;
        }
   }

   /**
     * 解法 1：哈希表法
     * 时间复杂度：O(n)
     * 空间复杂度：O(n)
     */
 public Node copyRandomList(Node head) {
 if (head == null) return null;

 Map<Node, Node> map = new HashMap<>();

      // 第一次遍历：创建所有新节点并建立映射
   Node curr = head;
   while(curr != null) {
      map.put(curr, new Node(curr.val));
      curr = curr.next;
   }

      // 第二次遍历：设置 next 和 random 指针
   curr = head;
   while(curr != null) {
     Node newNode = map.get(curr);
     newNode.next = map.get(curr.next);
     newNode.random = map.get(curr.random);
      curr = curr.next;
   }

 return map.get(head);
 }

    /**
     * 解法2：原地插入法（空间复杂度O(1)）
     */
    public Node copyRandomListO1(Node head) {
        // TODO: 请实现此方法（可选）
        return null;
    }

    public static void main(String[] args) {
    LB010_CopyRandomList solution = new LB010_CopyRandomList();

    // 创建判题引擎
  JudgeEngine<TestInput, Node> engine = new JudgeEngine<>();

    // 添加测试用例
   engine.addTestCase("基本功能",
        new TestInput(createTestList1()),
          createExpectedList1(),
         "5 个节点的链表",
         (expected, actual) -> nodeListEquals(expected, actual))
     .addTestCase("单个节点",
        new TestInput(createSingleNodeList()),
          createSingleNodeList(),
         "边界情况：单个节点",
         (expected, actual) -> nodeListEquals(expected, actual))
     .addTestCase("空链表",
        new TestInput(null),
          null,
         "边界情况：空链表");

    // 执行判题
  System.out.println("=== 复制带随机指针的链表 ===");
  List<JudgeResult> results = engine.judge(input ->
       solution.copyRandomList(input.head)
  );
 JudgeReporter.printReport(results);

    // 统计结果
   boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
  System.exit(allPassed ? 0 : 1);
 }

 // 辅助方法：比较两个带随机指针的链表是否相等
 private static boolean nodeListEquals(Node expected, Node actual) {
  if ((expected == null) != (actual == null)) return false;
  if (expected == null) return true;

  Node e = expected;
  Node a = actual;

  while(e != null && a != null) {
  if (e.val != a.val) return false;
  if ((e.random == null && a.random != null) ||
      (e.random != null && a.random == null) ||
      (e.random != null && a.random != null && e.random.val != a.random.val)) {
 return false;
  }
  e = e.next;
  a = a.next;
  }

 return e == null && a == null;
 }

 // 辅助方法：创建测试链表 [[7,null],[13,0],[11,4],[10,2],[1,0]]
private static Node createTestList1() {
 Node head = new Node(7);
 Node node2 = new Node(13);
 Node node3 = new Node(11);
 Node node4 = new Node(10);
 Node node5 = new Node(1);

 head.next = node2;
 node2.next = node3;
 node3.next = node4;
 node4.next = node5;

 head.random = null;
 node2.random = head;      // 指向 7
 node3.random = node5;     // 指向 1
 node4.random = node3;     // 指向 11
 node5.random = head;      // 指向 7

 return head;
}

// 辅助方法：创建期望的链表（结构相同但节点不同）
private static Node createExpectedList1() {
 Node head = new Node(7);
 Node node2 = new Node(13);
 Node node3 = new Node(11);
 Node node4 = new Node(10);
 Node node5 = new Node(1);

 head.next = node2;
 node2.next = node3;
 node3.next = node4;
 node4.next = node5;

 head.random = null;
 node2.random = head;      // 指向 7
 node3.random = node5;     // 指向 1
 node4.random = node3;     // 指向 11
 node5.random = head;      // 指向 7

 return head;
}

// 辅助方法：创建单个节点的链表
private static Node createSingleNodeList() {
 Node node = new Node(5);
 node.random = node;  // random 指向自己
 return node;
}

// 测试输入类
static class TestInput {
 Node head;
 TestInput(Node head) {
   this.head = head;
 }
}
}
