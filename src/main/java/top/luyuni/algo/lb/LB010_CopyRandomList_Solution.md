# LB010 - 复制带随机指针的链表 题解

## 思路是怎么想出来的

### 第一步：明确问题

```
问题：复制一个链表，链表节点除了next指针，还有random指针

random指针可以指向：
- 链表中的任意节点
- null

难点：
- 复制节点时，random指向的节点可能还没创建
- 需要建立原节点和新节点的对应关系
```

### 第二步：发现两种解法

```
解法1：哈希表法（直观）
- 第一次遍历：创建所有新节点，原节点→新节点的映射
- 第二次遍历：设置next和random指针

解法2：原地插入法（空间O(1)）
- 在每个原节点后面插入新节点
- 设置新节点的random（原节点random的next）
- 拆分两个链表
```

### 第三步：理解哈希表法

```
核心：用哈希表记录原节点和新节点的对应关系

步骤：
1. 遍历原链表，创建新节点
2. map.put(原节点, 新节点)
3. 再次遍历，设置新节点的next和random
   - newNode.next = map.get(oldNode.next)
   - newNode.random = map.get(oldNode.random)
```

---

## 解法1：哈希表法

```java
public Node copyRandomList(Node head) {
    // 空链表直接返回
    if (head == null) {
        return null;
    }
    
    // 哈希表：原节点 → 新节点
    Map<Node, Node> map = new HashMap<>();
    
    // 第一遍：创建所有新节点，建立映射
    Node curr = head;           // 从原链表头开始
    while (curr != null) {
        // 创建新节点，值相同
        Node newNode = new Node(curr.val);
        // 放入哈希表
        map.put(curr, newNode);
        // 移动到下一个原节点
        curr = curr.next;
    }
    
    // 第二遍：设置新节点的next和random
    curr = head;                // 重新从原链表头开始
    while (curr != null) {
        // 获取当前原节点对应的新节点
        Node newNode = map.get(curr);
        
        // 设置新节点的next
        // 原节点的next对应的新节点
        newNode.next = map.get(curr.next);
        
        // 设置新节点的random
        // 原节点的random对应的新节点
        newNode.random = map.get(curr.random);
        
        // 移动到下一个原节点
        curr = curr.next;
    }
    
    // 返回新链表的头节点
    return map.get(head);
}
```

### 完整执行图解

```
示例：
原链表：7 → 13 → 11
       ↓    ↓    ↓
      null  7    11

random关系：
- 7.random = null
- 13.random = 7
- 11.random = 11（自己指向自己）

========== 第一遍：创建新节点 ==========

初始：map = {}

curr = 7:
┌─────────────────────────────────────────┐
│  创建新节点 newNode(7)                   │
│  map.put(7, 7')                          │
│  map = {7→7'}                            │
└─────────────────────────────────────────┘

curr = 13:
┌─────────────────────────────────────────┐
│  创建新节点 newNode(13)                  │
│  map.put(13, 13')                        │
│  map = {7→7', 13→13'}                    │
└─────────────────────────────────────────┘

curr = 11:
┌─────────────────────────────────────────┐
│  创建新节点 newNode(11)                  │
│  map.put(11, 11')                        │
│  map = {7→7', 13→13', 11→11'}            │
└─────────────────────────────────────────┘

========== 第二遍：设置next和random ==========

curr = 7:
┌─────────────────────────────────────────┐
│  newNode = map.get(7) = 7'               │
│  newNode.next = map.get(7.next=13) = 13' │
│  newNode.random = map.get(7.random=null) │
│                = null                    │
└─────────────────────────────────────────┘

curr = 13:
┌─────────────────────────────────────────┐
│  newNode = map.get(13) = 13'             │
│  newNode.next = map.get(13.next=11) = 11'│
│  newNode.random = map.get(13.random=7)   │
│                = 7'                      │
└─────────────────────────────────────────┘

curr = 11:
┌─────────────────────────────────────────┐
│  newNode = map.get(11) = 11'             │
│  newNode.next = map.get(11.next=null)    │
│                = null                    │
│  newNode.random = map.get(11.random=11)  │
│                = 11'                     │
└─────────────────────────────────────────┘

新链表：
7' → 13' → 11'
↓     ↓      ↓
null  7'    11'

返回 map.get(head) = 7' ✓
```

