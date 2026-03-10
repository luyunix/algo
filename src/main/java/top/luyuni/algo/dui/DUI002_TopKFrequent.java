package top.luyuni.algo.dui;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeResult;
import top.luyuni.algo.oj.core.JudgeReporter;

import java.util.*;

/**
 * DUI002 - 前 K 个高频元素
 */
public class DUI002_TopKFrequent {
    
 public int[] topKFrequent(int[] nums, int k) {
      Map<Integer, Integer> freqMap = new HashMap<>();
     for (int num : nums) {
         freqMap.put(num, freqMap.getOrDefault(num, 0) + 1);
     }
      
     PriorityQueue<Map.Entry<Integer, Integer>> pq = 
         new PriorityQueue<>((a, b) -> a.getValue() - b.getValue());
      
     for (Map.Entry<Integer, Integer> entry : freqMap.entrySet()) {
        if (pq.size() < k) {
             pq.offer(entry);
         } else if (entry.getValue() > pq.peek().getValue()) {
             pq.poll();
             pq.offer(entry);
         }
     }
      
     int[] result = new int[k];
     for (int i = 0; i < k; i++) {
        result[i] = pq.poll().getKey();
     }
      
    return result;
  }
    
 public static void main(String[] args) {
      DUI002_TopKFrequent solution = new DUI002_TopKFrequent();
      
      JudgeEngine<TestInput, int[]> engine = new JudgeEngine<>();
      engine.addTestCase("示例 1", new TestInput(new int[]{1,1,1,2,2,3}, 2), 
              new int[]{2,1}, "前 2 个高频元素", 
              (expected, actual) -> arraysEqualIgnoreOrder(expected, actual))
          .addTestCase("示例 2", new TestInput(new int[]{1}, 1), 
              new int[]{1}, "单个元素",
              (expected, actual) -> arraysEqualIgnoreOrder(expected, actual))
          .addTestCase("多个相同元素", new TestInput(new int[]{1,1,1,1}, 1), 
              new int[]{1}, "频率最高的",
              (expected, actual) -> arraysEqualIgnoreOrder(expected, actual))
          .addTestCase("不同频率", new TestInput(new int[]{4,1,-1,2,-1,2,3}, 3), 
              new int[]{1,2,-1}, "前 3 个高频",
              (expected, actual) -> arraysEqualIgnoreOrder(expected, actual));
      
    System.out.println("=== 堆 + 哈希表方法测试 ===");
    List<JudgeResult> results = engine.judge(input -> 
          solution.topKFrequent(input.nums, input.k)
      );
      JudgeReporter.printReport(results);
      
      boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
    System.exit(allPassed ? 0 : 1);
  }
  
 private static boolean arraysEqualIgnoreOrder(int[] a, int[] b) {
  if (a.length != b.length) return false;
     // 创建副本避免修改原数组
    int[] sortedA = Arrays.copyOf(a, a.length);
    int[] sortedB = Arrays.copyOf(b, b.length);
    Arrays.sort(sortedA);
    Arrays.sort(sortedB);
 return Arrays.equals(sortedA, sortedB);
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
