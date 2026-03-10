package top.luyuni.algo.oj.core;

import java.util.List;

/**
 * 判题结果报告
 */
public class JudgeReporter {

    /**
     * 打印简单的测试报告（不带题目名称）- AC 时也显示完整信息
     */
    public static void printReport(List<JudgeResult> results) {
        int passed = 0;
        int total = results.size();

        for (int i = 0; i < results.size(); i++) {
            JudgeResult r = results.get(i);
            System.out.printf("测试 #%d: %s - ", i + 1, r.getTestCaseName());

            if (r.getStatus() == JudgeResult.Status.AC) {
                passed++;
                System.out.println(GREEN + "✓ AC" + RESET + " (" + r.getTimeMs() + "ms)");
                // AC 时也显示详细信息
                if (r.getInput() != null) {
                    System.out.println("  输入：" + formatValue(r.getInput()));
                }
                if (r.getExpected() != null) {
                    System.out.println("  期望：" + formatValue(r.getExpected()));
                }
                if (r.getActual() != null) {
                    System.out.println("  实际：" + formatValue(r.getActual()));
                }
            } else {
                System.out.println(RED + "✗ " + r.getStatus() + RESET);
                // WA 时显示详细信息
                if (r.getInput() != null) {
                    System.out.println("  输入：" + formatValue(r.getInput()));
                }
                if (r.getExpected() != null) {
                    System.out.println("  期望：" + formatValue(r.getExpected()));
                }
                if (r.getActual() != null) {
                    System.out.println("  实际：" + formatValue(r.getActual()));
                }
            }
        }

        System.out.println("----------------------------------------");
        System.out.printf("结果：%d/%d 通过", passed, total);
        if (passed == total) {
            System.out.println(GREEN + BOLD + " ✓ ALL PASSED" + RESET);
        } else {
            System.out.println(RED + BOLD + " ✗ FAILED" + RESET);
        }
    }

    public static void report(String problemName, List<JudgeResult> results) {
        System.out.println("========================================");
        System.out.println("题目：" + problemName);
        System.out.println("========================================\n");

        int passed = 0;
        int total = results.size();
        long totalTime = 0;

        for (int i = 0; i < results.size(); i++) {
            JudgeResult r = results.get(i);
            System.out.printf("测试用例 #%d: %s\n", i + 1, r.getTestCaseName());
            System.out.println("状态：" + formatStatus(r.getStatus()));

            if (r.getStatus() == JudgeResult.Status.AC) {
                passed++;
                System.out.println("耗时：" + r.getTimeMs() + "ms");
                totalTime += r.getTimeMs();
            } else {
                System.out.println("详情：" + r.getMessage());
            }
            // 无论 AC/WA 都显示详细信息
            if (r.getInput() != null) {
                System.out.println("  输入：" + formatValue(r.getInput()));
            }
            if (r.getExpected() != null) {
                System.out.println("  期望：" + formatValue(r.getExpected()));
            }
            if (r.getActual() != null) {
                System.out.println("  实际：" + formatValue(r.getActual()));
            }
            System.out.println();
        }

        System.out.println("----------------------------------------");
        System.out.printf("结果：%d/%d 通过", passed, total);
        if (passed == total) {
            System.out.println(GREEN + BOLD + " ✓ ALL PASSED" + RESET);
        } else {
            System.out.println(RED + BOLD + " ✗ FAILED" + RESET);
        }
        if (passed > 0) {
            System.out.println("总耗时：" + totalTime + "ms");
        }
        System.out.println("========================================");
    }

    // ANSI 颜色码
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    private static String formatStatus(JudgeResult.Status status) {
        switch (status) {
            case AC:
                return GREEN + BOLD + "✓ Accepted" + RESET;
            case WA:
                return RED + BOLD + "✗ Wrong Answer" + RESET;
            case TLE:
                return YELLOW + BOLD + "⏱ Time Limit Exceeded" + RESET;
            case RE:
                return RED + BOLD + "✗ Runtime Error" + RESET;
            case CE:
                return RED + BOLD + "✗ Compile Error" + RESET;
            default:
                return status.name();
        }
    }

    /**
     * 格式化值（支持数组和复杂对象）
     */
    private static String formatValue(Object value) {
        if (value == null) {
            return "null";
        }

        // 处理 int[] 数组
        if (value instanceof int[]) {
            int[] arr = (int[]) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(arr[i]);
            }
            sb.append("]");
            return sb.toString();
        }

