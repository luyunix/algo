package top.luyuni.algo.lb;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * LB002 - 环形链表
 *
 * 题目描述：
 * 给定一个链表，判断链表中是否有环。
 * 如果链表中有某个节点，可以通过连续跟踪 next 指针再次到达，则链表中存在环。
 *
 * 示例：
 * 输入：head = [3,2,0,-4], pos = 1（表示尾节点连接到第 1 个节点）
 * 输出：true
 *
 * 输入：head = [1], pos = -1
 * 输出：false
 *
 * 核心技巧：快慢指针
 * - 快指针每次走 2 步，慢指针每次走 1 步
 * - 如果有环，快指针一定会追上慢指针
 * - 如果无环，快指针会先到达null
 */
public class LB002_HasCycle {

    // 链表节点定义
   public static class ListNode {
       int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }

    /**
     * 解法：快慢指针
     * 时间复杂度：O(n)
     * 空间复杂度：O(1)
     */
   public boolean hasCycle(ListNode head) {
       if (head == null) return false;

        ListNode slow = head;   // 慢指针
        ListNode fast = head;   // 快指针

        while (fast != null && fast.next != null) {
            slow = slow.next;          // 慢指针走一步
            fast = fast.next.next;     // 快指针走两步

           if (slow == fast) {
                // 快慢指针相遇，说明有环
               return true;
            }
        }

        // 快指针到达终点，无环
       return false;
    }

    /**
     * 进阶：返回环的入口节点
     * 如果链表无环，返回 null
     */
   public ListNode detectCycle(ListNode head) {
       if (head == null) return null;

        ListNode slow = head;
        ListNode fast = head;
        boolean hasCycle = false;

        // 第一阶段：判断是否有环
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;

           if (slow == fast) {
                hasCycle = true;
                break;
            }
        }

       if (!hasCycle) return null;

        // 第二阶段：找到环的入口
        // 将一个指针放回起点，两个指针都每次走一步
        slow = head;
        while (slow != fast) {
            slow = slow.next;
            fast = fast.next;
        }

       return slow; // 环的入口
    }

    // ============ OJ 判题框架 ============

   public static void main(String[] args) {
        LB002_HasCycle solution = new LB002_HasCycle();

        // 创建判题引擎
        JudgeEngine<ListNode, Boolean> engine = new JudgeEngine<>();

        // 添加测试用例
        engine
            .addTestCase("示例 1",
                createCyclicList(new int[]{3, 2, 0, -4}, 1),
                true,
                "head = [3,2,0,-4], pos = 1")
            .addTestCase("示例 2",
                createList(new int[]{1}),
                false,
                "head = [1], pos = -1")
            .addTestCase("空链表",
                null,
                false,
                "head = [], pos = -1")
            .addTestCase("两个节点有环",
                createCyclicList(new int[]{1, 2}, 0),
                true,
                "head = [1,2], pos = 0");

        // 执行判题
       System.out.println("=== 快慢指针法测试 ===");
        List<JudgeResult> results = engine.judge(solution::hasCycle);
        JudgeReporter.printReport(results);

        // 统计结果
        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
       System.exit(allPassed ? 0 : 1);
    }

    // 辅助方法：创建普通链表
   private static ListNode createList(int[] values) {
       if (values == null || values.length == 0) return null;

        ListNode head = new ListNode(values[0]);
        ListNode curr = head;

       for (int i = 1; i < values.length; i++) {
            curr.next = new ListNode(values[i]);
            curr = curr.next;
        }

       return head;
    }

    // 辅助方法：创建带环的链表
   private static ListNode createCyclicList(int[] values, int pos) {
       if (values == null || values.length == 0) return null;

        ListNode head = new ListNode(values[0]);
        ListNode curr = head;
        ListNode cycleNode = (pos == 0) ? head : null;

       for (int i = 1; i < values.length; i++) {
            curr.next = new ListNode(values[i]);
            curr = curr.next;
           if (i == pos) {
                cycleNode = curr;
            }
        }

       if (pos >= 0) {
            curr.next = cycleNode;
        }

       return head;
    }
}
