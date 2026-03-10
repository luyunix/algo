# DFS008 - N皇后 题解

## 思路是怎么想出来的

### 第一步：明确问题

```
问题：在n×n的棋盘上放置n个皇后，使它们互不攻击
皇后可以攻击：同行、同列、同对角线

示例：n=4

解法1：
. Q . .
. . . Q
Q . . .
. . Q .

解法2：
. . Q .
Q . . .
. . . Q
. Q . .

输出：2种解法
```

### 第二步：发现规律

```
关键洞察：
- 每行只能放一个皇后（否则同行攻击）
- 每列只能放一个皇后（否则同列攻击）
- 对角线不能放（两条对角线）

逐行放置：
- 第0行放皇后，有n个位置可选
- 第1行放皇后，避开第0行的列和对角线
- ...
- 第n-1行放完，找到一个解

这是一个典型的DFS+回溯问题！
```

### 第三步：设计算法

```
用数组queens[n]记录：
- queens[row] = col 表示第row行第col列放皇后

DFS(row)：在第row行放置皇后
1. 如果row == n，所有皇后放置完毕，找到一个解
2. 遍历第row行的每一列col：
   a. 检查(row, col)是否与已放置的皇后冲突
   b. 如果不冲突，放置皇后：queens[row] = col
   c. DFS(row + 1)
   d. 回溯：撤销放置

冲突检查：
- 同列：queens[i] == col
- 对角线：|row - i| == |col - queens[i]|
```

---

## 解法：DFS + 回溯 + 剪枝

```java
public List<List<String>> solveNQueens(int n) {
    List<List<String>> result = new ArrayList<>();
    int[] queens = new int[n];  // queens[row] = col
    
    dfs(0, n, queens, result);
    return result;
}

private void dfs(int row, int n, int[] queens, List<List<String>> result) {
    // 所有皇后放置完毕
    if (row == n) {
        result.add(generateBoard(queens, n));
        return;
    }
    
    // 尝试第row行的每一列
    for (int col = 0; col < n; col++) {
        if (isValid(row, col, queens)) {
            queens[row] = col;  // 放置皇后
            dfs(row + 1, n, queens, result);
            // 回溯：自动撤销，因为下次会覆盖queens[row]
        }
    }
}

// 检查(row, col)是否可以放置皇后
private boolean isValid(int row, int col, int[] queens) {
    for (int i = 0; i < row; i++) {
        // 同列
        if (queens[i] == col) return false;
        
        // 对角线
        if (Math.abs(row - i) == Math.abs(col - queens[i])) {
            return false;
        }
    }
    return true;
}

// 生成棋盘
private List<String> generateBoard(int[] queens, int n) {
    List<String> board = new ArrayList<>();
    for (int i = 0; i < n; i++) {
        char[] row = new char[n];
        Arrays.fill(row, '.');
        row[queens[i]] = 'Q';
        board.add(new String(row));
    }
    return board;
}
```

### 完整执行图解（n=4）

```
========== DFS过程 ==========

dfs(row=0):
┌─────────────────────────────────────────┐
│  第0行尝试col=0,1,2,3                    │
│  col=0: 放置皇后queens[0]=0              │
│  递归dfs(1)                              │
└─────────────────────────────────────────┘

  dfs(row=1):
  ┌─────────────────────────────────────┐
  │  检查col=0: queens[0]=0，同列冲突    │
  │  检查col=1: |1-0|==|1-0|，对角线冲突 │
  │  检查col=2: 不冲突！                 │
  │  queens[1]=2，递归dfs(2)             │
  └─────────────────────────────────────┘

    dfs(row=2):
    ┌─────────────────────────────────┐
    │  col=0: |2-0|==|0-0|? 2!=0，但queens[0]=0同列? 否
    │         |2-1|==|0-2|? 1!=2，不冲突...等等
    │  实际上：检查与queens[0]=0和queens[1]=2
    │  col=0: 与queens[0]同列? 否，对角线? |2-0|==|0-0|否
    │         与queens[1]对角线? |2-1|==|0-2|否
    │  但等等，col=0和queens[0]=0同列！冲突
    │  col=1: 与queens[1]=2对角线? |2-1|==|1-2|是！冲突
    │  col=2: 与queens[1]同列！冲突
    │  col=3: 与queens[0]对角线? |2-0|==|3-0|否
    │         与queens[1]对角线? |2-1|==|3-2|是！冲突
    │  无可用列，返回                    │
    └─────────────────────────────────┘

  回溯，尝试col=3
  queens[1]=3，递归dfs(2)

    dfs(row=2):
    ┌─────────────────────────────────┐
    │  col=0: 检查通过！               │
    │  queens[2]=0，递归dfs(3)         │
    └─────────────────────────────────┘

      dfs(row=3):
      ┌─────────────────────────────┐
      │  col=0,1,2都冲突             │
      │  col=3: 与queens[2]=0对角线? |3-2|==|3-0|否
      │         与queens[1]=3同列！冲突
      │  无可用列，返回                │
      └─────────────────────────────┘

    回溯，尝试其他col...
    col=2: 通过，queens[2]=2
      dfs(3): col=1通过！
      queens=[0,3,2,1]，找到一个解！

回溯到row=0，尝试col=1...
最终找到另一个解 queens=[2,0,3,1]

结果：
[.Q..]    [..Q.]
[...Q]    [Q...]
[Q...]    [...Q]
[..Q.]    [.Q..]
```

---

## 边界 Case

**Case 1: n=1**

```
[Q]
只有一个解 ✓
```

**Case 2: n=2, n=3**

```
无解
返回[] ✓
```

**Case 3: n=4**

```
2个解（如上）✓
```

**Case 4: n=8**

```
92个解（经典8皇后问题）✓
```

---

## 记忆口诀

```
N皇后问题经典，逐行放置不会错
每行尝试每列放，检查冲突再决定
同列对角都不能，通过检查就放置
递归下一行继续，放完N行就找到
回溯撤销再尝试，所有解都能找到
```
