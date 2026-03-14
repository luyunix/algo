# 特殊DP类别详解

> 树形DP、区间DP、数位DP是三种重要的特殊动态规划类型，在面试中经常出现。

## 目录

- [树形DP](#树形dp)
- [区间DP](#区间dp)
- [数位DP](#数位dp)

---

## 树形DP

### 什么是树形DP

树形DP是在树结构上进行的动态规划。由于树具有天然的递归结构，树形DP通常采用自底向上（后序遍历）的方式计算。

### 核心思想

1. **定义状态**：`dp[u]` 表示以节点 `u` 为根的子树的状态
2. **状态转移**：`dp[u] = f(dp[v1], dp[v2], ...)`，其中 `v` 是 `u` 的子节点
3. **遍历顺序**：后序遍历（先处理子节点，再处理父节点）

### 经典例题1：树的最大独立集

**问题**：从树中选择若干节点，使得任意两个节点不相邻，求最大权值和。

**状态定义**：

```
dp[u][0] = 不选节点u时，以u为根的子树的最大权值
dp[u][1] = 选节点u时，以u为根的子树的最大权值
```

**状态转移**：

```
// 不选u：子节点可选可不选
dp[u][0] = Σ max(dp[v][0], dp[v][1])  // v是u的子节点

// 选u：子节点不能选
dp[u][1] = weight[u] + Σ dp[v][0]
```

**代码实现**：

```java
void dfs(int u, int parent) {
    dp[u][1] = weight[u];  // 选u

    for (int v : adj[u]) {
        if (v == parent) continue;
        dfs(v, u);

        dp[u][0] += Math.max(dp[v][0], dp[v][1]);  // 不选u，子节点可选可不选
        dp[u][1] += dp[v][0];  // 选u，子节点不能选
    }
}
```

### 经典例题2：树的直径

**问题**：求树中最长路径的长度。

**状态定义**：

```
dp[u] = 从u出发向下的最长路径长度
```

**状态转移**：

```java
int diameter = 0;

int dfs(int u, int parent) {
    int max1 = 0, max2 = 0;  // 最长的两条分支

    for (int v : adj[u]) {
        if (v == parent) continue;
        int len = dfs(v, u) + 1;  // 经过边(u,v)的最长路径

        if (len > max1) {
            max2 = max1;
            max1 = len;
        } else if (len > max2) {
            max2 = len;
        }
    }

    // 经过u的最长路径 = 最长分支 + 次长分支
    diameter = Math.max(diameter, max1 + max2);

    return max1;  // 返回最长分支
}
```

### 经典例题3：换根DP

**问题**：求每个节点作为根时，某个值的大小。

**核心思想**：先任选一个根（如1）计算一次DP，然后通过第二次DFS利用父节点的信息推导其他节点作为根的情况。

```java
// 第一次DFS：计算以1为根的信息
void dfs1(int u, int parent) {
    dp[u] = initial_value;
    for (int v : adj[u]) {
        if (v == parent) continue;
        dfs1(v, u);
        dp[u] = combine(dp[u], dp[v]);
    }
}

// 第二次DFS：换根
dp2[u] = dp[u];  // 换根后u为根的答案

void dfs2(int u, int parent) {
    int m = children.size();

    // 前缀和+后缀和优化
    for (int v : adj[u]) {
        if (v == parent) continue;

        // 从u的dp中移除v的贡献
        int original_u = dp[u];
        dp[u] = remove(dp[u], dp[v]);

        // v换根后获得u的信息
        dp[v] = combine(dp[v], dp[u]);

        dfs2(v, u);

        // 恢复
        dp[u] = original_u;
    }
}
```

---

## 区间DP

### 什么是区间DP

区间DP是解决与**区间**相关问题的动态规划方法。大区间的解依赖于小区间的解，通常按区间长度从小到大枚举。

### 核心特征

1. **状态定义**：`dp[i][j]` 表示区间 `[i, j]` 的答案
2. **状态转移**：枚举分割点 `k`，`dp[i][j] = f(dp[i][k], dp[k+1][j])`
3. **遍历顺序**：按区间长度从小到大

### 经典例题1：石子合并

**问题**：有n堆石子排成一列，每次合并相邻两堆，代价为两堆石子数之和，求合并成一堆的最小代价。

**状态定义**：

```
dp[i][j] = 合并区间[i,j]的最小代价
```

**状态转移**：

```
dp[i][j] = min(dp[i][k] + dp[k+1][j] + sum[i][j]) for k in [i, j)
```

**代码实现**：

```java
int n = stones.length;
int[][] dp = new int[n][n];
int[] prefix = new int[n + 1];

// 计算前缀和
for (int i = 0; i < n; i++) {
    prefix[i + 1] = prefix[i] + stones[i];
}

// 按区间长度枚举
for (int len = 2; len <= n; len++) {  // 区间长度
    for (int i = 0; i + len - 1 < n; i++) {  // 起点
        int j = i + len - 1;  // 终点
        dp[i][j] = Integer.MAX_VALUE;

        for (int k = i; k < j; k++) {  // 分割点
            int cost = dp[i][k] + dp[k + 1][j] + prefix[j + 1] - prefix[i];
            dp[i][j] = Math.min(dp[i][j], cost);
        }
    }
}

return dp[0][n - 1];
```

### 经典例题2：最长回文子序列

**问题**：求字符串的最长回文子序列长度。

**状态定义**：

```
dp[i][j] = 区间[i,j]的最长回文子序列长度
```

**状态转移**：

```
if (s[i] == s[j]):
    dp[i][j] = dp[i+1][j-1] + 2  // 两端相同，都选
else:
    dp[i][j] = max(dp[i+1][j], dp[i][j-1])  // 选较大的
```

**边界**：`dp[i][i] = 1`

### 经典例题3：矩阵链乘法

**问题**：给定矩阵链，求最优的乘法顺序，使得计算次数最少。

**状态定义**：

```
dp[i][j] = 计算矩阵i到j的最少乘法次数
```

**状态转移**：

```
dp[i][j] = min(dp[i][k] + dp[k+1][j] + p[i-1]*p[k]*p[j]) for k in [i, j)
```

---

## 数位DP

### 什么是数位DP

数位DP用于解决**统计数字个数**的问题，通常是在区间 `[L, R]` 中统计满足某种条件的数字个数。

### 核心思想

将数字按位拆分，从高位到低位递归处理，用记忆化搜索优化。

### 状态设计

```
dp[pos][tight][leadingZero][otherStates]

- pos: 当前处理的位置（第几位）
- tight: 是否紧贴上限（前几位是否与上界相同）
  - 1: 紧贴，当前位最大只能填limit[pos]
  - 0: 不紧贴，当前位可以填0-9
- leadingZero: 是否有前导零
  - 1: 还在前导零阶段
  - 0: 已经填了非零数字
- otherStates: 根据具体问题添加的状态
```

### 经典例题：统计[l,r]中不含49的数字个数

```java
char[] digits;
int[][][] dp;  // dp[pos][tight][has4]

// has4: 上一位是否是4

int dfs(int pos, boolean tight, boolean has4, boolean leadingZero) {
    if (pos == digits.length) return 1;  // 构造完成

    int t = tight ? 1 : 0;
    int h = has4 ? 1 : 0;
    int l = leadingZero ? 1 : 0;

    if (dp[pos][t][h] != -1 && !tight) {
        return dp[pos][t][h];
    }

    int limit = tight ? digits[pos] - '0' : 9;
    int ans = 0;

    for (int i = 0; i <= limit; i++) {
        boolean nextTight = tight && (i == limit);
        boolean nextLeadingZero = leadingZero && (i == 0);
        boolean nextHas4 = !nextLeadingZero && (i == 4);

        // 不能出现49
        if (has4 && i == 9) continue;

        ans += dfs(pos + 1, nextTight, nextHas4, nextLeadingZero);
    }

    if (!tight) dp[pos][t][h] = ans;
    return ans;
}

int solve(int n) {
    digits = String.valueOf(n).toCharArray();
    dp = new int[digits.length][2][2];
    for (int[][] arr : dp) for (int[] a : arr) Arrays.fill(a, -1);
    return dfs(0, true, false, true);
}

// 答案 = solve(R) - solve(L-1)
```

### 数位DP模板

```java
class DigitDP {
    char[] digits;
    int[][][] memo;

    // 主函数
    int count(int n) {
        digits = String.valueOf(n).toCharArray();
        memo = new int[digits.length][2][...];
        for (int[][] arr : memo) for (int[] a : arr) Arrays.fill(a, -1);
        return dfs(0, true, true, ...);
    }

    // 记忆化搜索
    int dfs(int pos, boolean tight, boolean leadingZero, ...) {
        if (pos == digits.length) {
            return check(...);  // 判断是否满足条件
        }

        int t = tight ? 1 : 0;
        int l = leadingZero ? 1 : 0;
        ...

        if (memo[pos][t][l] != -1 && !tight) {
            return memo[pos][t][l];
        }

        int limit = tight ? digits[pos] - '0' : 9;
        int ans = 0;

        for (int i = 0; i <= limit; i++) {
            // 递归处理下一位
            ans += dfs(pos + 1,
                      tight && i == limit,
                      leadingZero && i == 0,
                      ...);
        }

        if (!tight) memo[pos][t][l] = ans;
        return ans;
    }
}
```

---

## 三种特殊DP对比

| 类型 | 数据结构 | 遍历顺序 | 核心思想 |
|-----|---------|---------|---------|
| 树形DP | 树 | 后序遍历 | 子树信息汇总到父节点 |
| 区间DP | 线性数组 | 区间长度从小到大 | 大区间由小区间合并 |
| 数位DP | 数字的各位 | 从高位到低位 | 记忆化搜索 + 状态压缩 |

## 记忆口诀

```
树形DP后序走，子节点信息父节点收
区间DP长度序，分割点处找最优
数位DP位上搜，紧贴前导要记录
记忆化加剪枝，三大特殊要熟透
```

## 练习题目推荐

### 树形DP
- LeetCode 337. 打家劫舍 III
- LeetCode 124. 二叉树中的最大路径和
- LeetCode 543. 二叉树的直径

### 区间DP
- LeetCode 516. 最长回文子序列
- LeetCode 1312. 让字符串成为回文串的最少插入次数
- LeetCode 1000. 合并石头的最低成本

### 数位DP
- LeetCode 902. 最大为 N 的数字组合
- LeetCode 1012. 至少有 1 位重复的数字
- LeetCode 233. 数字 1 的个数
