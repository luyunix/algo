# 背包问题专题详解

> 背包问题是动态规划的经典模型，掌握背包问题等于理解了一大类DP问题。

## 目录

- [01背包](#01背包)
- [完全背包](#完全背包)
- [多重背包](#多重背包)
- [二维费用背包](#二维费用背包)
- [分组背包](#分组背包)
- [有依赖的背包](#有依赖的背包)

---

## 01背包

### 问题描述

有 `n` 个物品，第 `i` 个物品重量为 `w[i]`，价值为 `v[i]`，背包容量为 `W`。每个物品只能选一次，求能装入的最大价值。

### 状态定义

```
dp[i][j] = 考虑前i个物品，容量为j时能获得的最大价值
```

### 状态转移

对于第 `i` 个物品，有两种选择：
- **不选**：`dp[i][j] = dp[i-1][j]`
- **选**：`dp[i][j] = dp[i-1][j-w[i]] + v[i]`（需要 j >= w[i]）

```
dp[i][j] = max(dp[i-1][j], dp[i-1][j-w[i]] + v[i])  (j >= w[i])
dp[i][j] = dp[i-1][j]                               (j < w[i])
```

### 代码实现

```java
// 二维版本
int[][] dp = new int[n + 1][W + 1];
for (int i = 1; i <= n; i++) {
    for (int j = 0; j <= W; j++) {
        dp[i][j] = dp[i-1][j];  // 不选第i个物品
        if (j >= w[i]) {
            dp[i][j] = Math.max(dp[i][j], dp[i-1][j-w[i]] + v[i]);
        }
    }
}
return dp[n][W];
```

### 空间优化（一维）

观察到 `dp[i]` 只依赖 `dp[i-1]`，可以用一维数组。注意：**容量需要倒序遍历**，防止重复选取。

```java
// 一维版本（空间优化）
int[] dp = new int[W + 1];
for (int i = 0; i < n; i++) {
    for (int j = W; j >= w[i]; j--) {  // 倒序遍历！
        dp[j] = Math.max(dp[j], dp[j - w[i]] + v[i]);
    }
}
return dp[W];
```

**为什么倒序？** 因为 `dp[j]` 依赖 `dp[j-w[i]]`，倒序保证 `dp[j-w[i]]` 还是上一轮（i-1）的值。

---

## 完全背包

### 问题描述

有 `n` 种物品，每种物品有无限个，第 `i` 种物品重量为 `w[i]`，价值为 `v[i]`，背包容量为 `W`。求能装入的最大价值。

### 状态转移

可以选 0 个、1 个、2 个...无限个第 `i` 种物品：

```
dp[i][j] = max(dp[i-1][j], dp[i][j-w[i]] + v[i])  // 注意：第二个是dp[i]不是dp[i-1]
```

### 代码实现

```java
// 二维版本
int[][] dp = new int[n + 1][W + 1];
for (int i = 1; i <= n; i++) {
    for (int j = 0; j <= W; j++) {
        dp[i][j] = dp[i-1][j];  // 不选
        if (j >= w[i]) {
            dp[i][j] = Math.max(dp[i][j], dp[i][j-w[i]] + v[i]);  // 选（注意是dp[i]）
        }
    }
}
```

### 空间优化（一维）

```java
// 一维版本
int[] dp = new int[W + 1];
for (int i = 0; i < n; i++) {
    for (int j = w[i]; j <= W; j++) {  // 正序遍历！
        dp[j] = Math.max(dp[j], dp[j - w[i]] + v[i]);
    }
}
```

**为什么正序？** 完全背包可以重复选取，`dp[j-w[i]]` 应该已经包含了当前物品，所以正序。

### 与01背包的区别

| 类型 | 遍历顺序 | 状态转移 |
|-----|---------|---------|
| 01背包 | 容量**倒序** | `dp[j] = max(dp[j], dp[j-w] + v)` |
| 完全背包 | 容量**正序** | `dp[j] = max(dp[j], dp[j-w] + v)` |

---

## 多重背包

### 问题描述

有 `n` 种物品，第 `i` 种物品有 `cnt[i]` 个，重量为 `w[i]`，价值为 `v[i]`。

### 方法一：二进制优化

将 `cnt[i]` 拆分成若干个2的幂次：1, 2, 4, ..., 2^k, 剩余部分。

例如：`cnt = 13` → 拆成 `1, 2, 4, 6`（1+2+4+6=13）

这样就将多重背包转化为01背包问题。

```java
List<int[]> items = new ArrayList<>();  // 存储(重量,价值)

for (int i = 0; i < n; i++) {
    int weight = w[i], value = v[i], count = cnt[i];
    // 二进制拆分
    for (int k = 1; count > 0; k <<= 1) {
        int use = Math.min(k, count);
        items.add(new int[]{weight * use, value * use});
        count -= use;
    }
}

// 转化为01背包
int[] dp = new int[W + 1];
for (int[] item : items) {
    for (int j = W; j >= item[0]; j--) {
        dp[j] = Math.max(dp[j], dp[j - item[0]] + item[1]);
    }
}
```

### 方法二：单调队列优化

对于每个余数类分别用单调队列优化，时间复杂度 O(nW)。

---

## 二维费用背包

### 问题描述

每个物品有两个费用（如重量和体积），背包有两个限制。

### 状态定义

```
dp[i][j] = 费用1不超过i，费用2不超过j时的最大价值
```

### 代码实现

```java
int[][] dp = new int[W1 + 1][W2 + 1];

for (int i = 0; i < n; i++) {
    for (int j = W1; j >= w1[i]; j--) {      // 倒序
        for (int k = W2; k >= w2[i]; k--) {  // 倒序
            dp[j][k] = Math.max(dp[j][k], dp[j-w1[i]][k-w2[i]] + v[i]);
        }
    }
}
```

---

## 分组背包

### 问题描述

物品分成若干组，每组只能选一个物品。

### 代码实现

```java
// groups[i] 表示第i组的物品列表
int[] dp = new int[W + 1];

for (int g = 0; g < groups.length; g++) {
    // 倒序遍历容量
    for (int j = W; j >= 0; j--) {
        // 枚举组内每个物品
        for (Item item : groups[g]) {
            if (j >= item.weight) {
                dp[j] = Math.max(dp[j], dp[j - item.weight] + item.value);
            }
        }
    }
}
```

**关键**：外层组循环，内层容量倒序，最内层枚举组内物品。

---

## 有依赖的背包

### 问题描述

物品之间有依赖关系，选主物品才能选附属物品。

### 解决方法：树形DP

将依赖关系看作一棵树，用树形DP处理。

```
状态：dp[u][j] = 以u为根的子树，容量为j时的最大价值
转移：类似分组背包，枚举在主物品u上分配多少容量
```

---

## 背包问题的变种

### 1. 求方案数

```java
// 完全背包求方案数（硬币组合）
int[] dp = new int[amount + 1];
dp[0] = 1;  // 凑成0元有1种方案

for (int coin : coins) {
    for (int j = coin; j <= amount; j++) {
        dp[j] += dp[j - coin];  // 累加方案数
    }
}
```

### 2. 求具体方案

记录选择路径：

```java
boolean[][] choice = new boolean[n][W + 1];
// ...
if (dp[j] < dp[j - w[i]] + v[i]) {
    dp[j] = dp[j - w[i]] + v[i];
    choice[i][j] = true;  // 记录选了物品i
}
```

### 3. 恰好装满

初始化改为 `-INF`，`dp[0] = 0`：

```java
Arrays.fill(dp, Integer.MIN_VALUE);
dp[0] = 0;
// 正常转移
```

---

## 记忆口诀

```
01背包倒序走，完全背包正序游
多重背包二进制，分组背包组内搜
二维费用加一维，依赖背包转树求
装满初始负无穷，方案数里加不够
```

## 相关题目

| 题目 | 类型 | 文件 |
|-----|------|------|
| 01背包 | 模板题 | [DT008_Knapsack01.java](DT008_Knapsack01.java) |
| 零钱兑换 | 完全背包 | [DT004_CoinChange.java](DT004_CoinChange.java) |
