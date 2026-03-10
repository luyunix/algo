# SHU001 - 二叉树的最大深度 题解

## 核心概念

**问题本质**：求二叉树的高度（从根到最远叶子的节点数）

**核心思想**：一棵树的深度 = max(左子树深度, 右子树深度) + 1

---

## 解法1：递归（后序遍历）

### 代码

```java
public int maxDepth(TreeNode root) {
    // 终止条件：空树深度为0
    if (root == null) return 0;
    
    // 递归计算左右子树深度
    int leftDepth = maxDepth(root.left);
    int rightDepth = maxDepth(root.right);
    
    // 当前树深度 = max(左, 右) + 1
    return Math.max(leftDepth, rightDepth) + 1;
}
```

### 递归过程图解

```
树结构：
      3
     / \
    9  20
      /  \
     15   7

递归调用过程：
maxDepth(3)
├── maxDepth(9)
│   ├── maxDepth(null) = 0
│   ├── maxDepth(null) = 0
│   └── return max(0,0) + 1 = 1
├── maxDepth(20)
│   ├── maxDepth(15)
│   │   ├── maxDepth(null) = 0
│   │   ├── maxDepth(null) = 0
│   │   └── return 1
│   ├── maxDepth(7)
│   │   ├── maxDepth(null) = 0
│   │   ├── maxDepth(null) = 0
│   │   └── return 1
│   └── return max(1,1) + 1 = 2
└── return max(1,2) + 1 = 3
```

### 为什么用后序遍历？

因为要计算当前树的深度，**必须先知道左右子树的深度**。

- 前序：根左右 → 处理根时还不知道子树信息
- 中序：左根右 → 处理根时只知道左子树信息
- **后序：左右根** → 处理根时已经知道左右子树信息 ✓

---

## 解法2：层序遍历（BFS）

### 核心思想

一层一层遍历，遍历了几层，深度就是几。

### 代码

```java
public int maxDepthBFS(TreeNode root) {
    if (root == null) return 0;
    
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    int depth = 0;
    
    while (!queue.isEmpty()) {
        int levelSize = queue.size();  // 当前层的节点数
        depth++;  // 每遍历一层，深度+1
        
        // 处理当前层的所有节点
        for (int i = 0; i < levelSize; i++) {
            TreeNode node = queue.poll();
            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
    }
    
    return depth;
}
```

### BFS过程图解

```
初始：queue = [3], depth = 0

第1层：
  queue.size() = 1
  depth = 1
  处理3，加入9,20
  queue = [9, 20]

第2层：
  queue.size() = 2
  depth = 2
  处理9（无子节点）
  处理20，加入15,7
  queue = [15, 7]

第3层：
  queue.size() = 2
  depth = 3
  处理15（无子节点）
  处理7（无子节点）
  queue = []

结束，depth = 3
```

---

## 解法3：迭代（用栈模拟递归）

### 核心思想

用栈保存节点和当前深度，模拟递归过程。

### 代码

```java
public int maxDepthIterative(TreeNode root) {
    if (root == null) return 0;
    
    Stack<Pair<TreeNode, Integer>> stack = new Stack<>();
    stack.push(new Pair<>(root, 1));
    int maxDepth = 0;
    
    while (!stack.isEmpty()) {
        Pair<TreeNode, Integer> pair = stack.pop();
        TreeNode node = pair.getKey();
        int depth = pair.getValue();
        
        maxDepth = Math.max(maxDepth, depth);
        
        if (node.left != null) {
            stack.push(new Pair<>(node.left, depth + 1));
        }
        if (node.right != null) {
            stack.push(new Pair<>(node.right, depth + 1));
        }
    }
    
    return maxDepth;
}
```

---

## 三种方法对比

| 方法  | 时间复杂度 | 空间复杂度 | 特点          |
|-----|-------|-------|-------------|
| 递归  | O(n)  | O(h)  | 代码简洁，但可能栈溢出 |
| BFS | O(n)  | O(w)  | w是最大宽度，适合宽树 |
| 迭代  | O(n)  | O(h)  | 用显式栈避免递归溢出  |

注：h是树高度，n是节点数

---

## 递归四步法总结

| 步骤       | 内容       | 本题               |
|----------|----------|------------------|
| 1. 参数返回值 | 函数的输入输出  | 输入：节点，输出：深度      |
| 2. 终止条件  | 什么时候停止递归 | root == null 返回0 |
| 3. 单层逻辑  | 当前层做什么   | 计算左右深度，取max+1    |
| 4. 返回值   | 返回什么给上层  | 当前树的深度           |

---

## 记忆口诀

```
树深度怎么求，左右子树取最大
后序遍历最合适，子树信息往上带
递归终止看空树，返回0别搞错
BFS层序也可以，遍历几层深度几
```
