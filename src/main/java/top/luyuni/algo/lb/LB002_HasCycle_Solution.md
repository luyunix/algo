# LB002 - 环形链表 题解

## 核心思想：快慢指针

**问题本质**：判断链表是否有环，如果有环还要找到环的入口

**核心思想**：快指针走2步，慢指针走1步，如果有环一定会相遇

---

## 解法1：判断是否有环

### 代码

```java
public boolean hasCycle(ListNode head) {
    if (head == null || head.next == null) {
        return false;
    }
    
    ListNode slow = head;  // 慢指针，每次走1步
    ListNode fast = head;  // 快指针，每次走2步
    
    while (fast != null && fast.next != null) {
        slow = slow.next;           // 慢指针走1步
        fast = fast.next.next;      // 快指针走2步
        
        if (slow == fast) {         // 相遇，有环
            return true;
        }
    }
    
    return false;  // 快指针到达null，无环
}
```

### 完整执行图解

**示例1：有环链表 3 → 2 → 0 → -4 → (回到2)**

```
初始状态：
3 → 2 → 0 → -4 → 2（环）
↑
slow
fast

第1轮：
slow 走1步到 2
fast 走2步到 0

3 → 2 → 0 → -4 → 2
    ↑       ↑
   slow    fast

第2轮：
slow 走1步到 0
fast 走2步：0 → -4 → 2

3 → 2 → 0 → -4 → 2
        ↑       ↑
       slow    fast

第3轮：
slow 走1步到 -4
fast 走2步：2 → 0 → -4

3 → 2 → 0 → -4 → 2
            ↑   ↑
          slow fast

第4轮：
slow 走1步到 2
fast 走2步：-4 → 2 → 0

3 → 2 → 0 → -4 → 2
        ↑           ↑
       slow        fast

第5轮：
slow 走1步到 0
fast 走2步：0 → -4 → 2

3 → 2 → 0 → -4 → 2
            ↑   ↑
          slow fast

第6轮：
slow 走1步到 -4
fast 走2步：2 → 0 → -4

3 → 2 → 0 → -4 → 2
                ↑
              slow/fast  ← 相遇！

返回 true ✓
```

**示例2：无环链表 1 → 2 → 3 → null**

```
初始状态：
1 → 2 → 3 → null
↑
slow
fast

第1轮：
slow 到 2
fast 到 3

1 → 2 → 3 → null
    ↑   ↑
   slow fast

第2轮：
slow 到 3
fast 到 null（3.next.next）

1 → 2 → 3 → null
        ↑       ↑
       slow    fast = null

fast == null，退出循环
返回 false ✓
```

### 为什么快指针走2步，慢指针走1步？

```
假设链表有环：
- 快指针速度是慢指针的2倍
- 如果有环，快指针相对于慢指针的速度是1（2-1=1）
- 所以快指针一定会追上慢指针（相对速度为1，必然相遇）

如果快指针走3步，慢指针走1步：
- 相对速度是2
- 如果环的长度是奇数，可能会跳过慢指针，无法相遇
```

---

## 解法2：找环的入口（进阶）

### 数学推导

```
设：
- 链表头到环入口的距离为 a
- 环入口到相遇点的距离为 b
- 相遇点回到环入口的距离为 c

慢指针走的距离：a + b
快指针走的距离：a + b + n(b + c)  （n是快指针绕的圈数）

因为快指针速度是2倍：
2(a + b) = a + b + n(b + c)
a + b = n(b + c)
a = n(b + c) - b
a = (n-1)(b + c) + c

这意味着：
- 从头走 a 步 = 从相遇点走 (n-1)圈 + c 步
- 所以一个指针从头开始，一个从相遇点开始，都走1步
- 它们会在环入口相遇！
```

### 代码

```java
public ListNode detectCycle(ListNode head) {
    if (head == null || head.next == null) {
        return null;
    }
    
    // 第1步：判断是否有环，找到相遇点
    ListNode slow = head;
    ListNode fast = head;
    boolean hasCycle = false;
    
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) {
            hasCycle = true;
            break;
        }
    }
    
    if (!hasCycle) {
        return null;
    }
    
    // 第2步：找环的入口
    ListNode ptr1 = head;      // 从头开始
    ListNode ptr2 = slow;      // 从相遇点开始
    
    while (ptr1 != ptr2) {
        ptr1 = ptr1.next;
        ptr2 = ptr2.next;
    }
    
    return ptr1;  // 相遇点就是环入口
}
```

### 找入口执行图解

```
链表：1 → 2 → 3 → 4 → 5 → 6 → 3（环）
              ↑___________|

假设快慢指针在 5 相遇：
- slow 走的距离：1→2→3→4→5（4步）
- fast 走的距离：1→3→5→3→5（4步，相对于slow）

实际：
- slow：1→2→3→4→5（4步）
- fast：1→3→5→3→5→3→5（走了7步，但slow只走了4步）

从相遇点5开始：
ptr1 从1出发，ptr2 从5出发

ptr1: 1 → 2 → 3
ptr2: 5 → 6 → 3

在 3 相遇！3就是环入口 ✓
```

---

## 解法3：哈希表（辅助理解）

```java
public boolean hasCycleHash(ListNode head) {
    Set<ListNode> visited = new HashSet<>();
    
    while (head != null) {
        if (visited.contains(head)) {
            return true;  // 访问过，有环
        }
        visited.add(head);
        head = head.next;
    }
    
    return false;
}
```

**缺点**：需要O(n)额外空间，面试不推荐

---

## 三种方法对比

| 方法   | 时间复杂度 | 空间复杂度 | 特点        |
|------|-------|-------|-----------|
| 快慢指针 | O(n)  | O(1)  | 最优，面试首选   |
| 哈希表  | O(n)  | O(n)  | 简单直观，但费空间 |
| 破坏链表 | O(n)  | O(1)  | 修改链表，不推荐  |

---

## 边界 Case

**Case 1: 空链表**

```
head = null
直接返回 false ✓
```

**Case 2: 只有一个节点**

```
1 → null
fast.next == null，返回 false ✓
```

**Case 3: 两个节点成环**

```
1 → 2 → 1
fast 走2步会回到1，与slow在1相遇
返回 true ✓
```

**Case 4: 长链表小环**

```
1 → 2 → 3 → 4 → 5 → 6 → 4（环）
fast 会多绕几圈，但最终会追上slow
```

---

## 记忆口诀

```
判断环用快慢，fast走2slow走1
fast先到null就无环，相遇就有环
找入口要数学，头到入口=相遇点到入口
双指针再出发，相遇就是入口点
```
