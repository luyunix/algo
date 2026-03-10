package top.luyuni.algo.lb;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * ============================================================
 * 【LB001 - 反转链表】
 * ============================================================
 *
 * 题目描述：
 * 给你单链表的头节点 head，请你反转链表，并返回反转后的链表。
 *
 * 示例 1：
 * 输入：head = [1,2,3,4,5]
 * 输出：[5,4,3,2,1]
 *
 * 示例 2：
 * 输入：head = [1,2]
 * 输出：[2,1]
 *
 * 示例 3：
 * 输入：head = []
 * 输出：[]
 */
public class LB001_ReverseList {

    /**
     * 链表节点定义
     */
    public static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    /**
     * 方法1：迭代（三指针法）
     *
     * @param head 链表头节点
     * @return 反转后的链表头节点
     */
    public ListNode reverseList(ListNode head) {
        // prev：前一个节点，初始为null
        ListNode prev = null;
        // curr：当前节点，从头节点开始
        ListNode curr = head;

        // 遍历链表，直到curr为null
        while (curr != null) {
            // 保存下一个节点，防止断链后找不到
            ListNode next = curr.next;
            // 反转指针：当前节点指向前一个节点
            curr.next = prev;
            // prev前移
            prev = curr;
            // curr前移
            curr = next;
        }

        // prev现在是新的头节点
        return prev;
    }

    /**
     * 方法2：递归
     */
    public ListNode reverseListRecursive(ListNode head) {
        // 终止条件：空链表或只有一个节点
        if (head == null || head.next == null) {
            return head;
        }

        // 递归反转后面的链表
        ListNode newHead = reverseListRecursive(head.next);

        // 把当前节点接到反转后链表的末尾
        // head.next现在指向反转后链表的尾节点
        head.next.next = head;  // 让后一个节点指向当前节点
        head.next = null;       // 断开原来的连接，防止环

        return newHead;
    }

    /**
     * 方法3：用栈（辅助理解，面试不推荐）
     */
    public ListNode reverseListStack(ListNode head) {
        // 如果链表为空或只有一个节点，直接返回
        if (head == null || head.next == null) {
            return head;
        }

        // 用栈保存所有节点
        Deque<ListNode> stack = new ArrayDeque<>();
        ListNode curr = head;

        // 把所有节点压入栈
        while (curr != null) {
            stack.push(curr);
            curr = curr.next;
        }

        // 新的头节点是栈顶（原链表的尾节点）
        ListNode newHead = stack.pop();
        curr = newHead;

        // 依次弹出栈中节点，构建新链表
        while (!stack.isEmpty()) {
            curr.next = stack.pop();
            curr = curr.next;
        }

        // 最后一个节点的next设为null
        curr.next = null;

        return newHead;
    }

    // ============ 测试框架 ============

    /**
     * 从数组构建链表
     */
    public static ListNode buildList(int[] arr) {
        if (arr == null || arr.length == 0) return null;

        ListNode dummy = new ListNode(0);
        ListNode curr = dummy;

        for (int val : arr) {
            curr.next = new ListNode(val);
            curr = curr.next;
        }

        return dummy.next;
    }

    /**
     * 链表转数组（用于验证）
     */
    public static int[] listToArray(ListNode head) {
        List<Integer> list = new ArrayList<>();
        while (head != null) {
            list.add(head.val);
            head = head.next;
        }

        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    /**
     * 比较两个数组是否相等
     */
    public static boolean arrayEquals(int[] a, int[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    // ============ OJ判题框架 ============

    /**
     * 使用oj/core工具进行评测
     */
    public static void main(String[] args) {
        LB001_ReverseList solution = new LB001_ReverseList();

        // 创建判题引擎，输入是int[]，输出是int[]
        JudgeEngine<int[], int[]> engine = new JudgeEngine<>();

        // 添加测试用例
        engine
            .addTestCase("示例1：5个节点",
                new int[]{1, 2, 3, 4, 5},
                new int[]{5, 4, 3, 2, 1},
                "基本功能测试")
            .addTestCase("示例2：2个节点",
                new int[]{1, 2},
                new int[]{2, 1},
                "两个节点反转")
            .addTestCase("示例3：空链表",
                new int[]{},
                new int[]{},
                "边界情况：空链表")
            .addTestCase("单个节点",
                new int[]{1},
                new int[]{1},
                "边界情况：单个节点")
            .addTestCase("两个相同节点",
                new int[]{1, 1},
                new int[]{1, 1},
                "相同值节点");

        // 执行判题 - 迭代方法
        System.out.println("=== 迭代方法测试 ===");
        List<JudgeResult> results1 = engine.judge(input -> {
            ListNode head = buildList(input);
            ListNode reversed = solution.reverseList(head);
            return listToArray(reversed);
        });
        JudgeReporter.printReport(results1);

        // 执行判题 - 递归方法
        System.out.println("\n=== 递归方法测试 ===");
        List<JudgeResult> results2 = engine.judge(input -> {
            ListNode head = buildList(input);
            ListNode reversed = solution.reverseListRecursive(head);
            return listToArray(reversed);
        });
        JudgeReporter.printReport(results2);

        // 执行判题 - 栈方法
        System.out.println("\n=== 栈方法测试 ===");
        List<JudgeResult> results3 = engine.judge(input -> {
            ListNode head = buildList(input);
            ListNode reversed = solution.reverseListStack(head);
            return listToArray(reversed);
        });
        JudgeReporter.printReport(results3);

        // 统计结果
        boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted)
            && results2.stream().allMatch(JudgeResult::isAccepted)
            && results3.stream().allMatch(JudgeResult::isAccepted);

        System.exit(allPassed ? 0 : 1);
    }
}
