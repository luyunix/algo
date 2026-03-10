package top.luyuni.algo.dfs;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * DFS010 - 太平洋大西洋水流问题
 */
public class DFS010_PacificAtlantic {

 public List<List<Integer>> pacificAtlantic(int[][] heights) {
    List<List<Integer>> result = new ArrayList<>();
    if (heights == null || heights.length == 0) return result;

   int m = heights.length;
   int n = heights[0].length;
   boolean[][] pacific = new boolean[m][n];
   boolean[][] atlantic = new boolean[m][n];

   // 从太平洋边界开始 DFS
   for (int i = 0; i < m; i++) {
    dfs(heights, pacific, i, 0);
    dfs(heights, atlantic, i, n - 1);
   }
   for (int j = 0; j < n; j++) {
    dfs(heights, pacific, 0, j);
    dfs(heights, atlantic, m - 1, j);
   }

   // 收集既能流到太平洋又能流到大西洋的点
   for (int i = 0; i < m; i++) {
    for (int j = 0; j < n; j++) {
     if (pacific[i][j] && atlantic[i][j]) {
      result.add(Arrays.asList(i, j));
     }
    }
   }
 return result;
    }

 private void dfs(int[][] heights, boolean[][] visited, int i, int j) {
    int m = heights.length;
     int n = heights[0].length;

   if (i < 0 || j < 0 || i >= m || j >= n || visited[i][j]) return;

   visited[i][j] = true;
   int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
   for (int[] dir : dirs) {
    int ni = i + dir[0];
    int nj = j + dir[1];
    if (ni >= 0 && nj >= 0 && ni < m && nj < n &&
        heights[ni][nj] >= heights[i][j]) {
     dfs(heights, visited, ni, nj);
    }
   }
    }

 public static void main(String[] args) {
    DFS010_PacificAtlantic solution = new DFS010_PacificAtlantic();

    int[][] heights = {
      {1,2,2,3,5},
      {3,2,3,4,4},
      {2,4,5,3,1},
      {6,7,1,4,5},
      {5,1,1,2,4}
     };

    JudgeEngine<int[][], Integer> engine = new JudgeEngine<>();
  engine.addTestCase("示例", heights, 7, "7 个点可以流到两个大洋");

   System.out.println("=== 太平洋大西洋测试 ===");
   List<JudgeResult> results = engine.judge(input ->
         solution.pacificAtlantic(input).size()
     );
    JudgeReporter.printReport(results);

   boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
  System.exit(allPassed ? 0 : 1);
    }
}
