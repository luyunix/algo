# 股票买卖问题专题详解

> 股票买卖是经典的动态规划模型，核心在于状态的巧妙设计。

## 核心思想

股票问题的关键在于：**每天结束时，要么持有股票，要么不持有股票**。这两种状态会影响后续的决策。

## 状态设计模板

```
dp[i][0] = 第i天结束后不持有股票的最大收益
dp[i][1] = 第i天结束后持有股票的最大收益
```

## 题目变种

### 买卖股票的最佳时机 I（只能买卖一次）

**限制**：只能完成一笔交易。

**状态转移**：

```
dp[i][0] = max(dp[i-1][0], dp[i-1][1] + prices[i])  // 不持有：昨天就不持有 或 今天卖出
dp[i][1] = max(dp[i-1][1], -prices[i])              // 持有：昨天就持有 或 今天买入（首次）
```

**简化版**（贪心）：

```java
int minPrice = Integer.MAX_VALUE;
int maxProfit = 0;

for (int price : prices) {
    minPrice = Math.min(minPrice, price);
    maxProfit = Math.max(maxProfit, price - minPrice);
}
```

### 买卖股票的最佳时机 II（可以买卖多次）

**限制**：可以完成多笔交易，但不能同时持有多个。

**状态转移**：

```
dp[i][0] = max(dp[i-1][0], dp[i-1][1] + prices[i])  // 不持有：昨天就不持有 或 今天卖出
dp[i][1] = max(dp[i-1][1], dp[i-1][0] - prices[i])  // 持有：昨天就持有 或 今天买入
```

**简化版**（贪心）：

```java
int profit = 0;
for (int i = 1; i < prices.length; i++) {
    if (prices[i] > prices[i-1]) {
        profit += prices[i] - prices[i-1];  // 有涨就卖
    }
}
```

### 买卖股票的最佳时机 III（最多买卖两次）

**限制**：最多完成两笔交易。

**状态扩展**：

```
dp[i][0] = 从未交易，不持有
dp[i][1] = 第一次持有
dp[i][2] = 第一次交易后不持有
dp[i][3] = 第二次持有
dp[i][4] = 第二次交易后不持有
```

**状态转移**：

```
dp[i][0] = 0
dp[i][1] = max(dp[i-1][1], dp[i-1][0] - prices[i])  // 第一次买入
dp[i][2] = max(dp[i-1][2], dp[i-1][1] + prices[i])  // 第一次卖出
dp[i][3] = max(dp[i-1][3], dp[i-1][2] - prices[i])  // 第二次买入
dp[i][4] = max(dp[i-1][4], dp[i-1][3] + prices[i])  // 第二次卖出
```

### 买卖股票的最佳时机 IV（最多买卖 k 次）

**限制**：最多完成 k 笔交易。

**通用模板**：

```java
public int maxProfit(int k, int[] prices) {
    int n = prices.length;
    if (n <= 1 || k == 0) return 0;

    // 如果k很大，相当于无限次交易
    if (k >= n / 2) {
        int profit = 0;
        for (int i = 1; i < n; i++) {
            if (prices[i] > prices[i-1]) {
                profit += prices[i] - prices[i-1];
            }
        }
        return profit;
    }

    // dp[j][0] = 第j次交易后不持有
    // dp[j][1] = 第j次交易中持有
    int[][] dp = new int[k + 1][2];
    for (int j = 0; j <= k; j++) {
        dp[j][1] = Integer.MIN_VALUE;  // 初始化为负无穷
    }

    for (int price : prices) {
        for (int j = 1; j <= k; j++) {
            dp[j][0] = Math.max(dp[j][0], dp[j][1] + price);   // 卖出
            dp[j][1] = Math.max(dp[j][1], dp[j-1][0] - price); // 买入
        }
    }

    return dp[k][0];
}
```

### 最佳买卖时机含冷冻期

**限制**：卖出后有一天冷冻期，不能买入。

**状态扩展**：

```
dp[i][0] = 不持有，不在冷冻期
dp[i][1] = 持有
dp[i][2] = 不持有，在冷冻期（今天刚卖出）
```

**状态转移**：

```
dp[i][0] = max(dp[i-1][0], dp[i-1][2])           // 昨天就不持有 或 冷冻期结束
dp[i][1] = max(dp[i-1][1], dp[i-1][0] - prices[i]) // 昨天就持有 或 今天买入（昨天不持有且非冷冻）
dp[i][2] = dp[i-1][1] + prices[i]                 // 今天卖出（昨天必须持有）
```

### 买卖股票含手续费

**限制**：每笔交易需要支付手续费。

**状态转移**：

```
dp[i][0] = max(dp[i-1][0], dp[i-1][1] + prices[i])        // 不持有
dp[i][1] = max(dp[i-1][1], dp[i-1][0] - prices[i] - fee)  // 持有（买入时交手续费）
```

## 状态设计总结

| 题目 | 状态数 | 关键设计 |
|-----|-------|---------|
| 只能买卖一次 | 2 | dp[i][1]只能从-price转移 |
| 可以买卖多次 | 2 | dp[i][1]从dp[i-1][0]-price转移 |
| 最多k次 | 2k | 按交易次数分层 |
| 含冷冻期 | 3 | 增加冷冻期状态 |
| 含手续费 | 2 | 买入或卖出时扣除手续费 |

## 通用解题模板

```java
public int stockDP(int[] prices, int k, int cooldown, int fee) {
    int n = prices.length;
    if (n <= 1) return 0;

    // 状态：dp[i][j] 表示第i天处于状态j的最大收益
    // j=0: 不持有, j=1: 持有
    // 根据题目扩展状态数

    int[][] dp = new int[n][2 + extraStates];
    dp[0][1] = -prices[0];  // 第一天买入

    for (int i = 1; i < n; i++) {
        // 根据具体题目填写状态转移
        dp[i][0] = ...;
        dp[i][1] = ...;
    }

    return dp[n-1][0];  // 最终不持有股票
}
```

## 记忆口诀

```
股票问题看持有，两种状态要分清
不持持有和冷冻，根据题意来变形
一次交易负无穷，多次交易用前一
k次交易要分层，冷冻费用看仔细
```

## 相关题目

| 题目 | 限制条件 | LeetCode |
|-----|---------|----------|
| 买卖股票的最佳时机 I | 只能一次 | 121 |
| 买卖股票的最佳时机 II | 无限次 | 122 |
| 买卖股票的最佳时机 III | 最多两次 | 123 |
| 买卖股票的最佳时机 IV | 最多k次 | 188 |
| 最佳买卖时机含冷冻期 | 冷冻1天 | 309 |
| 买卖股票含手续费 | 每笔手续费 | 714 |
