package top.luyuni.algo.ef;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * EF001 - 寻找旋转排序数组中的最小值
 */
public class EF001_FindMinRotatedArray {

   public int findMin(int[] nums) {
       if (nums == null || nums.length == 0) return -1;

        int left = 0, right = nums.length - 1;
        while (left < right) {
            int mid = left + (right - left) / 2;
           if (nums[mid] > nums[right]) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
       return nums[left];
    }

   public int findMinWithDuplicates(int[] nums) {
       if (nums == null || nums.length == 0) return -1;

        int left = 0, right = nums.length -1;
        while (left < right) {
            int mid = left + (right - left) / 2;
           if (nums[mid] > nums[right]) {
                left = mid + 1;
            } else if (nums[mid] < nums[right]) {
                right = mid;
            } else {
                right--;
            }
        }
       return nums[left];
    }

   public static void main(String[] args) {
        EF001_FindMinRotatedArray solution = new EF001_FindMinRotatedArray();

        JudgeEngine<TestInput, Integer> engine = new JudgeEngine<>();
        engine.addTestCase("无重复：旋转 3 次", new TestInput(new int[]{3,4,5,1,2}), 1, "基本功能测试")
            .addTestCase("无重复：旋转 4 次", new TestInput(new int[]{4,5,6,7,0,1,2}), 0, "基本功能测试")
            .addTestCase("无重复：未旋转", new TestInput(new int[]{11,13,15,17}), 11, "边界情况")
            .addTestCase("无重复：单元素", new TestInput(new int[]{1}), 1, "边界情况");

       System.out.println("=== 无重复元素版本 ===");
       List<JudgeResult> results = engine.judge(input -> solution.findMin(input.nums));
        JudgeReporter.printReport(results);

        JudgeEngine<TestInput, Integer> engine2 = new JudgeEngine<>();
        engine2.addTestCase("有重复：最小值在中间", new TestInput(new int[]{2,2,2,0,1}), 0, "基本功能测试")
            .addTestCase("有重复：全是重复", new TestInput(new int[]{2,2,2,2,2}), 2, "基本功能测试")
            .addTestCase("有重复：首尾重复", new TestInput(new int[]{1,1,1,0,1}), 0, "基本功能测试");

       System.out.println("\n=== 有重复元素版本 ===");
       List<JudgeResult> results2 = engine2.judge(input -> solution.findMinWithDuplicates(input.nums));
        JudgeReporter.printReport(results2);

        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted) &&
                          results2.stream().allMatch(JudgeResult::isAccepted);
       System.exit(allPassed ? 0 : 1);
    }

   static class TestInput {
        int[] nums;
       TestInput(int[] nums) { this.nums = nums; }
    }
}
