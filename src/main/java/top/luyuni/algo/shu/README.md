# SHU 目录 - 二叉树专题

这个目录收录二叉树相关的题目。

## 为什么二叉树常考？

二叉树是面试中出现频率最高的数据结构之一：

- 考察递归思维的最佳载体
- 很多实际问题可以抽象为树结构
- 能区分候选人的算法功底

## 核心算法框架

### 1. 遍历框架

```java
// 前序遍历：根 → 左 → 右
void preorder(TreeNode root) {
    if (root == null) return;
    // 处理当前节点
    preorder(root.left);
    preorder(root.right);
}

// 中序遍历：左 → 根 → 右
void inorder(TreeNode root) {
    if (root == null) return;
    inorder(root.left);
    // 处理当前节点
    inorder(root.right);
}

// 后序遍历：左 → 右 → 根
void postorder(TreeNode root) {
    if (root == null) return;
    postorder(root.left);
    postorder(root.right);
    // 处理当前节点
}
```

### 2. 递归四步法

1. **确定递归函数的参数和返回值**
2. **确定终止条件**
3. **确定单层递归的逻辑**
4. **确定返回值**

### 3. 层序遍历（BFS）

```java
void levelOrder(TreeNode root) {
    if (root == null) return;
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    
    while (!queue.isEmpty()) {
        TreeNode node = queue.poll();
        // 处理当前节点
        if (node.left != null) queue.offer(node.left);
        if (node.right != null) queue.offer(node.right);
    }
}
```

## 二叉树题目分类

| 类别   | 典型题目        | 核心技巧 |
|------|-------------|------|
| 遍历   | 前/中/后序遍历    | 递归或栈 |
| 属性   | 深度、节点数、路径和  | 后序遍历 |
| 路径   | 根到叶路径、最大路径和 | 回溯   |
| 结构   | 对称、平衡、子树    | 递归比较 |
| 序列化  | 前序+中序重建     | 找根节点 |
| 公共祖先 | LCA         | 后序遍历 |

## 当前内容

| 文件                     | 题目      | 难度 |
|------------------------|---------|----|
| `SHU001_MaxDepth.java` | 二叉树最大深度 | 入门 |

## 二叉树学习建议

1. **先掌握三种遍历**：递归和非递归都要会
2. **再掌握递归思维**：把问题分解为子问题
3. **最后掌握套路**：很多题目都是遍历框架的变形

## 记忆口诀

```
二叉树考遍历，前中后序要熟练
递归四步走，终止条件别遗漏
层序用队列，一层一层来处理
后序最强大，子树信息往上带
```
