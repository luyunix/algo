package top.luyuni.algo.ef;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * EF002 - 搜索插入位置
 */
public class EF002_SearchInsert {

    public int searchInsert(int[] nums, int target) {
        if (nums == null || nums.length == 0) return 0;

        int left = 0, right = nums.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (nums[mid] == target) {
                return mid;
            } else if (nums[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return left;
    }

    public static void main(String[] args) {
        EF002_SearchInsert solution = new EF002_SearchInsert();

        JudgeEngine<TestInput, Integer> engine = new JudgeEngine<>();
        engine.addTestCase("target在数组中", new TestInput(new int[]{1, 3, 5, 6}, 5), 2, "基本功能测试")
                .addTestCase("target不在，插入中间", new TestInput(new int[]{1, 3, 5, 6}, 2), 1, "基本功能测试")
                .addTestCase("target比所有大", new TestInput(new int[]{1, 3, 5, 6}, 7), 4, "边界情况")
                .addTestCase("target比所有小", new TestInput(new int[]{1, 3, 5, 6}, 0), 0, "边界情况")
                .addTestCase("单元素，相等", new TestInput(new int[]{1}, 1), 0, "边界情况")
                .addTestCase("单元素，不相等", new TestInput(new int[]{1}, 0), 0, "边界情况")
                .addTestCase("空数组", new TestInput(new int[]{}, 5), 0, "边界情况");

        System.out.println("=== 搜索插入位置 ===");
        List<JudgeResult> results = engine.judge(input ->
                solution.searchInsert(input.nums, input.target)
        );
        JudgeReporter.printReport(results);

        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }

    static class TestInput {
        int[] nums;
        int target;

        TestInput(int[] nums, int target) {
            this.nums = nums;
            this.target = target;
        }
    }
}
