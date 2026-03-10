package top.luyuni.algo.lb;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * LB008 - 奇偶链表
 * 
 * 题目描述：
 * 给定单链表的头节点 head ，将所有索引为奇数的节点和索引为偶数的节点分别组合在一起，
 * 然后返回重新排序的列表。
 * 第一个节点的索引被认为是奇数，第二个节点的索引为偶数，以此类推。
 * 
 * 示例：
 * 输入：head = [1,2,3,4,5]
 * 输出：[1,3,5,2,4]
 * 
 * 输入：head = [2,1,3,5,6,4,7]
 * 输出：[2,3,6,7,1,5,4]
 * 
 * 核心技巧：双指针分组
 * - odd指针连接奇数索引节点
 * - even指针连接偶数索引节点
 * - 最后把偶数链表接到奇数链表后面
 */
public class LB008_OddEvenList {
    
    // 链表节点定义
    public static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }
    
    /**
     * 解法：双指针分组
     * 时间复杂度：O(n)
     * 空间复杂度：O(1)
     */
    public ListNode oddEvenList(ListNode head) {
        // TODO: 请实现此方法
        return null;
    }
    
    public static void main(String[] args) {
        LB008_OddEvenList solution = new LB008_OddEvenList();
        
        // 测试用例1：奇数个节点
        ListNode head1 = createList(new int[]{1, 2, 3, 4, 5});
        System.out.println("测试1: [1,2,3,4,5]");
        ListNode result1 = solution.oddEvenList(head1);
        System.out.println("结果: " + listToString(result1));
        System.out.println("期望: [1,3,5,2,4]");
        
        // 测试用例2：偶数个节点
        ListNode head2 = createList(new int[]{2, 1, 3, 5, 6, 4, 7});
        System.out.println("\n测试2: [2,1,3,5,6,4,7]");
        ListNode result2 = solution.oddEvenList(head2);
        System.out.println("结果: " + listToString(result2));
        System.out.println("期望: [2,3,6,7,1,5,4]");
        
        // 测试用例3：两个节点
        ListNode head3 = createList(new int[]{1, 2});
        System.out.println("\n测试3: [1,2]");
        ListNode result3 = solution.oddEvenList(head3);
        System.out.println("结果: " + listToString(result3));
        System.out.println("期望: [1,2]");
        
        // 测试用例4：单个节点
        ListNode head4 = createList(new int[]{1});
        System.out.println("\n测试4: [1]");
        ListNode result4 = solution.oddEvenList(head4);
        System.out.println("结果: " + listToString(result4));
        System.out.println("期望: [1]");
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
    
    private static String listToString(ListNode head) {
        if (head == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        while (head != null) {
            sb.append(head.val);
            if (head.next != null) sb.append(",");
            head = head.next;
        }
        sb.append("]");
        return sb.toString();
    }
}