        // 处理 char[] 数组
        if (value instanceof char[]) {
            char[] arr = (char[]) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(arr[i]);
            }
            sb.append("]");
            return sb.toString();
        }

        // 处理 char[][] 二维数组
        if (value instanceof char[][]) {
            char[][] arr = (char[][]) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append("[");
                for (int j = 0; j < arr[i].length; j++) {
                    if (j > 0) sb.append(",");
                    sb.append(arr[i][j]);
                }
                sb.append("]");
            }
            sb.append("]");
            return sb.toString();
        }

        // 处理 ListNode 链表
        if (value != null && "ListNode".equals(value.getClass().getSimpleName())) {
            try {
                java.lang.reflect.Field nextField = value.getClass().getDeclaredField("next");
                nextField.setAccessible(true);

                StringBuilder sb = new StringBuilder("[");
                Object curr = value;
                boolean first = true;
                java.util.Set<Object> visited = new java.util.HashSet<>(); // 检测环

                while (curr != null && !visited.contains(curr)) {
                    if (!first) sb.append(",");
                    first = false;
                    visited.add(curr); // 标记已访问

                    // 获取 val 字段
                    java.lang.reflect.Field valField = curr.getClass().getDeclaredField("val");
                    valField.setAccessible(true);
                    sb.append(valField.get(curr));

                    curr = nextField.get(curr);
                }

                if (curr != null) {
                    sb.append(" (有环)");
                }
                sb.append("]");

                return sb.toString();

            } catch (Exception e) {
                // 如果反射失败，返回默认 toString
            }
        }

        // 处理 TestInput（包含链表等字段）
        if (value.getClass().getName().contains("TestInput")) {
            try {
                java.lang.reflect.Field[] fields = value.getClass().getDeclaredFields();
                StringBuilder sb = new StringBuilder();
                boolean first = true;

                for (java.lang.reflect.Field field : fields) {
                    field.setAccessible(true);
                    Object fieldValue = field.get(value);

                    if (!first) sb.append(", ");
                    first = false;

                    sb.append(field.getName()).append("=");
                    sb.append(formatValue(fieldValue));
                }

                return sb.toString();
            } catch (Exception e) {
                // 如果反射失败，返回默认 toString
            }
        }

        // 处理 Node 带随机指针的链表
        if (value != null && "Node".equals(value.getClass().getSimpleName())) {
            try {
                java.lang.reflect.Field nextField = value.getClass().getDeclaredField("next");
                nextField.setAccessible(true);
                java.lang.reflect.Field randomField = value.getClass().getDeclaredField("random");
                randomField.setAccessible(true);
                java.lang.reflect.Field valField = value.getClass().getDeclaredField("val");
                valField.setAccessible(true);

                StringBuilder sb = new StringBuilder("[");
                Object curr = value;
                boolean first = true;
                java.util.Set<Object> visited = new java.util.HashSet<>(); // 检测环

                while (curr != null && !visited.contains(curr)) {
                    if (!first) sb.append("], [");
                    first = false;
                    visited.add(curr); // 标记已访问

                    // 获取 val 和 random
                    int val = (int) valField.get(curr);
                    Object random = randomField.get(curr);
                    String randomStr = (random != null) ? String.valueOf(valField.get(random)) : "null";

                    sb.append(val).append(",").append(randomStr);

                    curr = nextField.get(curr);
                }

                sb.append("]");

                return sb.toString();

            } catch (Exception e) {
                // 如果反射失败，返回默认 toString
            }
        }

        // 处理 TreeNode 二叉树
        if (value != null && "TreeNode".equals(value.getClass().getSimpleName())) {
            try {
                java.lang.reflect.Field leftField = value.getClass().getDeclaredField("left");
                leftField.setAccessible(true);
                java.lang.reflect.Field rightField = value.getClass().getDeclaredField("right");
                rightField.setAccessible(true);
                java.lang.reflect.Field valField = value.getClass().getDeclaredField("val");
                valField.setAccessible(true);

                // 层序遍历打印二叉树
                StringBuilder sb = new StringBuilder();
                java.util.Queue<Object> queue = new java.util.LinkedList<>();
                queue.offer(value);

                while (!queue.isEmpty()) {
                    int size = queue.size();
                    sb.append("[");
                    for (int i = 0; i < size; i++) {
                        if (i > 0) sb.append(", ");
                        Object node = queue.poll();
                        if (node == null) {
                            sb.append("null");
                        } else {
                            int val = (int) valField.get(node);
                            sb.append(val);
                            Object left = leftField.get(node);
                            Object right = rightField.get(node);
                            if (left != null || right != null) {
                                queue.offer(left);
                                queue.offer(right);
                            }
                        }
                    }
                    sb.append("]");
                    if (!queue.isEmpty()) sb.append(", ");
                }

                return sb.toString();

            } catch (Exception e) {
                // 如果反射失败，返回默认 toString
            }
        }

        // 处理 boolean[] 数组
        if (value instanceof boolean[]) {
            boolean[] arr = (boolean[]) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(arr[i]);
            }
            sb.append("]");
            return sb.toString();
        } else if (value instanceof Object[]) {
            Object[] arr = (Object[]) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(", ");
                if (arr[i] == null) {
                    sb.append("null");
                } else if (arr[i].getClass().isArray()) {
                    // 处理嵌套数组 - 递归调用
                    sb.append(formatArrayDeep(arr[i]));
                } else {
                    sb.append(String.valueOf(arr[i]));
                }
            }
            sb.append("]");
            return sb.toString();
        }

        return String.valueOf(value);
    }

    /**
     * 格式化数组为字符串
     */
    private static String arrayToString(Object arr) {
        if (arr instanceof int[]) {
            int[] a = (int[]) arr;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < a.length; i++) {
                sb.append(a[i]);
                if (i < a.length - 1) sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        } else if (arr instanceof Object[]) {
            Object[] o = (Object[]) arr;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < o.length; i++) {
                if (o[i] == null) {
                    sb.append("null");
                } else {
                    sb.append(o[i].toString());
                }
                if (i < o.length - 1) sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        }
        return arr.toString();
    }

    /**
     * 深度格式化数组（支持多维）
     */
    private static String formatArrayDeep(Object arr) {
        if (arr == null) {
            return "null";
        }

        // 检查是否是数组类型
        Class<?> componentType = arr.getClass().getComponentType();
        if (!componentType.isArray()) {
            // 一维数组
            return arrayToString(arr);
        }

        // 多维数组 - 递归处理
        Object[] objArr = (Object[]) arr;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < objArr.length; i++) {
            if (i > 0) sb.append(", ");
            if (objArr[i] == null) {
                sb.append("null");
            } else if (objArr[i].getClass().isArray()) {
                sb.append(formatArrayDeep(objArr[i]));
            } else {
                sb.append(objArr[i].toString());
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
