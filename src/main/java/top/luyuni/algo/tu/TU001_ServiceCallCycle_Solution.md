# TU001 - 服务调用环检测 题解

## 核心概念

**问题本质**：判断有向图中是否存在环

**应用场景**：

- 微服务依赖检测
- 编译依赖检测（Makefile、Maven）
- 任务调度死锁检测

---

## 解法1：DFS + 三色标记法

### 核心思想

用三种颜色标记节点状态：

- **白色（0）**：未访问
- **灰色（1）**：正在访问（在递归栈中）
- **黑色（2）**：已访问完成

**关键洞察**：如果DFS过程中遇到灰色节点，说明遇到了环

### 为什么遇到灰色节点就是环？

```
DFS过程：
A(白) → 标记为灰 → 访问B
B(白) → 标记为灰 → 访问C
C(白) → 标记为灰 → 访问A
A(灰) → 发现环！

    A(灰) → B(灰)
     ↑       ↓
     └──── C(灰)

A→B→C→A 形成环！
```

### 代码实现

```java
public boolean hasCycleDFS(String[] services, String[][] calls) {
    // 建图：邻接表
    Map<String, List<String>> graph = new HashMap<>();
    for (String s : services) graph.put(s, new ArrayList<>());
    for (String[] call : calls) {
        graph.get(call[0]).add(call[1]);
    }
    
    // 0:白色(未访问), 1:灰色(访问中), 2:黑色(已完成)
    Map<String, Integer> color = new HashMap<>();
    for (String s : services) color.put(s, 0);
    
    // 对每个未访问节点进行DFS
    for (String s : services) {
        if (color.get(s) == 0) {
            if (dfs(graph, s, color)) return true;
        }
    }
    return false;
}

private boolean dfs(Map<String, List<String>> graph, String node, 
                    Map<String, Integer> color) {
    color.put(node, 1);  // 标记为灰色（正在访问）
    
    for (String neighbor : graph.get(node)) {
        if (color.get(neighbor) == 1) {
            // 遇到灰色节点，发现环！
            return true;
        }
        if (color.get(neighbor) == 0) {
            // 未访问，继续DFS
            if (dfs(graph, neighbor, color)) return true;
        }
    }
    
    color.put(node, 2);  // 标记为黑色（已完成）
    return false;
}
```

---

## 解法2：拓扑排序（Kahn算法）

### 核心思想

**不断删除入度为0的节点**：

1. 找出入度为0的节点（没有依赖的节点）
2. 删除该节点和它的所有出边
3. 重复直到没有入度为0的节点
4. 如果还有节点没被删除，说明有环

### 为什么删不掉的节点就是环？

```
环上的节点互相依赖：
A → B → C → A

A的入度：1（来自C）
B的入度：1（来自A）
C的入度：1（来自B）

没有入度为0的节点，所以都删不掉 → 有环
```

### 代码实现

```java
public boolean hasCycleTopological(String[] services, String[][] calls) {
    // 建图 + 计算入度
    Map<String, List<String>> graph = new HashMap<>();
    Map<String, Integer> inDegree = new HashMap<>();
    
    for (String s : services) {
        graph.put(s, new ArrayList<>());
        inDegree.put(s, 0);
    }
    
    for (String[] call : calls) {
        graph.get(call[0]).add(call[1]);
        inDegree.put(call[1], inDegree.get(call[1]) + 1);
    }
    
    // 找所有入度为0的节点
    Queue<String> queue = new LinkedList<>();
    for (String s : services) {
        if (inDegree.get(s) == 0) queue.offer(s);
    }
    
    int visited = 0;  // 记录访问过的节点数
    
    while (!queue.isEmpty()) {
        String node = queue.poll();
        visited++;
        
        // 删除该节点的出边
        for (String neighbor : graph.get(node)) {
            inDegree.put(neighbor, inDegree.get(neighbor) - 1);
            if (inDegree.get(neighbor) == 0) {
                queue.offer(neighbor);
            }
        }
    }
    
    // 如果还有节点没被访问，说明有环
    return visited != services.length;
}
```

---

## 两种方法对比

| 方法    | 时间复杂度  | 空间复杂度 | 适用场景     |
|-------|--------|-------|----------|
| DFS三色 | O(V+E) | O(V)  | 需要找出具体环  |
| 拓扑排序  | O(V+E) | O(V)  | 只需要判断有无环 |

---

## 扩展：找出具体的环

```java
public List<String> findCycle(String[] services, String[][] calls) {
    // 建图
    Map<String, List<String>> graph = new HashMap<>();
    for (String s : services) graph.put(s, new ArrayList<>());
    for (String[] call : calls) graph.get(call[0]).add(call[1]);
    
    Map<String, Integer> color = new HashMap<>();
    for (String s : services) color.put(s, 0);
    
    Map<String, String> parent = new HashMap<>();  // 记录父节点
    List<String> cycle = new ArrayList<>();
    
    for (String s : services) {
        if (color.get(s) == 0) {
            if (dfsFindCycle(graph, s, color, parent, cycle)) {
                return cycle;
            }
        }
    }
    return cycle;
}

private boolean dfsFindCycle(Map<String, List<String>> graph, String node,
                              Map<String, Integer> color, 
                              Map<String, String> parent,
                              List<String> cycle) {
    color.put(node, 1);
    
    for (String neighbor : graph.get(node)) {
        if (color.get(neighbor) == 1) {
            // 找到环，回溯路径
            cycle.add(neighbor);
            String cur = node;
            while (!cur.equals(neighbor)) {
                cycle.add(cur);
                cur = parent.get(cur);
            }
            cycle.add(neighbor);
            Collections.reverse(cycle);
            return true;
        }
        if (color.get(neighbor) == 0) {
            parent.put(neighbor, node);
            if (dfsFindCycle(graph, neighbor, color, parent, cycle)) return true;
        }
    }
    
    color.put(node, 2);
    return false;
}
```

---

## 记忆口诀

```
环检测有两种，DFS和拓扑排序
DFS用三色，灰灰相遇就是环
拓扑删入度，删不掉的是环
有向图要注意，无向图更简单
```
