package top.luyuni.algo.dui;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * DUI001 - 数组中的第 K 个最大元素
 */
public class DUI001_KthLargest {
    
  public int findKthLargest(int[] nums, int k) {
       PriorityQueue<Integer> pq = new PriorityQueue<>(k);
        
        for (int num : nums) {
           if (pq.size() < k) {
                pq.offer(num);
            } else if (num > pq.peek()) {
                pq.poll();
                pq.offer(num);
            }
        }
        
     return pq.peek();
    }
    
  public int findKthLargestQuickSelect(int[] nums, int k) {
      return quickSelect(nums, 0, nums.length -1, nums.length - k);
    }
    
  private int quickSelect(int[] nums, int left, int right, int k) {
      if (left == right) return nums[left];
        
        int pivot = nums[left];
        int i = left, j = right;
        
        while (i < j) {
          while (i < j && nums[j] >= pivot) j--;
           while (i < j && nums[i] <= pivot) i++;
          if (i < j) {
               int temp = nums[i];
               nums[i] = nums[j];
               nums[j] = temp;
            }
        }
        
       nums[left] = nums[i];
       nums[i] = pivot;
        
       if (k == i) return nums[k];
       else if (k < i) return quickSelect(nums, left, i - 1, k);
       else return quickSelect(nums, i + 1, right, k);
    }
    
  public static void main(String[] args) {
        DUI001_KthLargest solution = new DUI001_KthLargest();
        
        JudgeEngine<TestInput, Integer> engine = new JudgeEngine<>();
        engine.addTestCase("示例 1", new TestInput(new int[]{3,2,1,5,6,4}, 2), 5, "第 2 大是 5")
            .addTestCase("示例 2", new TestInput(new int[]{3,2,3,1,2,4,5,5,6}, 4), 4, "第 4 大是 4")
            .addTestCase("单个元素", new TestInput(new int[]{1}, 1), 1, "只有 1 个元素")
            .addTestCase("有负数", new TestInput(new int[]{-1,-2,-3,-4,-5}, 3), -3, "第 3 大是 -3")
            .addTestCase("重复元素", new TestInput(new int[]{2,2,2,2}, 2), 2, "所有元素相同");
        
      System.out.println("=== 小顶堆方法测试 ===");
      List<JudgeResult> results1 = engine.judge(input -> 
            solution.findKthLargest(input.nums, input.k)
        );
        JudgeReporter.printReport(results1);
        
        boolean allPassed1 = results1.stream().allMatch(JudgeResult::isAccepted);
        
      System.out.println("\n=== 快速选择方法测试 ===");
      List<JudgeResult> results2 = engine.judge(input -> 
            solution.findKthLargestQuickSelect(input.nums, input.k)
        );
        JudgeReporter.printReport(results2);
        
        boolean allPassed2 = results2.stream().allMatch(JudgeResult::isAccepted);
        
      System.exit((allPassed1 && allPassed2) ? 0 : 1);
    }
    
  static class TestInput {
        int[] nums;
        int k;
      TestInput(int[] nums, int k) {
            this.nums = nums;
            this.k = k;
        }
    }
}
