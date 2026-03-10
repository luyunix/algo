package top.luyuni.algo.lb;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * LB007 - 回文链表
 *
 * 题目描述：
 * 给你一个单链表的头节点 head ，请你判断该链表是否为回文链表。
 * 如果是，返回 true ；否则，返回 false 。
 *
 * 示例：
 * 输入：head = [1,2,2,1]
 * 输出：true
 *
 * 输入：head = [1,2]
 * 输出：false
 *
 * 核心技巧：
 * 1. 快慢指针找中点
 * 2. 反转后半部分
 * 3. 比较前后两部分
 */
public class LB007_PalindromeList {

    // 链表节点定义
  public static class ListNode {
        int val;
      ListNode next;
    public ListNode(int val) { this.val = val; }
   }

   /**
     * 解法：找中点 + 反转 + 比较
     * 时间复杂度：O(n)
     * 空间复杂度：O(1)
     */
  public boolean isPalindrome(ListNode head) {
     if (head == null || head.next == null) return true;

       // 1. 快慢指针找中点
     ListNode slow = head;
     ListNode fast = head;
       while(fast != null && fast.next != null) {
          slow = slow.next;
          fast = fast.next.next;
       }

       // 2. 反转后半部分
     ListNode secondHalf = reverseList(slow);

       // 3. 比较前后两部分
     ListNode firstHalf = head;
     ListNode p1 = firstHalf;
     ListNode p2 = secondHalf;

       boolean isPalindrome = true;
       while(p2 != null) {
        if (p1.val != p2.val) {
            isPalindrome = false;
             break;
         }
        p1 = p1.next;
         p2 = p2.next;
       }

       // 4. 恢复链表（可选，但这是好习惯）
     reverseList(secondHalf);

     return isPalindrome;
   }

   // 辅助方法：反转链表
  private ListNode reverseList(ListNode head) {
     ListNode prev = null;
     ListNode curr = head;
       while(curr != null) {
        ListNode nextTemp = curr.next;
          curr.next = prev;
        prev = curr;
          curr = nextTemp;
       }
     return prev;
   }

    /**
     * 进阶：用栈实现（辅助理解）
     */
    public boolean isPalindromeStack(ListNode head) {
        // TODO: 请实现此方法（可选）
        return false;
    }

  public static void main(String[] args) {
       LB007_PalindromeList solution = new LB007_PalindromeList();

       // 创建判题引擎
     JudgeEngine<TestInput, Boolean> engine = new JudgeEngine<>();

       // 添加测试用例
       engine.addTestCase("偶数长度回文",
             new TestInput(createList(new int[]{1,2,2,1})),
               true,
               "基本功能测试：偶数长度回文")
           .addTestCase("奇数长度回文",
             new TestInput(createList(new int[]{1,2,3, 2, 1})),
               true,
               "基本功能测试：奇数长度回文")
           .addTestCase("非回文",
             new TestInput(createList(new int[]{1,2})),
               false,
               "边界情况：两个节点")
           .addTestCase("单个节点",
             new TestInput(createList(new int[]{1})),
               true,
               "边界情况：单个节点")
           .addTestCase("空链表",
             new TestInput(null),
               true,
               "边界情况：空链表");

       // 执行判题
     System.out.println("=== 回文链表检测 ===");
     List<JudgeResult> results = engine.judge(input ->
           solution.isPalindrome(input.head)
       );
     JudgeReporter.printReport(results);

       // 统计结果
       boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
     System.exit(allPassed ? 0 : 1);
   }

   // 测试输入类
   static class TestInput {
     ListNode head;
       TestInput(ListNode head) {
         this.head = head;
     }
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
}
