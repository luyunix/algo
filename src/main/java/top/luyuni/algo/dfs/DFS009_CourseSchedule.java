package top.luyuni.algo.dfs;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * DFS009 - 课程表
 */
public class DFS009_CourseSchedule {

 public boolean canFinish(int numCourses, int[][] prerequisites) {
    List<List<Integer>> graph = new ArrayList<>();
   for (int i = 0; i < numCourses; i++) {
    graph.add(new ArrayList<>());
   }
   for (int[] pair : prerequisites) {
    graph.get(pair[1]).add(pair[0]);
   }

   int[] visited = new int[numCourses]; // 0:未访问，1:访问中，2:已访问
   for (int i = 0; i < numCourses; i++) {
    if (hasCycle(graph, i, visited)) return false;
   }
 return true;
    }

 private boolean hasCycle(List<List<Integer>> graph, int node, int[] visited) {
    if (visited[node] == 1) return true;  // 发现环
   if (visited[node] == 2) return false; // 已访问

   visited[node] = 1; // 标记为访问中
   for (int neighbor : graph.get(node)) {
    if (hasCycle(graph, neighbor, visited)) return true;
   }
   visited[node] = 2; // 标记为已访问
 return false;
    }

 public static void main(String[] args) {
    DFS009_CourseSchedule solution = new DFS009_CourseSchedule();

    JudgeEngine<TestInput, Boolean> engine = new JudgeEngine<>();
  engine.addTestCase("numCourses=2,prerequisites=[[1,0]]",
        new TestInput(2, new int[][]{{1,0}}), true, "可以完成")
        .addTestCase("numCourses=2,prerequisites=[[1,0],[0,1]]",
        new TestInput(2, new int[][]{{1,0},{0,1}}), false, "有环无法完成")
        .addTestCase("numCourses=3,prerequisites=[[1,0],[2,0]]",
        new TestInput(3, new int[][]{{1,0},{2,0}}), true, "可以完成");

   System.out.println("=== 课程表测试 ===");
   List<JudgeResult> results = engine.judge(input ->
         solution.canFinish(input.numCourses, input.prerequisites)
     );
    JudgeReporter.printReport(results);

   boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
  System.exit(allPassed ? 0 : 1);
    }

 static class TestInput {
     int numCourses;
     int[][] prerequisites;
   TestInput(int numCourses, int[][] prerequisites) {
       this.numCourses = numCourses;
       this.prerequisites = prerequisites;
     }
 }
}
