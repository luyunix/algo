package top.luyuni.algo.zhan;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * ============================================================
 * 【ZHAN007- 用栈实现队列】双栈经典
 * ============================================================
 *
 * 题目链接：https://leetcode.cn/problems/implement-queue-using-stacks/
 *
 * 题目描述：
 * 请你仅使用两个栈实现先入先出队列。队列应当支持一般队列支持的所有操作
 * （push、pop、peek、empty）。
 *
 * 实现 MyQueue 类：
 * - void push(int x) 将元素 x 推到队列的末尾
 * - int pop() 从队列的开头移除并返回元素
 * - int peek() 返回队列开头的元素
 * - boolean empty() 如果队列为空，返回 true ；否则，返回 false
 *
 * 示例：
 * 输入：
 * ["MyQueue", "push", "push", "peek", "pop", "empty"]
 * [[], [1], [2], [], [], []]
 * 输出：[null, null, null, 1, 1, false]
 * 解释：
 * MyQueue myQueue = new MyQueue();
 * myQueue.push(1); // queue is: [1]
 * myQueue.push(2); // queue is: [1,2] (leftmost is front of the queue)
 * myQueue.peek(); // return 1
 * myQueue.pop(); // return 1, queue is [2]
 * myQueue.empty(); // return false
 */
public class ZHAN007_ImplementQueueUsingStacks {

  private Stack<Integer> inStack;
  private Stack<Integer> outStack;

    public ZHAN007_ImplementQueueUsingStacks() {
        inStack = new Stack<>();
       outStack = new Stack<>();
    }

    public void push(int x) {
       inStack.push(x);
    }

    public int pop() {
      if (outStack.isEmpty()) {
            inToOut();
        }
      return outStack.pop();
    }

    public int peek() {
      if (outStack.isEmpty()) {
            inToOut();
        }
      return outStack.peek();
    }

    public boolean empty() {
      return inStack.isEmpty() && outStack.isEmpty();
    }

  private void inToOut() {
       while (!inStack.isEmpty()) {
           outStack.push(inStack.pop());
       }
   }

    // ============ OJ 判题框架 ============

   /**
    * 使用 oj/core 工具进行评测
    */
   public static void main(String[] args) {
       ZHAN007_ImplementQueueUsingStacks solution = new ZHAN007_ImplementQueueUsingStacks();

       // 创建判题引擎，测试基本功能
       JudgeEngine<String[], Object[]> engine = new JudgeEngine<>();

       // 添加测试用例：示例
       engine.addTestCase("示例",
          new String[]{"push(1)", "push(2)", "peek()", "pop()", "empty()"},
          new Object[]{null, null, 1, 1, false},
          "基本功能测试");

       // 执行判题
       System.out.println("=== 用栈实现队列测试 ===");
       List<JudgeResult> results = engine.judge(operations -> {
           ZHAN007_ImplementQueueUsingStacks myQueue = new ZHAN007_ImplementQueueUsingStacks();
           List<Object> resultList = new ArrayList<>();

           for (String op : operations) {
             if (op.startsWith("push")) {
                   int val = Integer.parseInt(op.substring(5, op.length() - 1));
                   myQueue.push(val);
                 resultList.add(null);
               } else if (op.equals("pop")) {
                 resultList.add(myQueue.pop());
               } else if (op.equals("peek")) {
                 resultList.add(myQueue.peek());
               } else if (op.equals("empty")) {
                 resultList.add(myQueue.empty());
               }
           }

         return resultList.toArray();
       });

       JudgeReporter.printReport(results);

       // 统计结果
       boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
       System.exit(allPassed ? 0 : 1);
   }
}
