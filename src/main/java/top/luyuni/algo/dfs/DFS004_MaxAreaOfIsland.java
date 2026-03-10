package top.luyuni.algo.dfs;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * DFS004 - 岛屿的最大面积
 */
public class DFS004_MaxAreaOfIsland {
    
 public int maxAreaOfIsland(int[][] grid) {
    if (grid == null || grid.length == 0) return 0;
    
   int maxArea = 0;
   int m = grid.length;
   int n = grid[0].length;
    
   for (int i = 0; i < m; i++) {
     for (int j = 0; j < n; j++) {
      if (grid[i][j] == 1) {
        maxArea = Math.max(maxArea, dfs(grid, i, j));
      }
     }
   }
 return maxArea;
    }
    
 private int dfs(int[][] grid, int i, int j) {
    int m = grid.length;
     int n = grid[0].length;
    
   if (i < 0 || i >= m || j < 0 || j >= n || grid[i][j] != 1) {
      return 0;
     }
    
   grid[i][j] = 0;
   return 1 + dfs(grid, i-1, j) + dfs(grid, i+1, j) + 
            dfs(grid, i, j-1) + dfs(grid, i, j+1);
    }
    
 public static void main(String[] args) {
    DFS004_MaxAreaOfIsland solution = new DFS004_MaxAreaOfIsland();
    
    JudgeEngine<int[][], Integer> engine = new JudgeEngine<>();
  engine.addTestCase("示例 1", new int[][]{
        {0,0,1,0,0,0,0,1,0,0,0,0,0},
        {0,0,0,0,0,0,0,1,1,1,0,0,0},
        {0,1,1,0,1,0,0,0,0,0,0,0,0},
        {0,1,0,0,1,1,0,0,1,0,1,0,0},
        {0,1,0,0,1,1,0,0,1,1,1,0,0},
        {0,0,0,0,0,0,0,0,0,0,1,0,0},
        {0,0,0,0,0,0,0,1,1,1,0,0,0},
        {0,0,0,0,0,0,0,1,1,0,0,0,0}
     }, 6, "最大面积为 6")
        .addTestCase("全是水", new int[][]{{0,0},{0,0}}, 0, "没有岛屿")
        .addTestCase("全是陆地", new int[][]{{1,1},{1,1}}, 4, "整个都是岛")
        .addTestCase("单个陆地", new int[][]{{0,0,0},{0,1,0},{0,0,0}}, 1, "只有一个 1");
    
   System.out.println("=== 岛屿最大面积测试 ===");
   List<JudgeResult> results = engine.judge(input -> solution.maxAreaOfIsland(input));
    JudgeReporter.printReport(results);
    
   boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
  System.exit(allPassed ? 0 : 1);
    }
}
