package top.luyuni.algo.zhan;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * ============================================================
 * 【ZHAN002- 最小栈】辅助栈经典题
 * ============================================================
 * 
 * 题目链接：https://leetcode.cn/problems/min-stack/
 * 
 * 题目描述：
 * 设计一个支持 push ，pop ，top 操作，并能在常数时间内检索到最小元素的栈。
 * 
 * 实现 MinStack 类:
 * - MinStack() 初始化堆栈对象。
 * - void push(int val) 将元素 val 推入堆栈。
 * - void pop() 删除堆栈顶部的元素。
 * - int top() 获取堆栈顶部的元素。
 * - int getMin() 获取堆栈中的最小元素。
 * 
 * 示例：
 * 输入：
 * ["MinStack","push","push","push","getMin","pop","top","getMin"]
 * [[],[-2],[0],[-3],[],[],[],[]]
 * 输出：[null,null,null,null,-3,null,0,-2]
 * 解释：
 * MinStack minStack = new MinStack();
 * minStack.push(-2);
 * minStack.push(0);
 * minStack.push(-3);
 * minStack.getMin();   // 返回 -3
 * minStack.pop();
 * minStack.top();      // 返回 0
 * minStack.getMin();   // 返回 -2
 */
public class ZHAN002_MinStack {
    
   private Stack<Integer> stack;
   private Stack<Integer> minStack;
    
    public ZHAN002_MinStack() {
        stack = new Stack<>();
        minStack = new Stack<>();
    }
    
    public void push(int val) {
        stack.push(val);
       if (minStack.isEmpty() || val <= minStack.peek()) {
            minStack.push(val);
        } else {
            minStack.push(minStack.peek());
        }
    }
    
    public void pop() {
       if (!stack.isEmpty()) {
            stack.pop();
            minStack.pop();
        }
    }
    
    public int top() {
       return stack.peek();
    }
    
    public int getMin() {
       return minStack.peek();
    }
    
    // ============ OJ 判题框架 ============
   
   /**
    * 使用 oj/core 工具进行评测
    */
   public static void main(String[] args) {
       ZHAN002_MinStack solution = new ZHAN002_MinStack();
       
       // 创建判题引擎，测试基本功能
       JudgeEngine<String[], Object[]> engine = new JudgeEngine<>();
       
       // 添加测试用例：基本功能测试
       engine.addTestCase("基本功能", 
          new String[]{"push(-2)", "push(0)", "push(-3)", "getMin()", "pop()", "top()", "getMin()"},
          new Object[]{null, null, null, -3, null, 0, -2},
          "示例测试");
       
       // 执行判题
       System.out.println("=== 最小栈测试 ===");
       List<JudgeResult> results = engine.judge(operations -> {
           ZHAN002_MinStack minStack = new ZHAN002_MinStack();
           List<Object> resultList = new ArrayList<>();
           
           for (String op : operations) {
             if (op.startsWith("push(")) {
                    int val = Integer.parseInt(op.substring(5, op.length() - 1));
                    minStack.push(val);
                 resultList.add(null);
                } else if (op.equals("pop()")) {
                    minStack.pop();
                 resultList.add(null);
                } else if (op.equals("top()")) {
                 resultList.add(minStack.top());
                } else if (op.equals("getMin()")) {
                 resultList.add(minStack.getMin());
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
