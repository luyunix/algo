package top.luyuni.algo.dfs;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * DFS003 - 岛屿数量
 */
public class DFS003_NumberOfIslands {
    
 public int numIslands(char[][] grid) {
    if (grid == null || grid.length == 0) return 0;
    
   int count = 0;
   int m = grid.length;
   int n = grid[0].length;
    
   for (int i = 0; i < m; i++) {
     for (int j = 0; j < n; j++) {
      if (grid[i][j] == '1') {
        dfs(grid, i, j);
       count++;
      }
     }
   }
 return count;
    }
    
 private void dfs(char[][] grid, int i, int j) {
    int m = grid.length;
     int n = grid[0].length;
    
   if (i < 0 || j < 0 || i >= m || j >= n || grid[i][j] == '0') {
      return;
     }
    
   grid[i][j] = '0';
   dfs(grid, i + 1, j);
   dfs(grid, i - 1, j);
   dfs(grid, i, j + 1);
   dfs(grid, i, j - 1);
    }
    
 public static void main(String[] args) {
    DFS003_NumberOfIslands solution= new DFS003_NumberOfIslands();
    
    JudgeEngine<char[][], Integer> engine = new JudgeEngine<>();
  engine.addTestCase("示例 1", new char[][]{
        {'1','1','1','1','0'},
        {'1','1','0','1','0'},
        {'1','1','0','0','0'},
        {'0','0','0','0','0'}
     }, 1, "所有陆地都连通")
        .addTestCase("示例 2", new char[][]{
        {'1','1','0','0','0'},
        {'1','1','0','0','0'},
        {'0','0','1','0','0'},
        {'0','0','0','1','1'}
     }, 3, "三个独立的岛屿")
        .addTestCase("空网格", new char[][]{}, 0, "空网格")
        .addTestCase("全水", new char[][]{
        {'0','0','0'},
        {'0','0','0'}
     }, 0, "没有陆地");
    
   System.out.println("=== 岛屿数量测试 ===");
   List<JudgeResult> results = engine.judge(input -> solution.numIslands(input));
    JudgeReporter.printReport(results);
    
   boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
  System.exit(allPassed ? 0 : 1);
    }
}
