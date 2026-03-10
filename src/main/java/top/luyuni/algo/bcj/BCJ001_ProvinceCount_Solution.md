# BCJ001 - 省份数量 题解

## 核心概念

**问题本质**：求无向图的连通分量数量

**并查集思想**：把相连的城市合并到同一个集合，最后统计集合数量

---

## 解法1：并查集

### 并查集模板

```java
class UnionFind {
    int[] parent;  // parent[i] = i的父节点
    int[] rank;    // rank[i] = i所在树的高度
    int count;     // 连通分量数量
    
    public UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;  // 初始化：每个元素自成一个集合
            rank[i] = 1;
        }
        count = n;  // 初始有n个连通分量
    }
    
    // 查找根节点（带路径压缩）
    public int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);  // 递归路径压缩
        }
        return parent[x];
    }
    
    // 合并两个集合（按秩合并）
    public void union(int x, int y) {
        int px = find(x), py = find(y);
        if (px == py) return;  // 已经在同一集合
        
        // 小树挂在大树下
        if (rank[px] < rank[py]) {
            parent[px] = py;
        } else if (rank[px] > rank[py]) {
            parent[py] = px;
        } else {
            parent[py] = px;
            rank[px]++;
        }
        count--;  // 连通分量减1
    }
    
    public int getCount() {
        return count;
    }
}
```

### 解题代码

```java
public int findCircleNum(int[][] isConnected) {
    int n = isConnected.length;
    UnionFind uf = new UnionFind(n);
    
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            if (isConnected[i][j] == 1) {
                uf.union(i, j);  // 合并相连的城市
            }
        }
    }
    
    return uf.getCount();
}
```

## 完整执行过程图解

### Case 1: 基础案例

**输入**: isConnected = [[1,1,0],[1,1,0],[0,0,1]]

表示：城市0和城市1相连，城市2独立

```
初始状态（3个集合）：
parent: [0, 1, 2]   // 每个城市指向自己
rank:   [1, 1, 1]   // 每个树高度为1
count:  3           // 3个连通分量

      0     1     2
      ↑     ↑     ↑
     自己  自己  自己

第1步：合并 0 和 1
- find(0) = 0, find(1) = 1
- rank[0] == rank[1] == 1
- 把 1 挂在 0 下面，rank[0]++ = 2

parent: [0, 0, 2]   // 1指向0
rank:   [2, 1, 1]
count:  2           // 连通分量减1

      0     2
     / ↑     ↑
    1  自己  自己

第2步：检查 0 和 2
- isConnected[0][2] = 0，不相连，跳过

第3步：检查 1 和 2
- isConnected[1][2] = 0，不相连，跳过

最终结果：
parent: [0, 0, 2]
count:  2

集合1: {0, 1}   集合2: {2}
省份数量 = 2 ✓
```

### Case 2: 查找时的路径压缩

**输入**: isConnected = [[1,1,1],[1,1,0],[1,0,1]]

```
初始：
parent: [0, 1, 2]
rank:   [1, 1, 1]

第1步：合并 0 和 1
- find(0)=0, find(1)=1
- rank相等，1挂到0下

parent: [0, 0, 2]
rank:   [2, 1, 1]

     0
    / ↑
   1   2

第2步：合并 0 和 2
- find(0)=0, find(2)=2
- rank[0]=2 > rank[2]=1，2挂到0下

parent: [0, 0, 0]
rank:   [2, 1, 1]

     0
   / | \
  1  2  自己

第3步：检查 1 和 2
- find(1)：parent[1]=0，直接返回0
- find(2)：parent[2]=0，直接返回0
- 已经在同一集合，跳过

最终结果：
parent: [0, 0, 0]
count:  1

所有城市连通，1个省份 ✓
```

### Case 3: 多轮合并与路径压缩

**输入**: isConnected = [[1,0,0,1],[0,1,1,0],[0,1,1,0],[1,0,0,1]]

表示：{0,3}相连，{1,2}相连

```
初始：
parent: [0, 1, 2, 3]
rank:   [1, 1, 1, 1]

第1步：合并 0 和 3
- find(0)=0, find(3)=3
- rank相等，3挂到0下

parent: [0, 1, 2, 0]
rank:   [2, 1, 1, 1]

  0     1     2
 / ↑     ↑     ↑
3  自己  自己  自己

第2步：合并 1 和 2
- find(1)=1, find(2)=2
- rank相等，2挂到1下

parent: [0, 1, 1, 0]
rank:   [2, 2, 1, 1]

  0       1
 / ↑     / ↑
3  自己 2  自己

第3步：检查 0 和 2
- isConnected[0][2] = 0，不相连

第4步：检查 1 和 3
- isConnected[1][3] = 0，不相连

最终结果：
count:  2

集合1: {0, 3}   集合2: {1, 2}
省份数量 = 2 ✓
```

### Case 4: 复杂路径压缩

**输入**: isConnected = [[1,1,0,0],[1,1,1,0],[0,1,1,1],[0,0,1,1]]

```
初始：
parent: [0, 1, 2, 3]

第1步：合并 0 和 1
parent: [0, 0, 2, 3]

     0
    / ↑
   1   2     3

第2步：合并 1 和 2
- find(1)：parent[1]=0，返回0
- find(2)=2
- 0和2合并，2挂到0下

parent: [0, 0, 0, 3]

     0
   / | \
  1  2  3

第3步：合并 2 和 3
- find(2)：parent[2]=0，返回0
- find(3)=3
- 0和3合并，3挂到0下

parent: [0, 0, 0, 0]

     0
   / | | \
  1  2 3  自己

最终结果：
count:  1

所有城市连通 ✓
```

---

## 解法2：DFS

```java
public int findCircleNumDFS(int[][] isConnected) {
    int n = isConnected.length;
    boolean[] visited = new boolean[n];
    int provinces = 0;
    
    for (int i = 0; i < n; i++) {
        if (!visited[i]) {
            dfs(isConnected, visited, i);
            provinces++;
        }
    }
    
    return provinces;
}

private void dfs(int[][] isConnected, boolean[] visited, int city) {
    visited[city] = true;
    
    for (int j = 0; j < isConnected.length; j++) {
        if (isConnected[city][j] == 1 && !visited[j]) {
            dfs(isConnected, visited, j);
        }
    }
}
```

---

## 两种方法对比

| 方法  | 时间复杂度        | 空间复杂度 | 适用场景           |
|-----|--------------|-------|----------------|
| 并查集 | O(n² × α(n)) | O(n)  | 动态连通性，需要频繁合并查询 |
| DFS | O(n²)        | O(n)  | 一次性遍历所有连通分量    |

注：α(n) 是阿克曼函数的反函数，可以认为是一个很小的常数（< 5）

---

## 并查集的两个优化

### 1. 路径压缩

**作用**：查找时把节点直接挂到根，下次查找更快

**代码**：

```java
public int find(int x) {
    if (parent[x] != x) {
        parent[x] = find(parent[x]);  // 路径压缩
    }
    return parent[x];
}
```

### 2. 按秩合并

**作用**：把小树挂到大树下，控制树的高度

**代码**：

```java
public void union(int x, int y) {
    int px = find(x), py = find(y);
    if (px == py) return;
    
    if (rank[px] < rank[py]) {
        parent[px] = py;
    } else if (rank[px] > rank[py]) {
        parent[py] = px;
    } else {
        parent[py] = px;
        rank[px]++;
    }
}
```

---

## 记忆口诀

```
并查集三要素：找根、合并、计数
路径压缩快，按秩合并稳
省份数量题，连通分量数
相连就合并，最后数集合
```
