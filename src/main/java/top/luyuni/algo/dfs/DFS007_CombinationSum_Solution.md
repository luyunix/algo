# DFS007 - 组合总和 题解

## 思路是怎么想出来的

### 第一步：明确问题

```
问题：给定数组candidates和目标值target，找出所有和为target的组合
- 数组无重复元素
- 每个数字可以无限次使用

示例：
candidates = [2,3,6,7], target = 7

组合：
- [2,2,3] = 7
- [7] = 7

输出：[[2,2,3], [7]]
```

### 第二步：发现规律

```
关键洞察：
- 每个数字可以选多次（完全背包思想）
- 需要记录当前和，和等于target时加入结果
- 当前和超过target时，剪枝返回

与子集问题的区别：
- 子集：每个元素选或不选
- 组合总和：每个元素可以选0次、1次、多次
```

### 第三步：设计算法

```
需要维护：
1. 当前路径（path）
2. 当前和（sum）
3. 起始索引（start，避免重复组合）

DFS步骤：
1. 如果sum == target，找到一个组合，加入结果
2. 如果sum > target，剪枝返回
3. 从start开始遍历：
   a. 选择candidates[i]
   b. sum += candidates[i]
   c. 递归，还是从i开始（可以重复选）
   d. 回溯：撤销选择，sum减去
```

---

## 解法：DFS + 回溯 + 剪枝

```java
public List<List<Integer>> combinationSum(int[] candidates, int target) {
    List<List<Integer>> result = new ArrayList<>();
    List<Integer> path = new ArrayList<>();
    
    dfs(candidates, 0, target, 0, path, result);
    return result;
}

private void dfs(int[] candidates, int start, int target, int sum,
                 List<Integer> path, List<List<Integer>> result) {
    // 找到一个组合
    if (sum == target) {
        result.add(new ArrayList<>(path));
        return;
    }
    
    // 剪枝：超过目标值
    if (sum > target) {
        return;
    }
    
    // 从start开始选择
    for (int i = start; i < candidates.length; i++) {
        // 做选择
        path.add(candidates[i]);
        sum += candidates[i];
        
        // 递归，还是从i开始（可以重复选）
        dfs(candidates, i, target, sum, path, result);
        
        // 撤销选择（回溯）
        sum -= candidates[i];
        path.remove(path.size() - 1);
    }
}
```

### 完整执行图解

```
candidates = [2,3,6,7], target = 7

========== DFS过程 ==========

dfs([], start=0, sum=0):
┌─────────────────────────────────────────┐
│  sum=0 < 7，继续                         │
│  i=0: 选择2，path=[2], sum=2             │
│  递归dfs([2], 0, 2)                      │
└─────────────────────────────────────────┘

  dfs([2], start=0, sum=2):
  ┌─────────────────────────────────────┐
  │  sum=2 < 7，继续                     │
  │  i=0: 选择2，path=[2,2], sum=4       │
  │  递归dfs([2,2], 0, 4)                │
  └─────────────────────────────────────┘

    dfs([2,2], start=0, sum=4):
    ┌─────────────────────────────────┐
    │  sum=4 < 7，继续                 │
    │  i=0: 选择2，path=[2,2,2], sum=6 │
    │  递归dfs([2,2,2], 0, 6)          │
    └─────────────────────────────────┘

      dfs([2,2,2], start=0, sum=6):
      ┌─────────────────────────────┐
      │  sum=6 < 7，继续             │
      │  i=0: 选择2，sum=8 > 7，剪枝 │
      │  i=1: 选择3，path=[2,2,2,3]  │
      │  sum=9 > 7，剪枝             │
      │  i=2,3都剪枝                 │
      │  返回                        │
      └─────────────────────────────┘

    回溯：撤销2，path=[2,2], sum=4
    
    i=1: 选择3，path=[2,2,3], sum=7
    递归dfs([2,2,3], 1, 7)

      dfs([2,2,3], start=1, sum=7):
      ┌─────────────────────────────┐
      │  sum=7 == target！           │
      │  加入结果[2,2,3]             │
      │  返回                        │
      └─────────────────────────────┘

    回溯：撤销3，path=[2,2], sum=4
    i=2,3都剪枝

  回溯：撤销2，path=[2], sum=2
  
  i=1: 选择3，path=[2,3], sum=5
  递归dfs([2,3], 1, 5)
    ...（继续搜索，但无法凑出7）

  i=2,3都剪枝

回溯：撤销2，path=[], sum=0

i=1: 选择3，path=[3], sum=3
递归dfs([3], 1, 3)
  ...（无法凑出7）

i=2: 选择6，path=[6], sum=6
递归dfs([6], 2, 6)
  i=2: 选择6，sum=12 > 7，剪枝

i=3: 选择7，path=[7], sum=7
递归dfs([7], 3, 7)
  sum=7 == target！
  加入结果[7]

结果：[[2,2,3], [7]] ✓
```

### 决策树可视化

```
                                    []
                    ┌───────────────┼───────────────┐
                    2               3               6    7
                   /|\              |               |    |
                 2  3 6 7          ...             ...  ...
                /|
               2 3 6 7
              /
             2 3 6 7
            /
           剪枝(>7)

找到[2,2,3]和[7]
```

---

## 关键点：为什么从i开始而不是i+1

```
组合总和：可以重复选同一个元素
- 递归调用dfs(candidates, i, ...)
- 下次还可以选candidates[i]

子集问题：每个元素只能选一次
- 递归调用dfs(nums, i+1, ...)
- 下次从i+1开始，不能重复选
```

---

## 边界 Case

**Case 1: 无解**

```
candidates = [2,4], target = 3
2和4都是偶数，无法凑出奇数3
返回[] ✓
```

**Case 2: target等于某个元素**

```
candidates = [2,3,5], target = 5
返回[[5], [2,3]] ✓
```

**Case 3: 需要多次选择同一个元素**

```
candidates = [2], target = 4
返回[[2,2]] ✓
```

**Case 4: 空数组**

```
candidates = [], target = 1
返回[] ✓
```

---

## 记忆口诀

```
组合总和DFS，每个数字可重复
当前和要记录，等于目标就加入
超过目标要剪枝，减少无效搜索
从i开始递归，可以重复选择
回溯撤销选择，尝试其他可能
```
