package top.luyuni.algo.lb;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * LB006 - 相交链表
 * 
 * 题目描述：
 * 给你两个单链表的头节点 headA 和 headB ，请你找出并返回两个单链表相交的起始节点。
 * 如果两个链表不存在相交节点，返回 null 。
 * 
 * 示例：
 * 输入：intersectVal = 8, listA = [4,1,8,4,5], listB = [5,6,1,8,4,5], skipA = 2, skipB = 3
 * 输出：Intersected at '8'
 * 
 * 核心技巧：双指针
 * - 两个指针分别遍历两个链表
 * - 到达末尾后切换到另一个链表的头
 * - 如果相交，会在交点相遇；如果不相交，会同时到达 null
 */
public class LB006_IntersectionNode {
    
    // 链表节点定义
  public static class ListNode {
        int val;
      ListNode next;
     public ListNode(int val) { this.val = val; }
   }
    
    /**
     * 解法：双指针（浪漫相遇法）
     * 时间复杂度：O(n+m)
     * 空间复杂度：O(1)
     */
 public ListNode getIntersectionNode(ListNode headA, ListNode headB) {
    if (headA == null || headB == null) return null;
       
       // 双指针
    ListNode pA = headA;
    ListNode pB = headB;
      
      // 如果相交，会在交点相遇；如果不相交，会同时到达 null
   while(pA != pB) {
        // pA 走到末尾后切换到 headB
      pA = (pA == null) ? headB : pA.next;
        // pB 走到末尾后切换到 headA
     pB = (pB == null) ? headA : pB.next;
      }
      
 return pA; // 返回交点或 null
    }
    
    /**
     * 解法 2：计算长度差
     */
  public ListNode getIntersectionNodeByLength(ListNode headA, ListNode headB) {
       // TODO: 请实现此方法（可选）
      return null;
   }
   
 public static void main(String[] args) {
     LB006_IntersectionNode solution = new LB006_IntersectionNode();
     
     // 创建判题引擎
  JudgeEngine<TestInput, ListNode> engine = new JudgeEngine<>();
     
     // 注意：由于相交链表的特殊性（共享节点引用），我们使用自定义比较器
    // 测试用例 1：相交链表
  ListNode common1 = createList(new int[]{8, 4,5});
  ListNode listA1 = createList(new int[]{4, 1});
   listA1.next.next = common1;
  ListNode listB1 = createList(new int[]{5, 6, 1});
   listB1.next.next.next = common1;
   
   engine.addTestCase("相交链表",
   new TestInput(listA1, listB1),
      common1,
      "基本功能测试：相交于值为 8 的节点",
     (expected, actual) -> (expected == actual));
   
   // 测试用例 2：不相交链表
  ListNode listC = createList(new int[]{1,2, 3});
  ListNode listD = createList(new int[]{4, 5, 6});
   
   engine.addTestCase("不相交链表",
  new TestInput(listC, listD),
     null,
    "边界情况：不相交",
     (expected, actual) -> (expected == actual));
   
   // 测试用例 3：一个链表为空
  ListNode emptyList = null;
  ListNode nonEmptyList = createList(new int[]{1,2});
   
   engine.addTestCase("一个为空",
  new TestInput(emptyList, nonEmptyList),
     null,
    "边界情况：空链表",
     (expected, actual) -> (expected == actual));
   
   // 测试用例 4：完全相同的链表（从头开始相交）
  ListNode sameList = createList(new int[]{1,2,3});
   
   engine.addTestCase("完全相同",
  new TestInput(sameList, sameList),
     sameList,
    "边界情况：完全相同的链表",
     (expected, actual) -> (expected == actual));
   
   // 执行判题
 System.out.println("=== 双指针法测试 ===");
  List<JudgeResult> results = engine.judge(input -> 
     solution.getIntersectionNode(input.headA, input.headB)
  );
 JudgeReporter.printReport(results);
  
  // 统计结果
 boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
 System.exit(allPassed ? 0 : 1);
 }
 
 // 辅助方法：比较两个链表是否相等（用于不相交的情况）
private static boolean listEquals(ListNode l1, ListNode l2) {
 while(l1 != null && l2 != null) {
 if (l1.val != l2.val) return false;
   l1 = l1.next;
    l2 = l2.next;
  }
 return l1 == null && l2 == null;
}

// 辅助方法：从数组创建链表
private static ListNode createList(int[] arr) {
 if (arr == null || arr.length == 0) return null;
 ListNode dummy = new ListNode(0);
 ListNode curr = dummy;
 for (int val : arr) {
  curr.next = new ListNode(val);
  curr = curr.next;
 }
 return dummy.next;
}

// 测试输入类
static class TestInput {
 ListNode headA;
 ListNode headB;
 TestInput(ListNode headA, ListNode headB) {
   this.headA = headA;
  this.headB = headB;
 }
}
}
