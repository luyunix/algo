# LB003 - 合并两个有序链表 题解

## 思路是怎么想出来的

### 第一步：明确问题

```
问题：两个升序链表合并成一个升序链表
示例：1→2→4 和 1→3→4 合并成 1→1→2→3→4→4

暴力法：把两个链表的值都存到数组，排序后再构建链表
- 需要O(n log n)时间排序
- 需要O(n)额外空间
- 可以利用链表已有序的特点优化
```

### 第二步：利用有序性

```
两个链表都是有序的，就像两个有序数组的归并：
- 每次比较两个链表当前节点的值
- 小的那个就是当前最小的，加入结果
- 指针后移，继续比较

为什么不用排序？
因为两个链表已经有序，直接归并就是O(n)时间
```

### 第三步：处理剩余节点

```
当一个链表遍历完后，另一个链表可能还有剩余
- 剩余部分已经是有序的
- 直接接到结果链表后面即可
```

### 第四步：虚拟头节点技巧

```
问题：结果链表的头节点从哪来？
- 可能是list1的头，也可能是list2的头
- 需要特殊处理

解决方案：虚拟头节点
- 创建一个dummy节点
- 所有节点都接在dummy后面
- 最后返回dummy.next
```

---

## 解法1：迭代法

### 代码

```java
public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
    ListNode dummy = new ListNode(0);  // 虚拟头节点
    ListNode curr = dummy;
    
    while (list1 != null && list2 != null) {
        if (list1.val <= list2.val) {
            curr.next = list1;
            list1 = list1.next;
        } else {
            curr.next = list2;
            list2 = list2.next;
        }
        curr = curr.next;
    }
    
    // 连接剩余部分
    if (list1 != null) {
        curr.next = list1;
    } else {
        curr.next = list2;
    }
    
    return dummy.next;
}
```

### 完整执行图解

```
初始：
list1: 1 → 2 → 4
list2: 1 → 3 → 4
dummy: 0
curr: 指向dummy

第1轮：list1.val=1, list2.val=1
1 <= 1，取list1的1
dummy: 0 → 1(list1)
list1: 2 → 4
list2: 1 → 3 → 4

第2轮：list1.val=2, list2.val=1
2 > 1，取list2的1
dummy: 0 → 1 → 1
list1: 2 → 4
list2: 3 → 4

第3轮：list1.val=2, list2.val=3
2 <= 3，取list1的2
dummy: 0 → 1 → 1 → 2
list1: 4
list2: 3 → 4

第4轮：list1.val=4, list2.val=3
4 > 3，取list2的3
dummy: 0 → 1 → 1 → 2 → 3
list1: 4
list2: 4

第5轮：list1.val=4, list2.val=4
4 <= 4，取list1的4
dummy: 0 → 1 → 1 → 2 → 3 → 4
list1: null
list2: 4

list1为null，连接list2剩余部分
dummy: 0 → 1 → 1 → 2 → 3 → 4 → 4

返回 dummy.next = 1 → 1 → 2 → 3 → 4 → 4 ✓
```

---

## 解法2：递归法

```java
public ListNode mergeTwoListsRecursive(ListNode list1, ListNode list2) {
    if (list1 == null) return list2;
    if (list2 == null) return list1;
    
    if (list1.val <= list2.val) {
        list1.next = mergeTwoListsRecursive(list1.next, list2);
        return list1;
    } else {
        list2.next = mergeTwoListsRecursive(list1, list2.next);
        return list2;
    }
}
```

### 递归过程

```
merge(1→2→4, 1→3→4)
    1 <= 1，选list1的1
    1.next = merge(2→4, 1→3→4)
        
        merge(2→4, 1→3→4)
            2 > 1，选list2的1
            1.next = merge(2→4, 3→4)
                
                merge(2→4, 3→4)
                    2 <= 3，选list1的2
                    2.next = merge(4, 3→4)
                        
                        merge(4, 3→4)
                            4 > 3，选list2的3
                            3.next = merge(4, 4)
                                
                                merge(4, 4)
                                    4 <= 4，选list1的4
                                    4.next = merge(null, 4)
                                        
                                        merge(null, 4)
                                            return 4
                                    
                                    return 4 → 4
                                
                                return 3 → 4 → 4
                            
                            return 2 → 3 → 4 → 4
                        
                        return 1 → 2 → 3 → 4 → 4
                    
                    return 1 → 1 → 2 → 3 → 4 → 4
```

---

## 边界 Case

**Case 1: 都为空**
```
list1 = null, list2 = null
返回 null ✓
```

**Case 2: 一个为空**
```
list1 = null, list2 = 1→2→3
返回 1→2→3 ✓
```

**Case 3: 有负数**
```
list1 = -5→-3→0, list2 = -2→1
返回 -5→-3→-2→0→1 ✓
```

---

## 记忆口诀

```
合并链表双指针，谁小谁先进结果
一个空了接另一个，虚拟头节点省边界
递归思路更简洁，谁小谁就是头节点
next指向合并后，返回当前头节点
```
