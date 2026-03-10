# LB008 - 奇偶链表 题解

## 思路是怎么想出来的

### 第一步：明确问题

```
问题：把链表按奇数索引和偶数索引分开，奇数在前，偶数在后

索引从1开始：
- 第1个节点：奇数索引
- 第2个节点：偶数索引
- 第3个节点：奇数索引
...

示例：
输入：1 → 2 → 3 → 4 → 5
输出：1 → 3 → 5 → 2 → 4

奇数索引节点：1, 3, 5
偶数索引节点：2, 4
```

### 第二步：发现规律

```
关键洞察：
- 奇数索引节点的next指向偶数索引节点
- 偶数索引节点的next指向奇数索引节点
- 可以把奇数节点串成一条链，偶数节点串成一条链
- 最后把偶数链接到奇数链后面

双指针：
- odd指针：连接奇数索引节点
- even指针：连接偶数索引节点
- evenHead：保存偶数链的头（最后用来连接）
```

### 第三步：设计算法

```
步骤：
1. 初始化：odd=head, even=head.next, evenHead=even
2. 遍历：
   - odd.next = even.next（奇数指向下一个奇数）
   - odd = odd.next（odd后移）
   - even.next = odd.next（偶数指向下一个偶数）
   - even = even.next（even后移）
3. 连接：odd.next = evenHead
```

---

## 解法：双指针分组

```java
public ListNode oddEvenList(ListNode head) {
    // 空链表或只有一个节点，直接返回
    if (head == null || head.next == null) {
        return head;
    }
    
    // odd指向第一个节点（奇数索引）
    ListNode odd = head;
    
    // even指向第二个节点（偶数索引）
    ListNode even = head.next;
    
    // 保存偶数链的头，最后用来连接
    ListNode evenHead = even;
    
    // 遍历，当even不为空且even.next不为空时继续
    // even.next是下一个奇数节点
    while (even != null && even.next != null) {
        // 1. odd指向下一个奇数节点（跳过even）
        odd.next = even.next;
        
        // 2. odd后移
        odd = odd.next;
        
        // 3. even指向下一个偶数节点（跳过odd）
        even.next = odd.next;
        
        // 4. even后移
        even = even.next;
    }
    
    // 把偶数链表接到奇数链表后面
    odd.next = evenHead;
    
    // 返回头节点
    return head;
}
```

### 完整执行图解

```
示例：1 → 2 → 3 → 4 → 5

========== 初始状态 ==========

odd = 1（第1个节点，奇数索引）
even = 2（第2个节点，偶数索引）
evenHead = 2（保存偶数链头）

链表：1 → 2 → 3 → 4 → 5
      ↑    ↑
     odd  even

========== 第1轮循环 ==========

条件：even!=null && even.next!=null ✓

步骤1：odd.next = even.next
┌─────────────────────────────────────────┐
│  odd.next = even.next = 3               │
│  1 → 3                                  │
│  同时：2 → 3（even还在指向3）            │
└─────────────────────────────────────────┘

链表：1 → 3    2 → 3 → 4 → 5
      ↑        ↑
     odd      even

步骤2：odd = odd.next
┌─────────────────────────────────────────┐
│  odd后移到3                              │
└─────────────────────────────────────────┘

链表：1 → 3    2 → 3 → 4 → 5
           ↑   ↑
          odd even

步骤3：even.next = odd.next
┌─────────────────────────────────────────┐
│  even.next = odd.next = 4               │
│  2 → 4                                  │
└─────────────────────────────────────────┘

链表：1 → 3    2 → 4 → 5
           ↑   ↑
          odd even
          ↑
         1（奇数链头）

步骤4：even = even.next
┌─────────────────────────────────────────┐
│  even后移到4                             │
└─────────────────────────────────────────┘

链表：1 → 3    2 → 4 → 5
           ↑        ↑
          odd      even

========== 第2轮循环 ==========

条件：even!=null && even.next!=null ✓

步骤1：odd.next = even.next
┌─────────────────────────────────────────┐
│  odd.next = even.next = 5               │
│  3 → 5                                  │
└─────────────────────────────────────────┘

链表：1 → 3 → 5    2 → 4 → 5
                ↑        ↑
               odd      even

步骤2：odd = odd.next
┌─────────────────────────────────────────┐
│  odd后移到5                              │
└─────────────────────────────────────────┘

链表：1 → 3 → 5    2 → 4 → 5
                     ↑        ↑
                    odd      even

步骤3：even.next = odd.next
┌─────────────────────────────────────────┐
│  even.next = odd.next = null            │
│  4 → null                               │
└─────────────────────────────────────────┘

链表：1 → 3 → 5    2 → 4
                     ↑    ↑
                    odd  even

步骤4：even = even.next
┌─────────────────────────────────────────┐
│  even后移到null                          │
└─────────────────────────────────────────┘

========== 循环结束 ==========

条件：even == null，停止循环

连接：odd.next = evenHead
┌─────────────────────────────────────────┐
│  5.next = 2（evenHead）                  │
│  奇数链和偶数链连接起来了                 │
└─────────────────────────────────────────┘

最终链表：1 → 3 → 5 → 2 → 4

返回 head = 1 ✓
```

---

## 关键点分析

### 为什么 while 条件是 even != null && even.next != null？

```
even != null：
- even是偶数链的尾
- even为null说明没有偶数节点了

even.next != null：
- even.next是下一个奇数节点
- even.next为null说明没有下一个奇数了

两种情况：
1. 奇数长度链表：even最后为null
   示例：1 → 2 → 3，最后even指向null
2. 偶数长度链表：even.next为null
   示例：1 → 2 → 3 → 4，最后even指向4，even.next为null
```

### 为什么要保存 evenHead？

```
遍历过程中：
- even指针一直在后移
- 最后even可能为null
- 需要保存偶数链的头，才能连接
```

---

## 边界 Case

**Case 1: 空链表**
```
head = null
返回null ✓
```

**Case 2: 只有一个节点**
```
1
odd=1, even=null
不进入循环
odd.next = evenHead = null
结果：1
```

**Case 3: 只有两个节点**
```
1 → 2
odd=1, even=2
even.next = null，不进入循环
odd.next = evenHead = 2
结果：1 → 2
```

**Case 4: 偶数长度**
```
1 → 2 → 3 → 4
结果：1 → 3 → 2 → 4 ✓
```

---

## 复杂度分析

```
时间复杂度：O(n)
- 遍历一遍链表

空间复杂度：O(1)
- 只使用了几个指针
```

---

## 记忆口诀

```
奇偶链表双指针，odd even 分开走
odd连接下一个奇数，even连接下一个偶数
evenHead保存头，最后连接奇数尾
遍历条件要注意，even和even.next
```
