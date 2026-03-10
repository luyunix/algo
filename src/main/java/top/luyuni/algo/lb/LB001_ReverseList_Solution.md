# LB001 - 反转链表 题解

## 思路是怎么想出来的

### 第一步：明确问题

```
问题：把链表的所有指针方向反转
示例：1 → 2 → 3 → 4 → 5 变成 5 → 4 → 3 → 2 → 1

暴力法：遍历链表，把值存到数组，再反向构建链表
- 需要O(n)额外空间
- 需要遍历两次
- 可以优化到O(1)空间
```

### 第二步：分析链表结构

```
链表节点的特点：
- 每个节点只知道自己的下一个节点（next）
- 无法直接访问前一个节点

关键问题：如何找到前一个节点？
答案：遍历的时候保存前一个节点！
```

### 第三步：设计指针操作

```
遍历链表时需要三个指针：
- prev：前一个节点（初始为null，因为头节点前面没有节点）
- curr：当前节点（从头节点开始）
- next：下一个节点（临时保存，防止断链）

操作步骤：
1. 保存 curr.next 到 next（防止断链后找不到后面）
2. 把 curr.next 指向 prev（反转指针）
3. prev 移动到 curr
4. curr 移动到 next
```

### 第四步：验证边界情况

```
空链表：head = null，直接返回null ✓
单个节点：反转后还是它自己 ✓
两个节点：1 → 2 变成 2 → 1 ✓
```

## 核心概念

**问题本质**：把链表中每个节点的 next 指针指向前一个节点

**核心思想**：遍历链表，逐个反转指针方向

---

## 解法1：迭代（三指针法）

### 代码

```java
public ListNode reverseList(ListNode head) {
    ListNode prev = null;   // 前一个节点
    ListNode curr = head;   // 当前节点
    
    while (curr != null) {
        ListNode next = curr.next;  // 保存下一个节点
        curr.next = prev;           // 反转指针
        prev = curr;                // prev前移
        curr = next;                // curr前移
    }
    
    return prev;  // 新的头节点
}
```

### 过程图解

```
初始：
prev = null
curr = 1 → 2 → 3 → 4 → 5 → null

第1步：
next = 2
1.next = null (指向prev)
prev = 1
curr = 2

null ← 1    2 → 3 → 4 → 5 → null
      prev  curr

第2步：
next = 3
2.next = 1 (指向prev)
prev = 2
curr = 3

null ← 1 ← 2    3 → 4 → 5 → null
            prev  curr

第3步：
next = 4
3.next = 2
prev = 3
curr = 4

null ← 1 ← 2 ← 3    4 → 5 → null
                prev  curr

...继续

最后：
null ← 1 ← 2 ← 3 ← 4 ← 5
                    prev(curr为null)

返回 prev = 5
```

### 为什么需要 next 指针？

如果不保存 next：

```java
curr.next = prev;  // 这里已经断开了和下一个节点的连接
// 现在 curr.next 指向 prev，找不到原来的下一个节点了！
```

所以需要先用 next 保存 `curr.next`，再修改指针。

---

## 解法2：递归

### 代码

```java
public ListNode reverseListRecursive(ListNode head) {
    // 终止条件：空链表或只有一个节点
    if (head == null || head.next == null) {
        return head;
    }
    
    // 递归反转后面的链表
    ListNode newHead = reverseListRecursive(head.next);
    
    // 把当前节点接到反转后链表的末尾
    head.next.next = head;  // 让后一个节点指向当前节点
    head.next = null;       // 断开原来的连接，防止环
    
    return newHead;
}
```

### 递归过程图解

```
原始链表：1 → 2 → 3 → 4 → 5 → null

递归展开：
reverse(1)
  └── reverse(2)
        └── reverse(3)
              └── reverse(4)
                    └── reverse(5)
                          └── return 5 (base case)

递归回溯（关键操作）：

第4层：head=4, head.next=5
  5.next = 4  (5指向4)
  4.next = null
  结果：4 ← 5
  return 5

第3层：head=3, head.next=4
  4.next = 3  (4指向3)
  3.next = null
  结果：3 ← 4 ← 5
  return 5

第2层：head=2, head.next=3
  3.next = 2
  2.next = null
  结果：2 ← 3 ← 4 ← 5
  return 5

第1层：head=1, head.next=2
  2.next = 1
  1.next = null
  结果：1 ← 2 ← 3 ← 4 ← 5
  return 5

最终结果：5 → 4 → 3 → 2 → 1 → null
```

### 为什么 head.next = null？

如果不断开，会形成环：

```
1 → 2 → 3 → ...
↑___________|

1.next = 2
2.next = 1 (反转后)
这样就形成了 1 ↔ 2 的环！
```

所以必须 `head.next = null` 断开原来的连接。

---

## 解法3：用栈（辅助理解）

```java
public ListNode reverseListStack(ListNode head) {
    if (head == null) return null;
    
    Stack<ListNode> stack = new Stack<>();
    
    // 所有节点入栈
    while (head != null) {
        stack.push(head);
        head = head.next;
    }
    
    // 出栈重建链表
    ListNode newHead = stack.pop();
    ListNode curr = newHead;
    
    while (!stack.isEmpty()) {
        curr.next = stack.pop();
        curr = curr.next;
    }
    curr.next = null;  // 最后一个节点指向null
    
    return newHead;
}
```

---

## 三种方法对比

| 方法 | 时间复杂度 | 空间复杂度 | 特点           |
|----|-------|-------|--------------|
| 迭代 | O(n)  | O(1)  | 最优，面试首选      |
| 递归 | O(n)  | O(n)  | 代码简洁，但有栈溢出风险 |
| 栈  | O(n)  | O(n)  | 辅助理解，面试不推荐   |

---

## 易错点总结

| 错误      | 原因             | 解决               |
|---------|----------------|------------------|
| 断链      | 没保存 next 就修改指针 | 先用 next 保存       |
| 返回 curr | curr 最后为 null  | 返回 prev          |
| 递归形成环   | 没断开 head.next  | head.next = null |

---

## 记忆口诀

```
反转链表三指针，prev curr next
先保存再反转，prev curr 往前走
递归思路要记牢，先递归后连接
head.next 要断开，否则形成环
```
