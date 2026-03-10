package top.luyuni.algo.ef;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * EF004 - 寻找峰值
 */
public class EF004_FindPeak {
    
  public int findPeakElement(int[] nums) {
      if (nums == null || nums.length == 0) return -1;
      if (nums.length == 1) return 0;
        
        int left = 0, right = nums.length -1;
        while (left < right) {
            int mid = left + (right - left) / 2;
          if (nums[mid] < nums[mid +1]) {
                // 在上坡，峰值在右边
                left = mid + 1;
            } else {
                // 在下坡或已在峰值
                right = mid;
            }
        }
      return left;
    }
    
  public static void main(String[] args) {
        EF004_FindPeak solution = new EF004_FindPeak();
        
        JudgeEngine<TestInput, Integer> engine = new JudgeEngine<>();
        engine.addTestCase("示例 1：峰值在中间", new TestInput(new int[]{1,2,3,1}), 2, "基本功能测试",
                (expected, actual) -> isPeak(actual, new int[]{1,2,3,1}))
            .addTestCase("示例 2：多个峰值", new TestInput(new int[]{1,2,1,3,5,6,4}), null, "基本功能测试",
                (expected, actual) -> isPeak(actual, new int[]{1,2,1,3,5,6,4}))
            .addTestCase("单调递增", new TestInput(new int[]{1,2,3,4,5}), 4, "边界情况",
                (expected, actual) -> isPeak(actual, new int[]{1,2,3,4,5}))
            .addTestCase("单调递减", new TestInput(new int[]{5,4,3,2,1}), 0, "边界情况",
                (expected, actual) -> isPeak(actual, new int[]{5,4,3,2,1}))
            .addTestCase("单元素", new TestInput(new int[]{1}), 0, "边界情况",
                (expected, actual) -> isPeak(actual, new int[]{1}))
            .addTestCase("两个元素，升序", new TestInput(new int[]{1,2}), 1, "边界情况",
                (expected, actual) -> isPeak(actual, new int[]{1,2}))
            .addTestCase("两个元素，降序", new TestInput(new int[]{2,1}), 0, "边界情况",
                (expected, actual) -> isPeak(actual, new int[]{2,1}));
        
      System.out.println("=== 寻找峰值 ===");
      List<JudgeResult> results = engine.judge(input -> 
            solution.findPeakElement(input.nums)
        );
        JudgeReporter.printReport(results);
        
        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
      System.exit(allPassed ? 0 : 1);
    }
    
  private static boolean isPeak(int index, int[] nums) {
      if (index < 0 || index >= nums.length) return false;
       int left = index > 0 ? nums[index - 1] : Integer.MIN_VALUE;
       int right = index < nums.length -1 ? nums[index + 1] : Integer.MIN_VALUE;
      return nums[index] > left && nums[index] > right;
    }
    
  static class TestInput {
        int[] nums;
      TestInput(int[] nums) {
            this.nums = nums;
        }
    }
}
