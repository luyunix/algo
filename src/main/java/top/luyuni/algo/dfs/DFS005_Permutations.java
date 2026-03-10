package top.luyuni.algo.dfs;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * DFS005 - 全排列
 */
public class DFS005_Permutations {

    public List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        boolean[] used = new boolean[nums.length];
        backtrack(nums, new ArrayList<>(), used, result);
        return result;
    }

    private void backtrack(int[] nums, List<Integer> path, boolean[] used, List<List<Integer>> result) {
        if (path.size() == nums.length) {
            result.add(new ArrayList<>(path));
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            if (used[i]) continue;
            used[i] = true;
            path.add(nums[i]);
            backtrack(nums, path, used, result);
            path.remove(path.size() - 1);
            used[i] = false;
        }
    }

    public static void main(String[] args) {
        DFS005_Permutations solution = new DFS005_Permutations();

        JudgeEngine<int[], List<List<Integer>>> engine = new JudgeEngine<>();
        engine.addTestCase("nums=[1,2,3]", new int[]{1, 2, 3},
                        Arrays.asList(Arrays.asList(1, 2, 3), Arrays.asList(1, 3, 2),
                                Arrays.asList(2, 1, 3), Arrays.asList(2, 3, 1),
                                Arrays.asList(3, 1, 2), Arrays.asList(3, 2, 1)),
                        "6 种全排列",
                        (expected, actual) -> comparePermutations(expected, actual))
                .addTestCase("nums=[0,1]", new int[]{0, 1},
                        Arrays.asList(Arrays.asList(0, 1), Arrays.asList(1, 0)),
                        "2 种全排列",
                        (expected, actual) -> comparePermutations(expected, actual))
                .addTestCase("nums=[1]", new int[]{1},
                        Arrays.asList(Arrays.asList(1)),
                        "只有 1 种",
                        (expected, actual) -> comparePermutations(expected, actual));

        System.out.println("=== 全排列测试 ===");
        List<JudgeResult> results = engine.judge(input -> solution.permute(input));
        JudgeReporter.printReport(results);

        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }

    private static boolean comparePermutations(List<List<Integer>> expected, List<List<Integer>> actual) {
        if (expected.size() != actual.size()) return false;
        Set<List<Integer>> expectedSet = new HashSet<>(expected);
        for (List<Integer> perm : actual) {
            if (!expectedSet.contains(perm)) return false;
        }
        return true;
    }
}
