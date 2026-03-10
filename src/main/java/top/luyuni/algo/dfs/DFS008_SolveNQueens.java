package top.luyuni.algo.dfs;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * DFS008 - N 皇后
 */
public class DFS008_SolveNQueens {
    
 public List<List<String>> solveNQueens(int n) {
    List<List<String>> result = new ArrayList<>();
   char[][] board = new char[n][n];
   for (char[] row : board) Arrays.fill(row, '.');
   backtrack(board, 0, result);
 return result;
    }
    
 private void backtrack(char[][] board, int row, List<List<String>> result) {
    if (row == board.length) {
      result.add(construct(board));
     return;
     }
    
   for (int col = 0; col < board.length; col++) {
     if (!isValid(board, row, col)) continue;
     board[row][col] = 'Q';
     backtrack(board, row + 1, result);
     board[row][col] = '.';
     }
    }
    
 private boolean isValid(char[][] board, int row, int col) {
    int n = board.length;
   // 检查列
   for (int i = 0; i < row; i++) {
    if (board[i][col] == 'Q') return false;
   }
   // 检查左上对角线
   for (int i = row -1, j = col -1; i >= 0 && j >= 0; i--, j--) {
    if (board[i][j] == 'Q') return false;
   }
   // 检查右上对角线
   for (int i = row -1, j = col +1; i >= 0 && j < n; i--, j++) {
    if (board[i][j] == 'Q') return false;
   }
 return true;
    }
    
 private List<String> construct(char[][] board) {
    List<String> res = new ArrayList<>();
   for (char[] row : board) {
    res.add(new String(row));
   }
 return res;
    }
    
 public static void main(String[] args) {
    DFS008_SolveNQueens solution = new DFS008_SolveNQueens();
    
    JudgeEngine<Integer, Integer> engine = new JudgeEngine<>();
  engine.addTestCase("n=4", 4, 2, "4 皇后有 2 个解")
        .addTestCase("n=1", 1, 1, "1 皇后只有 1 个解");
    
   System.out.println("=== N 皇后测试 ===");
   List<JudgeResult> results = engine.judge(input -> 
         solution.solveNQueens(input).size()
     );
    JudgeReporter.printReport(results);
    
   boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
  System.exit(allPassed ? 0 : 1);
    }
}
