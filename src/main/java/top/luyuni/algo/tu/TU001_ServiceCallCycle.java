package top.luyuni.algo.tu;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * ============================================================
 * 【TU001 - 服务调用环检测】
 * ============================================================
 * <p>
 * 题目背景：
 * 微服务架构中，服务之间的调用关系如果形成循环（如 A→B→C→A），
 * 会导致服务启动死锁、运行时调用雪崩等问题。
 * <p>
 * 题目描述：
 * 给定一组服务名称（以大写英文字母命名，如 A、B、C）和若干服务调用关系
 * （表示 "服务 X 调用服务 Y"），请判断这些服务之间是否存在循环调用环。
 * <p>
 * 示例 1：
 * 输入：services = ["A", "B", "C"], calls = [["A","B"], ["B","C"], ["C","A"]]
 * 输出：true
 * 解释：A→B→C→A 形成环
 * <p>
 * 示例 2：
 * 输入：services = ["A", "B", "C"], calls = [["A","B"], ["B","C"]]
 * 输出：false
 * 解释：A→B→C，无环
 * <p>
 * 示例 3：
 * 输入：services = ["A", "B", "C", "D"], calls = [["A","B"], ["B","C"], ["C","D"], ["D","B"]]
 * 输出：true
 * 解释：B→C→D→B 形成环
 */
public class TU001_ServiceCallCycle {

