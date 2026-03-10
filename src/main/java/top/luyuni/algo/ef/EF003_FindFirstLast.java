package top.luyuni.algo.ef;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * EF003 - 在排序数组中查找元素的第一个和最后一个位置
 */
public class EF003_FindFirstLast {
    
  public int[] searchRange(int[] nums, int target) {
      if (nums == null || nums.length == 0) return new int[]{-1, -1};
        
        int leftIdx = findLeft(nums, target);
        int rightIdx = findRight(nums, target);
        
       if (leftIdx > rightIdx) return new int[]{-1, -1};
        
      return new int[]{leftIdx, rightIdx};
    }
    
  private int findLeft(int[] nums, int target) {
        int left = 0, right = nums.length - 1;
        int result = -1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
          if (nums[mid] >= target) {
               if (nums[mid] == target) result = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
      return result;
    }
    
  private int findRight(int[] nums, int target) {
        int left = 0, right = nums.length -1;
        int result = -1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
          if (nums[mid] <= target) {
               if (nums[mid] == target) result = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
      return result;
    }
    
  public static void main(String[] args) {
        EF003_FindFirstLast solution = new EF003_FindFirstLast();
        
        JudgeEngine<TestInput, int[]> engine = new JudgeEngine<>();
        engine.addTestCase("target出现两次", new TestInput(new int[]{5,7,7,8,8,10}, 8), 
                new int[]{3,4}, "基本功能测试", Arrays::equals)
            .addTestCase("target出现一次", new TestInput(new int[]{5,7,7,8,8,10}, 7), 
                new int[]{1,2}, "基本功能测试", Arrays::equals)
            .addTestCase("target不存在", new TestInput(new int[]{5,7,7,8,8,10}, 6), 
                new int[]{-1,-1}, "边界情况", Arrays::equals)
            .addTestCase("target在开头", new TestInput(new int[]{5,5,5,8,8,10}, 5), 
                new int[]{0,2}, "边界情况", Arrays::equals)
            .addTestCase("target在末尾", new TestInput(new int[]{5,7,7,8,10,10}, 10), 
                new int[]{4,5}, "边界情况", Arrays::equals)
            .addTestCase("单元素，命中", new TestInput(new int[]{5}, 5), 
                new int[]{0,0}, "边界情况", Arrays::equals)
            .addTestCase("单元素，未命中", new TestInput(new int[]{5}, 4), 
                new int[]{-1,-1}, "边界情况", Arrays::equals)
            .addTestCase("空数组", new TestInput(new int[]{}, 0), 
                new int[]{-1,-1}, "边界情况", Arrays::equals);
        
      System.out.println("=== 查找元素的第一个和最后一个位置 ===");
      List<JudgeResult> results = engine.judge(input -> 
            solution.searchRange(input.nums, input.target)
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
