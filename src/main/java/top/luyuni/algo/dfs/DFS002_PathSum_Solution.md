# DFS002 - 路径总和 题解

## 思路是怎么想出来的

### 第一步：明确问题

```
问题：给定二叉树和目标值targetSum，判断是否存在从根到叶子的路径，路径上节点值之和等于targetSum

示例树：
      5
     / \
    4   8
   /   / \
  11  13  4
 /  \      \
7    2      1

targetSum = 22

路径 5→4→11→2 的和 = 5+4+11+2 = 22 ✓
```

### 第二步：发现规律

```
关键洞察：
- 从根节点开始，沿着路径累加
- 到叶子节点时，判断和是否等于targetSum
- 这是一个典型的DFS路径搜索问题

递归思路：
- 当前节点为null，返回false
- 当前节点是叶子，判断sum + node.val == targetSum
- 否则，递归检查左右子树，目标值减去当前节点值
```

### 第三步：设计算法

```
DFS函数：hasPathSum(node, remainingSum)
- remainingSum：还需要凑的值

步骤：
1. 如果node为null，返回false
2. 如果node是叶子节点，返回node.val == remainingSum
3. 递归检查左子树：hasPathSum(node.left, remainingSum - node.val)
4. 递归检查右子树：hasPathSum(node.right, remainingSum - node.val)
5. 左右子树有一个满足即可
```

---

## 解法：DFS路径搜索

```java
public boolean hasPathSum(TreeNode root, int targetSum) {
    // 空节点
    if (root == null) return false;
    
    // 叶子节点
    if (root.left == null && root.right == null) {
        return root.val == targetSum;
    }
    
    // 非叶子节点，递归检查左右子树
    int remaining = targetSum - root.val;
    return hasPathSum(root.left, remaining) || 
           hasPathSum(root.right, remaining);
}
```

### 完整执行图解

```
树：
      5
     / \
    4   8
   /   / \
  11  13  4
 /  \      \
7    2      1

targetSum = 22

========== DFS过程 ==========

hasPathSum(5, 22):
┌─────────────────────────────────────────┐
│  5不是叶子                               │
│  remaining = 22 - 5 = 17                 │
│  检查左：hasPathSum(4, 17)               │
│  检查右：hasPathSum(8, 17)               │
└─────────────────────────────────────────┘

  hasPathSum(4, 17):
  ┌─────────────────────────────────────┐
  │  4不是叶子                           │
  │  remaining = 17 - 4 = 13             │
  │  左：hasPathSum(11, 13)              │
  │  右：null                            │
  └─────────────────────────────────────┘

    hasPathSum(11, 13):
    ┌─────────────────────────────────┐
    │  11不是叶子                      │
    │  remaining = 13 - 11 = 2         │
    │  左：hasPathSum(7, 2)            │
    │  右：hasPathSum(2, 2)            │
    └─────────────────────────────────┘

      hasPathSum(7, 2):
      ┌─────────────────────────────┐
      │  7是叶子                     │
      │  7 == 2 ? false              │
      └─────────────────────────────┘
      返回false

      hasPathSum(2, 2):
      ┌─────────────────────────────┐
      │  2是叶子                     │
      │  2 == 2 ? true ✓             │
      └─────────────────────────────┘
      返回true

    返回true（左false || 右true）

  返回true

返回true ✓

路径：5 → 4 → 11 → 2，和为22
```

### 递归调用栈

```
hasPathSum(5, 22)
  hasPathSum(4, 17)
    hasPathSum(11, 13)
      hasPathSum(7, 2) → false
      hasPathSum(2, 2) → true ✓
    返回true
  返回true
返回true
```

---

## 边界 Case

**Case 1: 空树**
```
root = null
返回false ✓
```

**Case 2: 只有根节点，且值等于targetSum**
```
    5
targetSum = 5
是叶子，5==5，返回true ✓
```

**Case 3: 只有根节点，值不等于targetSum**
```
    5
targetSum = 3
是叶子，5!=3，返回false ✓
```

**Case 4: 没有满足条件的路径**
```
    1
   / \
  2   3
targetSum = 10

1→2：和为3
1→3：和为4
都≠10，返回false ✓
```

**Case 5: 负数情况**
```
    -2
   /
  -3
targetSum = -5

-2 + (-3) = -5，返回true ✓
```

---

## 记忆口诀

```
路径总和DFS，从根到叶来搜索
遇到叶子就判断，值等目标和就赢
不是叶子继续走，目标减去当前值
左右子树有一个，满足条件就返回
```