    /**
     * 方法 1：DFS + 三色标记法
     *
     * @param services 服务列表
     * @param calls    调用关系，calls[i] = ["A", "B"] 表示 A 调用 B
     * @return 是否存在环
     */
    public boolean hasCycleDFS(String[] services, String[][] calls) {
        // 构建图：邻接表
        Map<String, List<String>> graph = new HashMap<>();
        for (String service : services) {
            graph.put(service, new ArrayList<>());
        }
        for (String[] call : calls) {
            graph.get(call[0]).add(call[1]);
        }

        // 三色标记：0=未访问，1=正在访问（递归栈中），2=已访问完成
        Map<String, Integer> color = new HashMap<>();
        for (String service : services) {
            color.put(service, 0);
        }

        // 对每个未访问的节点进行 DFS
        for (String service : services) {
            if (color.get(service) == 0) {
                if (dfsHasCycle(service, graph, color)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean dfsHasCycle(String node, Map<String, List<String>> graph, Map<String, Integer> color) {
        if (color.get(node) == 1) {
            // 遇到正在访问的节点，说明有环
            return true;
        }
        if (color.get(node) == 2) {
            // 已经访问完成，无需再访问
            return false;
        }

        // 标记为正在访问
        color.put(node, 1);

        // 访问所有邻居
        for (String neighbor : graph.get(node)) {
            if (dfsHasCycle(neighbor, graph, color)) {
                return true;
            }
        }

        // 标记为已访问完成
        color.put(node, 2);
        return false;
    }

    /**
     * 方法2：拓扑排序（Kahn 算法）
     *
     * @param services 服务列表
     * @param calls    调用关系
     * @return 是否存在环
     */
    public boolean hasCycleTopological(String[] services, String[][] calls) {
        // 构建图：邻接表和入度表
        Map<String, List<String>> graph = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();

        // 初始化
        for (String service : services) {
            graph.put(service, new ArrayList<>());
            inDegree.put(service, 0);
        }

        // 建图，计算入度
        for (String[] call : calls) {
            graph.get(call[0]).add(call[1]);
            inDegree.put(call[1], inDegree.get(call[1]) + 1);
        }

        // Kahn 算法：不断移除入度为 0 的节点
        Queue<String> queue = new LinkedList<>();
        for (String service : services) {
            if (inDegree.get(service) == 0) {
                queue.offer(service);
            }
        }

        int visitedCount = 0;
        while (!queue.isEmpty()) {
            String node = queue.poll();
            visitedCount++;

            for (String neighbor : graph.get(node)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        // 如果访问的节点数小于总节点数，说明有环
        return visitedCount < services.length;
    }

    /**
     * 扩展：返回具体的环（如果有）
     */
    public List<String> findCycle(String[] services, String[][] calls) {
        // 构建图：邻接表
        Map<String, List<String>> graph = new HashMap<>();
        for (String service : services) {
            graph.put(service, new ArrayList<>());
        }
        for (String[] call : calls) {
            graph.get(call[0]).add(call[1]);
        }

        // 三色标记：0=未访问，1=正在访问（递归栈中），2=已访问完成
        Map<String, Integer> color = new HashMap<>();
        Map<String, String> parent = new HashMap<>(); // 记录路径
        String cycleNode = null; // 环的入口节点

        for (String service : services) {
            color.put(service, 0);
            parent.put(service, null);
        }

        // DFS 找环
        for (String service : services) {
            if (color.get(service) == 0) {
                cycleNode = dfsFindCycle(service, graph, color, parent);
                if (cycleNode != null) {
                    break;
                }
            }
        }

        if (cycleNode == null) {
            return new ArrayList<>(); // 无环
        }

        // 重构环的路径
        List<String> cycle = new ArrayList<>();
        String curr = cycleNode;
        do {
            cycle.add(curr);
            curr = parent.get(curr);
        } while (curr != null && !curr.equals(cycleNode));

        cycle.add(cycleNode); // 再次添加起点，形成闭环
        Collections.reverse(cycle);

        return cycle;
    }

    private String dfsFindCycle(String node, Map<String, List<String>> graph,
                                Map<String, Integer> color, Map<String, String> parent) {
        if (color.get(node) == 1) {
            // 遇到正在访问的节点，找到环
            return node;
        }
        if (color.get(node) == 2) {
            return null;
        }

        // 标记为正在访问
        color.put(node, 1);

        // 访问所有邻居
        for (String neighbor : graph.get(node)) {
            parent.put(neighbor, node);
            String result = dfsFindCycle(neighbor, graph, color, parent);
            if (result != null) {
                return result;
            }
        }

        // 标记为已访问完成
        color.put(node, 2);
        return null;
    }

    // ============ OJ 判题框架 ============

    /**
     * 使用 oj/core 工具进行评测
     */
    public static void main(String[] args) {
        TU001_ServiceCallCycle solution = new TU001_ServiceCallCycle();

        // 创建判题引擎，输入是 Object[]（services 数组和 calls 数组），输出是 boolean
        JudgeEngine<Object[], Boolean> engine = new JudgeEngine<>();

        // 添加测试用例
        engine
                .addTestCase("示例 1",
                        new Object[]{new String[]{"A", "B", "C"}, new String[][]{{"A", "B"}, {"B", "C"}, {"C", "A"}}},
                        true, "A→B→C→A 形成环")
                .addTestCase("示例 2",
                        new Object[]{new String[]{"A", "B", "C"}, new String[][]{{"A", "B"}, {"B", "C"}}},
                        false, "A→B→C，无环")
                .addTestCase("示例 3",
                        new Object[]{new String[]{"A", "B", "C", "D"}, new String[][]{{"A", "B"}, {"B", "C"}, {"C", "D"}, {"D", "B"}}},
                        true, "B→C→D→B 形成环")
                .addTestCase("自环",
                        new Object[]{new String[]{"A"}, new String[][]{{"A", "A"}}},
                        true, "A→A 自环")
                .addTestCase("无调用",
                        new Object[]{new String[]{"A", "B", "C"}, new String[][]{}},
                        false, "没有调用关系")
                .addTestCase("多个连通分量",
                        new Object[]{new String[]{"A", "B", "C", "D"}, new String[][]{{"A", "B"}, {"C", "D"}, {"D", "C"}}},
                        true, "一个连通分量有环");

        // 执行判题 - DFS方法
        System.out.println("=== DFS方法测试 ===");
        List<JudgeResult> results1 = engine.judge(input -> {
            String[] services = (String[]) input[0];
            String[][] calls = (String[][]) input[1];
            return solution.hasCycleDFS(services, calls);
        });
        JudgeReporter.printReport(results1);

        // 执行判题 - 拓扑排序方法
        System.out.println("\n=== 拓扑排序方法测试 ===");
        List<JudgeResult> results2 = engine.judge(input -> {
            String[] services = (String[]) input[0];
            String[][] calls = (String[][]) input[1];
            return solution.hasCycleTopological(services, calls);
        });
        JudgeReporter.printReport(results2);

        // 统计结果
        boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted)
                && results2.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
}
