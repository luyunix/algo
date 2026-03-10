package top.luyuni.algo.lb;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * LB004 - 删除链表的倒数第 N 个结点
 * 
 * 题目描述：
 * 给你一个链表，删除链表的倒数第 n 个结点，并且返回链表的头结点。
 * 
 * 示例：
 * 输入：head = [1,2,3,4,5], n = 2
 * 输出：[1,2,3,5]
 * 
 * 输入：head = [1], n = 1
 * 输出：[]
 * 
 * 输入：head = [1,2], n = 1
 * 输出：[1]
 * 
 * 核心技巧：快慢指针
 * - 快指针先走n步
 * - 然后快慢指针一起走
 * - 快指针到达末尾时，慢指针在倒数第n个
 */
public class LB004_RemoveNthFromEnd {
    
    // 链表节点定义
    public static class ListNode {
        int val;
        ListNode next;
         public ListNode(int val) { this.val = val; }
    }
    
    /**
     * 解法：快慢指针（一次遍历）
     * 时间复杂度：O(n)
     * 空间复杂度：O(1)
     */
   public ListNode removeNthFromEnd(ListNode head, int n) {
        // 创建虚拟头节点，简化边界处理
       ListNode dummy = new ListNode(0);
       dummy.next = head;
        
       // 快慢指针
      ListNode fast = dummy;
      ListNode slow = dummy;
       
        // 快指针先走 n+1 步
      for (int i = 0; i <= n; i++) {
         fast = fast.next;
        }
        
       // 快慢指针一起走，直到快指针到达末尾
      while (fast != null) {
         fast = fast.next;
         slow = slow.next;
        }
        
       // 删除倒数第 n 个节点
      slow.next = slow.next.next;
        
     return dummy.next;
    }
    
    /**
     * 解法2：计算长度后删除（两次遍历）
     */
    public ListNode removeNthFromEndTwoPass(ListNode head, int n) {
        // TODO: 请实现此方法（可选）
        return null;
    }
    
    // 辅助方法
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
    
    // 辅助方法：比较两个链表是否相等
    private static boolean listEquals(ListNode l1, ListNode l2) {
        while (l1 != null && l2 != null) {
            if (l1.val != l2.val) return false;
            l1 = l1.next;
            l2 = l2.next;
        }
        return l1 == null && l2 == null;
    }
    
    public static void main(String[] args) {
        LB004_RemoveNthFromEnd solution = new LB004_RemoveNthFromEnd();
        
        // 创建判题引擎
        JudgeEngine<TestInput, ListNode> engine = new JudgeEngine<>();
        
        // 添加测试用例
        engine
            .addTestCase("正常删除",
                new TestInput(createList(new int[]{1, 2, 3, 4, 5}), 2),
                createList(new int[]{1, 2, 3, 5}),
                "基本功能测试",
                (expected, actual) -> listEquals(expected, actual))
            .addTestCase("删除唯一节点",
                new TestInput(createList(new int[]{1}), 1),
                null,
                "边界情况：单个节点",
                (expected, actual) -> listEquals(expected, actual))
            .addTestCase("删除最后一个",
                new TestInput(createList(new int[]{1, 2}), 1),
                createList(new int[]{1}),
                "边界情况：删除末尾",
                (expected, actual) -> listEquals(expected, actual))
            .addTestCase("删除第一个",
                new TestInput(createList(new int[]{1, 2, 3}), 3),
                createList(new int[]{2, 3}),
                "边界情况：删除头部",
                (expected, actual) -> listEquals(expected, actual));
        
        // 执行判题
        System.out.println("=== 快慢指针法测试 ===");
        List<JudgeResult> results = engine.judge(input -> 
            solution.removeNthFromEnd(input.head, input.n)
        );
        JudgeReporter.printReport(results);
        
        // 统计结果
        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
    
    // 测试输入类
    static class TestInput {
        ListNode head;
        int n;
        TestInput(ListNode head, int n) {
            this.head = head;
            this.n = n;
        }
    }
}
