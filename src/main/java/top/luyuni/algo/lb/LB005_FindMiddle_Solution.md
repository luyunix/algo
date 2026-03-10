# LB005 - 链表的中间节点 题解

## 思路是怎么想出来的

### 第一步：明确问题

```
问题：找到链表的中间节点
示例：
- 1→2→3→4→5，中间是3
- 1→2→3→4→5→6，中间有两个（3和4），返回第二个（4）

暴力法：先遍历算长度L，再遍历到L/2位置
- 需要遍历两次
- 可以优化到一次遍历
```

### 第二步：快慢指针思想

```
关键洞察：
- 快指针速度是慢指针的2倍
- 快指针到达末尾时，慢指针正好在中间

具体：
- 快指针每次走2步
- 慢指针每次走1步
- 快指针到null时，慢指针在中间
```

### 第三步：处理奇偶长度

```
奇数长度（1→2→3→4→5）：
- fast: 1→3→5→null
- slow: 1→2→3
- 返回3 ✓

偶数长度（1→2→3→4→5→6）：
- fast: 1→3→5→null（fast.next为null时停）
- slow: 1→2→3→4
- 返回4（第二个中间节点）✓

循环条件：while (fast != null && fast.next != null)
- 奇数：fast == null 时停
- 偶数：fast.next == null 时停
```

---

## 解法：快慢指针

```java
public ListNode middleNode(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;
    
    while (fast != null && fast.next != null) {
        slow = slow.next;        // 慢指针走1步
        fast = fast.next.next;   // 快指针走2步
    }
    
    return slow;
}
```

### 完整执行图解

**示例1：奇数长度 1→2→3→4→5**

```
初始：
slow: 1
fast: 1

第1轮：
slow: 1 → 2
fast: 1 → 3

第2轮：
slow: 2 → 3
fast: 3 → 5

第3轮：
fast.next = null，停止

返回 slow = 3 ✓
```

**示例2：偶数长度 1→2→3→4→5→6**

```
初始：
slow: 1
fast: 1

第1轮：
slow: 1 → 2
fast: 1 → 3

第2轮：
slow: 2 → 3
fast: 3 → 5

第3轮：
slow: 3 → 4
fast: 5 → null

fast == null，停止

返回 slow = 4 ✓（第二个中间节点）
```

---

## 进阶：返回第一个中间节点

```
偶数长度时返回第一个中间节点（如1→2→3→4返回2）

调整：初始时fast=head.next

public ListNode middleNodeFirst(ListNode head) {
    ListNode slow = head;
    ListNode fast = head.next;  // 关键区别
    
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
    }
    
    return slow;
}
```

---

## 边界 Case

**Case 1: 空链表**

```
head = null
直接返回null ✓
```

**Case 2: 单个节点**

```
head = [1]
fast.next == null，直接返回1 ✓
```

**Case 3: 两个节点**

```
head = [1,2]
fast: 1 → null
返回2 ✓
```

---

## 记忆口诀

```
找中点用快慢，fast走2slow走1
fast到null就停，slow就是中节点
奇数偶数都能用，偶数返回第二个
想要返回第一个，fast初始指向二
```
