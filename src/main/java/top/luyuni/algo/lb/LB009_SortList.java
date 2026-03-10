package top.luyuni.algo.lb;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * LB009 - 排序链表
 * 
 * 题目描述：
 * 给你链表的头结点 head ，请将其按升序排列并返回排序后的链表。
 * 
 * 示例：
 * 输入：head = [4,2,1,3]
 * 输出：[1,2,3,4]
 * 
 * 输入：head = [-1,5,3,4,0]
 * 输出：[-1,0,3,4,5]
 * 
 * 核心技巧：归并排序
 * 1. 快慢指针找中点，将链表分成两半
 * 2. 递归排序左右两部分
 * 3. 合并两个有序链表
 */
public class LB009_SortList {
    
    // 链表节点定义
    public static class ListNode {
        int val;
       ListNode next;
        public ListNode(int val) { this.val = val; }
    }
    
    /**
     * 解法：归并排序（自顶向下）
     * 时间复杂度：O(n log n)
     * 空间复杂度：O(log n)（递归栈空间）
     */
    public ListNode sortList(ListNode head) {
        // 边界条件
        if (head == null || head.next == null) return head;
        
        // 1. 快慢指针找中点
       ListNode slow = head;
       ListNode fast = head;
       ListNode prev = null;
        
        while(fast != null && fast.next != null) {
           prev = slow;
            slow = slow.next;
            fast = fast.next.next;
        }
        
        // 断开链表
        if (prev != null) {
           prev.next = null;
        }
        
        // 2. 递归排序左右两部分
       ListNode left = sortList(head);
       ListNode right = sortList(slow);
        
        // 3. 合并两个有序链表
       return merge(left, right);
    }
    
    // 辅助方法：合并两个有序链表
   private ListNode merge(ListNode l1, ListNode l2) {
       ListNode dummy = new ListNode(0);
       ListNode curr = dummy;
        
        while(l1 != null && l2 != null) {
            if (l1.val <= l2.val) {
                curr.next = l1;
                l1 = l1.next;
            } else {
                curr.next = l2;
                l2 = l2.next;
            }
            curr = curr.next;
        }
        
        // 连接剩余节点
        if (l1 != null) {
            curr.next = l1;
        } else {
            curr.next = l2;
        }
        
       return dummy.next;
    }
    
    /**
     * 进阶：自底向上的归并排序（空间复杂度 O(1)）
     */
    public ListNode sortListBottomUp(ListNode head) {
        // TODO: 请实现此方法（可选）
       return null;
    }
    
    public static void main(String[] args) {
        LB009_SortList solution = new LB009_SortList();
        
        // 创建判题引擎
        JudgeEngine<TestInput, ListNode> engine = new JudgeEngine<>();
        
        // 添加测试用例
        engine.addTestCase("正常排序",
                new TestInput(createList(new int[]{4,2,1,3})),
                createList(new int[]{1,2,3,4}),
                "基本功能测试",
                (expected, actual) -> listEquals(expected, actual))
            .addTestCase("有负数",
                new TestInput(createList(new int[]{-1,5,3,4,0})),
                createList(new int[]{-1,0,3,4,5}),
                "包含负数",
                (expected, actual) -> listEquals(expected, actual))
            .addTestCase("空链表",
                new TestInput(null),
                null,
                "边界情况：空链表")
            .addTestCase("单个节点",
                new TestInput(createList(new int[]{1})),
                createList(new int[]{1}),
                "边界情况：单个节点",
                (expected, actual) -> listEquals(expected, actual))
            .addTestCase("已经有序",
                new TestInput(createList(new int[]{1,2,3,4,5})),
                createList(new int[]{1,2,3,4,5}),
                "边界情况：已经有序",
                (expected, actual) -> listEquals(expected, actual));
        
        // 执行判题
        System.out.println("=== 归并排序测试 ===");
       List<JudgeResult> results = engine.judge(input -> 
            solution.sortList(input.head)
        );
        JudgeReporter.printReport(results);
        
        // 统计结果
        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
    
    // 辅助方法：比较两个链表是否相等
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
       ListNode head;
       TestInput(ListNode head) {
            this.head = head;
        }
    }
}