---

## 解法2：原地插入法（进阶）

```java
public Node copyRandomListO1(Node head) {
    if (head == null) {
        return null;
    }
    
    // 步骤1：在每个原节点后面插入新节点
    Node curr = head;
    while (curr != null) {
        // 创建新节点
        Node newNode = new Node(curr.val);
        // 新节点的next指向原节点的next
        newNode.next = curr.next;
        // 原节点的next指向新节点
        curr.next = newNode;
        // 跳过新节点，移动到下一个原节点
        curr = newNode.next;
    }
    
    // 步骤2：设置新节点的random
    curr = head;
    while (curr != null) {
        // 新节点是原节点的next
        Node newNode = curr.next;
        // 新节点的random = 原节点random的next
        if (curr.random != null) {
            newNode.random = curr.random.next;
        }
        // 跳过新节点，移动到下一个原节点
        curr = newNode.next;
    }
    
    // 步骤3：拆分两个链表
    curr = head;
    Node newHead = head.next;   // 新链表的头
    while (curr != null) {
        Node newNode = curr.next;       // 新节点
        curr.next = newNode.next;       // 恢复原链表的next
        if (newNode.next != null) {
            newNode.next = newNode.next.next;   // 设置新链表的next
        }
        curr = curr.next;               // 移动到下一个原节点
    }
    
    return newHead;
}
```

### 原地插入法图解

```
原链表：7 → 13 → 11

========== 步骤1：插入新节点 ==========

初始：7 → 13 → 11

curr = 7:
┌─────────────────────────────────────────┐
│  创建7'                                  │
│  7' → 13                                 │
│  7 → 7'                                  │
└─────────────────────────────────────────┘
结果：7 → 7' → 13 → 11

curr = 13:
┌─────────────────────────────────────────┐
│  创建13'                                 │
│  13' → 11                                │
│  13 → 13'                                │
└─────────────────────────────────────────┘
结果：7 → 7' → 13 → 13' → 11

curr = 11:
┌─────────────────────────────────────────┐
│  创建11'                                 │
│  11' → null                              │
│  11 → 11'                                │
└─────────────────────────────────────────┘
结果：7 → 7' → 13 → 13' → 11 → 11'

========== 步骤2：设置random ==========

7.random = null:
  7'.random = null

13.random = 7:
  13'.random = 7.random.next = 7'

11.random = 11:
  11'.random = 11.random.next = 11'

========== 步骤3：拆分 ==========

原链表恢复：7 → 13 → 11
新链表：7' → 13' → 11'

random关系也正确复制！
```

---

## 关键点分析

### 哈希表法的核心

```
关键：建立原节点 → 新节点的映射

为什么需要两次遍历？
- 第一次：创建所有节点（因为random可能指向还没创建的节点）
- 第二次：设置next和random（此时所有节点都已创建）

map.get(null) 返回 null，正好符合需求
```

### 原地插入法的巧妙之处

```
原节点A，新节点A'，A'插在A后面

A.random = B
那么 A'.random = B'
因为 B' 就是 B 的 next

所以：A'.random = A.random.next
```

---

## 边界 Case

**Case 1: 空链表**
```
head = null
返回null ✓
```

**Case 2: 单个节点，random指向自己**
```
1 → null
↓
1

复制后：
1' → null
↓
1'
```

**Case 3: 所有random都是null**
```
正常复制，所有新节点random=null ✓
```

**Case 4: random形成环**
```
A.random = B
B.random = A

复制后：
A'.random = B'
B'.random = A'
```

---

## 复杂度分析

```
哈希表法：
- 时间：O(n)，两次遍历
- 空间：O(n)，哈希表

原地插入法：
- 时间：O(n)，三次遍历
- 空间：O(1)，只用指针
```

---

## 记忆口诀

```
复制链表有random，哈希表法最直观
原节点映射新节点，两遍遍历就完成
第一遍创建节点，第二遍设置指针
进阶方法原地插，空间复杂度是O1
每个原节点后面，插入对应新节点
random指向有技巧，原random的next就是
```
