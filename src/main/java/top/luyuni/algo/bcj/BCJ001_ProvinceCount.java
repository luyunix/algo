package top.luyuni.algo.bcj;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.List;

/**
 * BCJ001 - 省份数量（并查集）
 */
public class BCJ001_ProvinceCount {
    
 public int findCircleNum(int[][] isConnected) {
    int n = isConnected.length;
     UnionFind uf = new UnionFind(n);
     for (int i = 0; i < n; i++) {
       for (int j = i + 1; j < n; j++) {
        if (isConnected[i][j] == 1) {
          uf.union(i, j);
         }
       }
     }
   return uf.getCount();
    }
    
 public int findCircleNumDFS(int[][] isConnected) {
    int n = isConnected.length;
     boolean[] visited = new boolean[n];
     int count = 0;
     for (int i = 0; i < n; i++) {
      if (!visited[i]) {
        dfs(isConnected, visited, i);
       count++;
      }
     }
   return count;
    }
    
 private void dfs(int[][] isConnected, boolean[] visited, int i) {
    visited[i] = true;
     for (int j = 0; j < isConnected.length; j++) {
      if (isConnected[i][j] == 1 && !visited[j]) {
        dfs(isConnected, visited, j);
      }
     }
    }
    
 static class UnionFind {
     int[] parent;
     int[] rank;
     int count;
     
  public UnionFind(int n) {
       parent = new int[n];
       rank = new int[n];
       count = n;
       for (int i = 0; i < n; i++) {
        parent[i] = i;
       }
     }
     
  public int find(int x) {
      if (parent[x] != x) {
        parent[x] = find(parent[x]);
      }
     return parent[x];
     }
     
  public void union(int x, int y) {
      int rootX = find(x);
      int rootY = find(y);
      if (rootX == rootY) return;
      
      if (rank[rootX] < rank[rootY]) {
        parent[rootX] = rootY;
      } else if (rank[rootX] > rank[rootY]) {
        parent[rootY] = rootX;
      } else {
        parent[rootY] = rootX;
        rank[rootX]++;
      }
     count--;
     }
     
  public int getCount() {
      return count;
     }
 }
    
 public static void main(String[] args) {
    BCJ001_ProvinceCount solution = new BCJ001_ProvinceCount();
    
    JudgeEngine<int[][], Integer> engine = new JudgeEngine<>();
  engine.addTestCase("示例 1", new int[][]{{1,1,0},{1,1,0},{0,0,1}}, 2, "2 个省份")
        .addTestCase("示例 2", new int[][]{{1,0,0},{0,1,0},{0,0,1}}, 3, "3 个省份")
        .addTestCase("示例 3", new int[][]{{1,1,1},{1,1,1},{1,1,1}}, 1, "1 个省份")
        .addTestCase("单个城市", new int[][]{{1}}, 1, "只有 1 个城市")
        .addTestCase("4 城市", new int[][]{{1,0,0,1},{0,1,1,0},{0,1,1,0},{1,0,0,1}}, 2, "2 个省份");
    
   System.out.println("=== 并查集方法测试 ===");
   List<JudgeResult> results1 = engine.judge(input -> solution.findCircleNum(input));
    JudgeReporter.printReport(results1);
    
  System.out.println("\n=== DFS 方法测试 ===");
  List<JudgeResult> results2 = engine.judge(input -> solution.findCircleNumDFS(input));
   JudgeReporter.printReport(results2);
    
   boolean allPassed = results1.stream().allMatch(JudgeResult::isAccepted) &&
                     results2.stream().allMatch(JudgeResult::isAccepted);
  System.exit(allPassed ? 0 : 1);
    }
}
