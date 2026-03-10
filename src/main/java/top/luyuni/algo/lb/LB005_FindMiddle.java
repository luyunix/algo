package top.luyuni.algo.lb;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * LB005 - 链表的中间结点
 * <p>
 * 题目描述：
 * 给你单链表的头结点 head ，请你找出并返回链表的中间结点。
 * 如果有两个中间结点，则返回第二个中间结点。
 * <p>
 * 示例：
 * 输入：head = [1,2,3,4,5]
 * 输出：[3,4,5]（返回节点3）
 * <p>
 * 输入：head = [1,2,3,4,5,6]
 * 输出：[4,5,6]（返回节点4，第二个中间节点）
 * <p>
 * 核心技巧：快慢指针
 * - 快指针每次走2步，慢指针每次走1步
 * - 快指针到达末尾时，慢指针在中间
 */
public class LB005_FindMiddle {

    // 链表节点定义
    public static class ListNode {
        int val;
        ListNode next;

        public ListNode(int val) {
            this.val = val;
        }
    }

    /**
     * 解法：快慢指针
     * 时间复杂度：O(n)
     * 空间复杂度：O(1)
     */
    public ListNode middleNode(ListNode head) {
        // 快慢指针
        ListNode fast = head;
        ListNode slow = head;

        // 快指针每次走 2 步，慢指针每次走 1 步
        while (fast != null && fast.next != null) {
            fast = fast.next.next;
            slow = slow.next;
        }

        // 快指针到达末尾时，慢指针在中间
        return slow;
    }

    /**
     * 进阶：如果有两个中间结点，返回第一个
     */
    public ListNode middleNodeFirst(ListNode head) {
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

    public static void main(String[] args) {
        LB005_FindMiddle solution = new LB005_FindMiddle();

        // 创建判题引擎
        JudgeEngine<TestInput, ListNode> engine = new JudgeEngine<>();

        // 添加测试用例
        engine
                .addTestCase("奇数个节点",
                        new TestInput(createList(new int[]{1, 2, 3, 4, 5})),
                        createList(new int[]{3, 4, 5}),
                        "基本功能测试：奇数长度",
                        (expected, actual) -> listEquals(expected, actual))
                .addTestCase("偶数个节点",
                        new TestInput(createList(new int[]{1, 2, 3, 4, 5, 6})),
                        createList(new int[]{4, 5, 6}),
                        "基本功能测试：偶数长度（返回第二个中间节点）",
                        (expected, actual) -> listEquals(expected, actual))
                .addTestCase("单个节点",
                        new TestInput(createList(new int[]{1})),
                        createList(new int[]{1}),
                        "边界情况：只有一个节点",
                        (expected, actual) -> listEquals(expected, actual))
                .addTestCase("两个节点",
                        new TestInput(createList(new int[]{1, 2})),
                        createList(new int[]{2}),
                        "边界情况：两个节点",
                        (expected, actual) -> listEquals(expected, actual));

        // 执行判题
        System.out.println("=== 快慢指针法测试 ===");
        List<JudgeResult> results = engine.judge(input ->
                solution.middleNode(input.head)
        );
        JudgeReporter.printReport(results);

        // 统计结果
        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
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

    // 测试输入类
    static class TestInput {
        ListNode head;

        TestInput(ListNode head) {
            this.head = head;
        }
    }
}
