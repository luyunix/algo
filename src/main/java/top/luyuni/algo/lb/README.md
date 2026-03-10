# LB 目录 - 链表专题

这个目录收录链表相关的题目。

## 为什么链表常考？

链表是面试基础题，考察：
- 指针操作（Java中是引用操作）
- 边界条件处理
- 递归思维（递归反转等）

## 链表核心技巧

### 1. 虚拟头节点

处理头节点可能被修改的情况（如删除头节点）：

```java
ListNode dummy = new ListNode(0);  // 虚拟头节点
dummy.next = head;
ListNode curr = dummy;

// 处理...

return dummy.next;  // 返回真正的头节点
```

### 2. 快慢指针

```java
// 找中点：快指针走2步，慢指针走1步
ListNode slow = head, fast = head;
while (fast != null && fast.next != null) {
    slow = slow.next;
    fast = fast.next.next;
}
// slow 就是中点

// 找倒数第K个：快指针先走K步
ListNode fast = head, slow = head;
for (int i = 0; i < k; i++) fast = fast.next;
while (fast != null) {
    fast = fast.next;
    slow = slow.next;
}
// slow 就是倒数第K个
```

### 3. 反转链表

```java
ListNode reverse(ListNode head) {
    ListNode prev = null, curr = head;
    while (curr != null) {
        ListNode next = curr.next;
        curr.next = prev;
        prev = curr;
        curr = next;
    }
    return prev;
}
```

### 4. 递归处理链表

```java
// 递归反转
ListNode reverseRecursive(ListNode head) {
    if (head == null || head.next == null) return head;
    ListNode newHead = reverseRecursive(head.next);
    head.next.next = head;
    head.next = null;
    return newHead;
}
```

## 链表题目分类

| 类别 | 典型题目 | 核心技巧 |
|------|----------|----------|
| 反转 | 反转链表、K个一组反转 | 三指针法 |
| 删除 | 删除节点、删除倒数第N个 | 虚拟头节点 |
| 查找 | 中点、倒数第K、相交节点 | 快慢指针 |
| 合并 | 合并两个有序链表 | 双指针 |
| 重排 | 重排链表、旋转链表 | 找中点+反转+合并 |
| 判断 | 回文链表、有环链表 | 快慢指针 |

## 当前内容

| 文件 | 题目 | 难度 | 核心技巧 |
|------|------|------|----------|
| `LB001_ReverseList.java` | 反转链表 | 入门 | 三指针法 |
| `LB002_HasCycle.java` | 环形链表 | 入门 | 快慢指针 |
| `LB003_MergeTwoLists.java` | 合并两个有序链表 | 入门 | 双指针 |
| `LB004_RemoveNthFromEnd.java` | 删除倒数第N个节点 | 中等 | 快慢指针 |
| `LB005_FindMiddle.java` | 链表的中间节点 | 入门 | 快慢指针 |
| `LB006_IntersectionNode.java` | 相交链表 | 入门 | 双指针 |
| `LB007_PalindromeList.java` | 回文链表 | 中等 | 找中点+反转+比较 |
| `LB008_OddEvenList.java` | 奇偶链表 | 中等 | 双指针分组 |
| `LB009_SortList.java` | 排序链表 | 中等 | 归并排序 |
| `LB010_CopyRandomList.java` | 复制带随机指针的链表 | 中等 | 哈希表/原地插入 |

## 链表学习建议

1. **先掌握基础操作**：遍历、插入、删除
2. **再掌握虚拟头节点**：避免头节点特殊处理
3. **最后掌握快慢指针**：解决80%的链表难题

## 记忆口诀

```
链表操作三要素：指针、边界、返回值
虚拟头节点真好用，头节点变不用愁
快慢指针是神器，中点倒数都能求
反转链表三指针，prev curr next别乱走
```
