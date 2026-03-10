package top.luyuni.algo.dfs;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * DFS006 - 子集
 */
public class DFS006_Subsets {

    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(nums, 0, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(int[] nums, int start, List<Integer> path, List<List<Integer>> result) {
        result.add(new ArrayList<>(path));
        for (int i = start; i < nums.length; i++) {
            path.add(nums[i]);
            backtrack(nums, i + 1, path, result);
            path.remove(path.size() - 1);
        }
    }

    public static void main(String[] args) {
        DFS006_Subsets solution = new DFS006_Subsets();

        JudgeEngine<int[], List<List<Integer>>> engine = new JudgeEngine<>();
        engine.addTestCase("nums=[1,2,3]", new int[]{1, 2, 3},
                        Arrays.asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2), Arrays.asList(3),
                                Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3)),
                        "8 个子集",
                        (expected, actual) -> compareSubsets(expected, actual))
                .addTestCase("nums=[0]", new int[]{0},
                        Arrays.asList(Arrays.asList(), Arrays.asList(0)),
                        "2 个子集",
                        (expected, actual) -> compareSubsets(expected, actual));

        System.out.println("=== 子集测试 ===");
        List<JudgeResult> results = engine.judge(input -> solution.subsets(input));
        JudgeReporter.printReport(results);

        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }

    private static boolean compareSubsets(List<List<Integer>> expected, List<List<Integer>> actual) {
        if (expected.size() != actual.size()) return false;
        Set<Set<Integer>> expectedSet = new HashSet<>();
        for (List<Integer> subset : expected) {
            expectedSet.add(new HashSet<>(subset));
        }
        for (List<Integer> subset : actual) {
            if (!expectedSet.contains(new HashSet<>(subset))) return false;
        }
        return true;
    }
}
