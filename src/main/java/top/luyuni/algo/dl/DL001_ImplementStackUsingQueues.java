package top.luyuni.algo.dl;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * DL001 - 用队列实现栈
 */
public class DL001_ImplementStackUsingQueues {

    static class MyStack {
        private Queue<Integer> queue1;
        private Queue<Integer> queue2;

        public MyStack() {
            queue1 = new LinkedList<>();
            queue2 = new LinkedList<>();
        }

        public void push(int x) {
            queue1.offer(x);
        }

        public int pop() {
            while (queue1.size() > 1) {
                queue2.offer(queue1.poll());
            }
            int result = queue1.poll();
            Queue<Integer> temp = queue1;
            queue1 = queue2;
            queue2 = temp;
            return result;
        }

        public int top() {
            while (queue1.size() > 1) {
                queue2.offer(queue1.poll());
            }
            int result = queue1.peek();
            queue2.offer(queue1.poll());
            Queue<Integer> temp = queue1;
            queue1 = queue2;
            queue2 = temp;
            return result;
        }

        public boolean empty() {
            return queue1.isEmpty();
        }
    }

    public static void main(String[] args) {
        JudgeEngine<String[], Object[]> engine = new JudgeEngine<>();
        engine.addTestCase("基本操作",
                new String[]{"push", "push", "top", "pop", "empty"},
                new Object[]{null, null, null, 2, false},
                "测试栈的基本操作",
                (expected, actual) -> testBasicOps(expected, actual));

        System.out.println("=== 用队列实现栈测试 ===");
        List<JudgeResult> results = engine.judge(input -> runOperations(input));
        JudgeReporter.printReport(results);

        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }

    private static Object[] runOperations(String[] ops) {
        MyStack stack = new MyStack();
        List<Object> results = new ArrayList<>();

        for (String op : ops) {
            switch (op) {
                case "push":
                    stack.push(1);
                    results.add(null);
                    break;
                case "pop":
                    results.add(stack.pop());
                    break;
                case "top":
                    results.add(stack.top());
                    break;
                case "empty":
                    results.add(stack.empty());
                    break;
            }
        }
        return results.toArray();
    }

    private static boolean testBasicOps(Object[] expected, Object[] actual) {
        if (expected.length != actual.length) return false;
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] == null && actual[i] == null) continue;
            if (expected[i] != null && !expected[i].equals(actual[i])) return false;
        }
        return true;
    }
}
