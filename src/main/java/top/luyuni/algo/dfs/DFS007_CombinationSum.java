package top.luyuni.algo.dfs;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * DFS007 - 组合总和
 */
public class DFS007_CombinationSum {
    
 public List<List<Integer>> combinationSum(int[] candidates, int target) {
    List<List<Integer>> result = new ArrayList<>();
   backtrack(candidates, target, 0, new ArrayList<>(), result);
 return result;
    }
    
 private void backtrack(int[] candidates, int remaining, int start, List<Integer> path, List<List<Integer>> result) {
    if (remaining == 0) {
      result.add(new ArrayList<>(path));
     return;
     }
   if (remaining < 0) return;
    
   for (int i = start; i < candidates.length; i++) {
     path.add(candidates[i]);
     backtrack(candidates, remaining - candidates[i], i, path, result);
     path.remove(path.size() -1);
     }
    }
    
 public static void main(String[] args) {
    DFS007_CombinationSum solution = new DFS007_CombinationSum();
    
    JudgeEngine<TestInput, List<List<Integer>>> engine = new JudgeEngine<>();
  engine.addTestCase("candidates=[2,3,6,7],target=7", 
        new TestInput(new int[]{2,3,6,7}, 7),
        Arrays.asList(Arrays.asList(2,2,3), Arrays.asList(7)),
        "和为 7 的组合",
        (expected, actual) -> compareCombinations(expected, actual))
        .addTestCase("candidates=[2,3,5],target=8",
        new TestInput(new int[]{2,3,5}, 8),
        Arrays.asList(Arrays.asList(2,2,2,2), Arrays.asList(2,3,3), Arrays.asList(3,5)),
        "和为 8 的组合",
        (expected, actual) -> compareCombinations(expected, actual));
    
   System.out.println("=== 组合总和测试 ===");
   List<JudgeResult> results = engine.judge(input -> 
         solution.combinationSum(input.candidates, input.target)
     );
    JudgeReporter.printReport(results);
    
   boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
  System.exit(allPassed ? 0 : 1);
    }
    
 private static boolean compareCombinations(List<List<Integer>> expected, List<List<Integer>> actual) {
   if (expected.size() != actual.size()) return false;
   Set<List<Integer>> expectedSet = new HashSet<>(expected);
   for (List<Integer> comb : actual) {
    Collections.sort(comb);
    if (!expectedSet.contains(comb)) return false;
   }
 return true;
   }
    
 static class TestInput {
     int[] candidates;
     int target;
   TestInput(int[] candidates, int target) {
       this.candidates = candidates;
       this.target = target;
     }
 }
}
