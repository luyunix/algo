# DFS009 - 课程表 题解

## 思路是怎么想出来的

### 第一步：明确问题

```
问题：给定课程总数和先修关系，判断是否可能完成所有课程

示例：
numCourses = 2, prerequisites = [[1,0]]
- 课程1的先修课程是0
- 先修0，再修1
- 可以完成，返回true

示例2：
numCourses = 2, prerequisites = [[1,0],[0,1]]
- 1依赖0，0又依赖1
- 循环依赖，无法完成，返回false
```

### 第二步：发现问题本质

```
关键洞察：
- 课程是节点，先修关系是有向边
- 问题转化为：判断有向图是否有环
- 无环 → 可以完成（有拓扑序）
- 有环 → 无法完成

如何判断有环？
- DFS遍历图
- 如果遇到一个正在访问的节点（在当前路径上），说明有环
- 需要三种状态：未访问、访问中、已访问
```

### 第三步：设计算法

```
状态定义：
- 0：未访问（白色）
- 1：访问中（灰色）- 在当前DFS路径上
- 2：已访问（黑色）- 已完成，无环

DFS(node)：
1. 如果state[node] == 1，遇到环，返回false
2. 如果state[node] == 2，已访问，返回true
3. 标记state[node] = 1（访问中）
4. 遍历所有邻接节点：
   a. 如果DFS(邻接节点) == false，返回false
5. 标记state[node] = 2（已访问）
6. 返回true

主函数：
- 对所有未访问节点执行DFS
- 如果有任何DFS返回false，说明有环
```

---

## 解法：DFS判断环（拓扑排序）

```java
public boolean canFinish(int numCourses, int[][] prerequisites) {
    // 构建邻接表
    List<Integer>[] graph = new ArrayList[numCourses];
    for (int i = 0; i < numCourses; i++) {
        graph[i] = new ArrayList<>();
    }
    for (int[] pre : prerequisites) {
        graph[pre[0]].add(pre[1]);  // pre[0]依赖pre[1]
    }
    
    // 0:未访问, 1:访问中, 2:已访问
    int[] state = new int[numCourses];
    
    // 对每个未访问节点执行DFS
    for (int i = 0; i < numCourses; i++) {
        if (state[i] == 0) {
            if (!dfs(graph, i, state)) {
                return false;  // 有环
            }
        }
    }
    
    return true;  // 无环
}

private boolean dfs(List<Integer>[] graph, int node, int[] state) {
    // 遇到访问中的节点，有环
    if (state[node] == 1) return false;
    
    // 已访问，无环
    if (state[node] == 2) return true;
    
    // 标记为访问中
    state[node] = 1;
    
    // 遍历邻接节点
    for (int neighbor : graph[node]) {
        if (!dfs(graph, neighbor, state)) {
            return false;
        }
    }
    
    // 标记为已访问
    state[node] = 2;
    return true;
}
```

### 完整执行图解

```
示例1：numCourses=2, prerequisites=[[1,0]]

图：0 ← 1（1依赖0）

========== DFS过程 ==========

初始：state=[0,0]

从节点0开始：
dfs(0):
┌─────────────────────────────────────────┐
│  state[0]=0，标记为访问中state[0]=1      │
│  节点0没有邻接节点（没人依赖0）           │
│  标记为已访问state[0]=2                  │
│  返回true                                │
└─────────────────────────────────────────┘

从节点1开始：
dfs(1):
┌─────────────────────────────────────────┐
│  state[1]=0，标记为访问中state[1]=1      │
│  邻接节点：0                             │
│  dfs(0): state[0]=2，已访问，返回true    │
│  标记为已访问state[1]=2                  │
│  返回true                                │
└─────────────────────────────────────────┘

所有节点访问完，无环，返回true ✓

===============================

示例2：numCourses=2, prerequisites=[[1,0],[0,1]]

图：0 ↔ 1（互相依赖，形成环）

从节点0开始：
dfs(0):
┌─────────────────────────────────────────┐
│  state[0]=0，标记为访问中state[0]=1      │
│  邻接节点：1                             │
│  dfs(1):                                 │
└─────────────────────────────────────────┘

  dfs(1):
  ┌─────────────────────────────────────┐
  │  state[1]=0，标记为访问中state[1]=1  │
  │  邻接节点：0                         │
  │  dfs(0):                             │
  └─────────────────────────────────────┘

    dfs(0):
    ┌─────────────────────────────────┐
    │  state[0]=1，访问中！            │
    │  遇到环，返回false               │
    └─────────────────────────────────┘

  返回false
返回false

有环，返回false ✓
```

### 状态变化表（有环示例）

```
节点0开始DFS：

步骤    操作            state[0]    state[1]
─────────────────────────────────────────────
初始    -               0(白)       0(白)
1       访问0           1(灰)       0(白)
2       访问1           1(灰)       1(灰)
3       发现0是灰       1(灰)       1(灰)  ← 有环！
```

---

## 三种颜色法的意义

```
白色(0)：未访问
- 从未到达过这个节点

灰色(1)：访问中
- 在当前DFS路径上
- 如果再次遇到灰色节点，说明形成了环

黑色(2)：已访问
- 从这个节点出发的所有路径都已检查完毕
- 无环，可以放心重用

为什么不用boolean？
- boolean只能区分访问/未访问
- 无法区分"在当前路径上"和"已完成"
- 需要三种状态才能准确判断环
```

---

## 边界 Case

**Case 1: 无先修关系**

```
numCourses=3, prerequisites=[]
所有课程独立，可以完成
返回true ✓
```

**Case 2: 单课程自环**

```
numCourses=1, prerequisites=[[0,0]]
课程0依赖自己，有环
返回false ✓
```

**Case 3: 长链无环**

```
numCourses=4, prerequisites=[[1,0],[2,1],[3,2]]
0→1→2→3，无环
返回true ✓
```

**Case 4: 多个独立链**

```
numCourses=4, prerequisites=[[1,0],[3,2]]
0→1 和 2→3，两个独立链
返回true ✓
```

---

## 记忆口诀

```
课程表是拓扑排，有向图中找环路
三种颜色标记好，白色未访灰在路
黑色已经访问完，遇到灰色就有环
DFS遍历每个点，无环就能修完课
```
