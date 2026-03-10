package top.luyuni.algo.lb;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * LB003 - 合并两个有序链表
 * 
 * 题目描述：
 * 将两个升序链表合并为一个新的升序链表并返回。
 * 新链表是通过拼接给定的两个链表的所有节点组成的。
 * 
 * 示例：
 * 输入：l1 = [1,2,4], l2 = [1,3,4]
 * 输出：[1,1,2,3,4,4]
 * 
 * 输入：l1 = [], l2 = []
 * 输出：[]
 * 
 * 输入：l1 = [], l2 = [0]
 * 输出：[0]
 * 
 * 核心技巧：双指针
 * - 比较两个链表当前节点的值
 * - 小的那个加入结果链表，指针后移
 * - 处理剩余节点
 */
public class LB003_MergeTwoLists {
    
    // 链表节点定义
    public static class ListNode {
        int val;
        ListNode next;
         public ListNode(int val) { this.val = val; }
    }
    
    /**
     * 解法：双指针
     * 时间复杂度：O(n+m)
     * 空间复杂度：O(1)（只移动指针，不创建新节点）
     */
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
       // 创建虚拟头节点
      ListNode dummy = new ListNode(0);
      ListNode curr = dummy;
      
     // 双指针遍历两个链表
    while (list1 != null && list2 != null) {
        if (list1.val <= list2.val) {
            curr.next = list1;
            list1 = list1.next;
            } else {
            curr.next = list2;
            list2 = list2.next;
            }
        curr = curr.next;
        }
    
   // 连接剩余节点
  if (list1 != null) {
      curr.next = list1;
     } else {
      curr.next = list2;
     }
     
   return dummy.next;
    }
    
    /**
     * 进阶：递归解法
     */
    public ListNode mergeTwoListsRecursive(ListNode list1, ListNode list2) {
        // TODO: 请实现此方法（可选）
        return null;
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

    // 辅助方法：链表转字符串
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
        LB003_MergeTwoLists solution = new LB003_MergeTwoLists();
        
        // 创建判题引擎
        JudgeEngine<TestInput, ListNode> engine = new JudgeEngine<>();
        
        // 添加测试用例（带自定义比较器）
        // 注意：由于 mergeTwoLists 会修改输入链表的结构，需要确保输入和期望使用独立的链表
        engine
            .addTestCase("正常合并",
                new TestInput(createList(new int[]{1, 2, 4}), createList(new int[]{1, 3, 4})),
                createList(new int[]{1, 1, 2, 3, 4, 4}),
                "基本功能测试",
                (expected, actual) -> listEquals(expected, actual))
            .addTestCase("都为空",
                new TestInput(null, null),
                null,
                "边界情况：都为空",
                (expected, actual) -> listEquals(expected, actual))
            .addTestCase("一个为空",
                new TestInput(null, createList(new int[]{0})),
                createList(new int[]{0}),
                "边界情况：一个为空",
                (expected, actual) -> listEquals(expected, actual))
            .addTestCase("有负数",
                new TestInput(createList(new int[]{-5, -3, 0}), createList(new int[]{-2, 1})),
                createList(new int[]{-5, -3, -2, 0, 1}),
                "包含负数",
                (expected, actual) -> listEquals(expected, actual));
        
        // 执行判题
      System.out.println("=== 双指针法测试 ===");
        List<JudgeResult> results = engine.judge(input -> 
            solution.mergeTwoLists(input.list1, input.list2)
        );
      JudgeReporter.printReport(results);
        
        // 统计结果
        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
      System.exit(allPassed ? 0 : 1);
    }
    
    // 测试输入类
    static class TestInput {
        ListNode list1;
        ListNode list2;
        TestInput(ListNode list1, ListNode list2) {
            this.list1 = list1;
            this.list2 = list2;
        }
    }
}
