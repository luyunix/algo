# LB004 - 删除倒数第N个节点 题解

## 思路是怎么想出来的

### 第一步：明确问题

```
问题：删除链表的倒数第N个节点
示例：1→2→3→4→5, n=2，删除4，变成1→2→3→5

暴力法：先遍历一遍算长度L，再遍历到L-n位置删除
- 需要遍历两次
- 可以优化到一次遍历
```

### 第二步：快慢指针思想

```
问题：如何一次遍历找到倒数第N个？

关键洞察：
- 倒数第N个 = 正数第(L-N+1)个
- 需要知道长度L才能定位

快慢指针技巧：
- 快指针先走N步
- 然后快慢指针一起走
- 快指针到达末尾时，慢指针在倒数第N个

为什么这样可行？
- 快指针先走N步，领先慢指针N个节点
- 当快指针到达null（走了L步），慢指针走了L-N步
- 慢指针正好在倒数第N个位置
```

### 第三步：处理删除操作

```
要删除节点，需要找到它的前一个节点

调整：
- 快指针先走N+1步（而不是N步）
- 或者慢指针从虚拟头节点开始
- 这样慢指针最终停在要删除节点的前一个
```

---

## 解法：快慢指针（一次遍历）

```java
public ListNode removeNthFromEnd(ListNode head, int n) {
    ListNode dummy = new ListNode(0);
    dummy.next = head;
    
    ListNode fast = dummy;
    ListNode slow = dummy;
    
    // 快指针先走n+1步
    for (int i = 0; i <= n; i++) {
        fast = fast.next;
    }
    
    // 快慢指针一起走
    while (fast != null) {
        fast = fast.next;
        slow = slow.next;
    }
    
    // slow现在在要删除节点的前一个
    slow.next = slow.next.next;
    
    return dummy.next;
}
```

### 完整执行图解

```
链表：1 → 2 → 3 → 4 → 5，n = 2（删除倒数第2个，即4）

初始化：
dummy: 0 → 1 → 2 → 3 → 4 → 5
fast: 指向dummy
slow: 指向dummy

第1步：快指针先走n+1=3步
fast: dummy → 1 → 2 → 3
fast现在指向3

第2步：快慢指针一起走

第1轮：
fast: 3 → 4
slow: dummy → 1

第2轮：
fast: 4 → 5
slow: 1 → 2

第3轮：
fast: 5 → null
slow: 2 → 3

fast到达null，停止
slow现在指向3，3的下一个就是要删除的4

删除操作：
slow.next = slow.next.next
3.next = 3.next.next = 5

结果：0 → 1 → 2 → 3 → 5
返回 dummy.next = 1 → 2 → 3 → 5 ✓
```

---

## 边界 Case

**Case 1: 删除唯一节点**
```
head = [1], n = 1
快指针走2步直接到null
slow指向dummy，删除dummy.next
返回null ✓
```

**Case 2: 删除头节点**
```
head = [1,2,3], n = 3
快指针走4步到null
slow指向dummy，删除dummy.next（即1）
返回2→3 ✓
```

**Case 3: 删除尾节点**
```
head = [1,2], n = 1
快指针走2步到null
slow指向1，删除1.next（即2）
返回1 ✓
```

---

## 记忆口诀

```
删除倒数第N个，快慢指针来帮忙
快指针先走N+1，然后一起走到底
快指针到null停，慢指针在前一个
slow.next = slow.next.next，删除完成
虚拟头节点真好用，删除头节点不用愁
```
