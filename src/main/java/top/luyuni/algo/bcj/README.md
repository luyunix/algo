# BCJ 目录 - 并查集专题

这个目录收录并查集（Union-Find）相关的题目。

## 什么是并查集？

并查集是一种处理**不相交集合**的数据结构，支持两种操作：
- **Find**：查找元素属于哪个集合（找根）
- **Union**：合并两个集合

## 为什么学并查集？

| 优势 | 说明 |
|------|------|
| 时间复杂度低 | 近乎 O(1) 的查询和合并 |
| 代码简洁 | 核心代码不到20行 |
| 应用广泛 | 连通分量、最小生成树、最近公共祖先等 |

## 核心模板

```java
class UnionFind {
    int[] parent;  // parent[i] = i的父节点
    int[] rank;    // rank[i] = i所在树的高度（用于优化）
    
    public UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;  // 初始化：每个元素自成一个集合
            rank[i] = 1;
        }
    }
    
    // 查找根节点（带路径压缩）
    public int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);  // 路径压缩
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
    }
    
    // 判断是否连通
    public boolean connected(int x, int y) {
        return find(x) == find(y);
    }
}
```

## 执行过程图解

### 初始化

```
parent: [0, 1, 2, 3, 4]   // 每个元素指向自己
rank:   [1, 1, 1, 1, 1]   // 每个树高度为1

  0     1     2     3     4
  ↑     ↑     ↑     ↑     ↑
 自己   自己  自己  自己  自己

5个独立集合，5个连通分量
```

### union(0, 1) 合并

```
find(0) = 0, find(1) = 1
rank[0] == rank[1] == 1
把 1 挂在 0 下面，rank[0]++ = 2

parent: [0, 0, 2, 3, 4]
rank:   [2, 1, 1, 1, 1]

  0     2     3     4
 / ↑     ↑     ↑     ↑
1  自己  自己  自己

4个集合，4个连通分量
```

### union(2, 3) 合并

```
find(2) = 2, find(3) = 3
rank[2] == rank[3] == 1
把 3 挂在 2 下面，rank[2]++ = 2

parent: [0, 0, 2, 2, 4]
rank:   [2, 1, 2, 1, 1]

  0     2     4
 / ↑    / ↑    ↑
1  自己 3  自己 自己

3个集合，3个连通分量
```

### union(0, 2) 合并两个树

```
find(0) = 0, find(2) = 2
rank[0] == rank[2] == 2
把 2 挂在 0 下面，rank[0]++ = 3

parent: [0, 0, 0, 2, 4]
rank:   [3, 1, 2, 1, 1]

     0
   / | \
  1  2  自己
    /
   3

2个集合，2个连通分量
```

### find(3) 路径压缩

```
查找前：
     0
   / | \
  1  2  4
    /
   3

find(3)过程：
1. parent[3] = 2 ≠ 3，递归 find(2)
2. parent[2] = 0 ≠ 2，递归 find(0)
3. parent[0] = 0，返回 0
4. 回溯：parent[2] = 0
5. 回溯：parent[3] = 0

查找后（路径压缩）：
     0
   / | | \
  1  2 3  4

parent: [0, 0, 0, 0, 4]
```
```

## 两种优化

| 优化 | 作用 | 效果 |
|------|------|------|
| **路径压缩** | 查找时把节点直接挂到根 | 查询接近 O(1) |
| **按秩合并** | 小树挂在大树下 | 控制树高度 |

## 当前内容

| 文件 | 题目 | 难度 |
|------|------|------|
| `BCJ001_ProvinceCount.java` | 省份数量 | 入门 |

## 并查集适用场景

1. **连通性问题**：判断两个元素是否连通
2. **连通分量计数**：图中有多少个连通块
3. **最小生成树**：Kruskal算法
4. **最近公共祖先**：离线查询
5. **冗余连接**：找出图中多余的边
