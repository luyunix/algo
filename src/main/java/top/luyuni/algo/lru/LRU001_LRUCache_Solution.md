# LRU001 - LRU 缓存 题解

## 思路是怎么想出来的

### 第一步：明确问题

```
问题：实现一个容量有限的缓存，支持get和put操作
- get：获取key对应的value，不存在返回-1
- put：插入或更新key-value
- 容量满时，淘汰最久未使用的key

示例：容量=2
put(1,1): [1]
put(2,2): [1,2]
get(1): 返回1，1变成最近使用 [2,1]
put(3,3): 满了，淘汰2，加入3 [1,3]
```

### 第二步：分析操作需求

```
需要的操作：
1. get(key)：查询value
   - O(1)时间找到key
   - O(1)时间把key移到"最近使用"

2. put(key, value)：插入/更新
   - O(1)时间插入
   - O(1)时间把key移到"最近使用"
   - 满时O(1)时间淘汰"最久未用"

数据结构选择：
- 数组/链表：查询O(n)，不满足
- 哈希表：查询O(1)，但无法维护顺序
- 哈希表 + 链表：查询O(1)，维护顺序O(1) ✓
```

### 第三步：设计数据结构

```
哈希表 + 双向链表：
- 哈希表：key → 链表节点（O(1)查询）
- 双向链表：按使用顺序排列
  - 头部：最近使用的节点
  - 尾部：最久未用的节点

为什么双向链表？
- 删除节点需要知道前驱
- 单链表找前驱O(n)
- 双向链表O(1)
```

---

## 解法：哈希表 + 双向链表

```java
class LRUCache {
    // 双向链表节点
    class DLinkedNode {
        int key, value;
        DLinkedNode prev, next;
        public DLinkedNode() {}
        public DLinkedNode(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }
    
    private Map<Integer, DLinkedNode> cache;
    private int capacity, size;
    private DLinkedNode head, tail;  // 伪头部和伪尾部
    
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        cache = new HashMap<>();
        
        // 初始化双向链表
        head = new DLinkedNode();
        tail = new DLinkedNode();
        head.next = tail;
        tail.prev = head;
    }
    
    public int get(int key) {
        DLinkedNode node = cache.get(key);
        if (node == null) return -1;
        
        // 移动到头部
        moveToHead(node);
        return node.value;
    }
    
    public void put(int key, int value) {
        DLinkedNode node = cache.get(key);
        
        if (node == null) {
            // 新建节点
            node = new DLinkedNode(key, value);
            cache.put(key, node);
            addToHead(node);
            size++;
            
            // 超出容量，删除尾部
            if (size > capacity) {
                DLinkedNode tailNode = removeTail();
                cache.remove(tailNode.key);
                size--;
            }
        } else {
            // 更新值
            node.value = value;
            moveToHead(node);
        }
    }
    
    // 添加节点到头部
    private void addToHead(DLinkedNode node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }
    
    // 删除节点
    private void removeNode(DLinkedNode node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
    
    // 移动到头部
    private void moveToHead(DLinkedNode node) {
        removeNode(node);
        addToHead(node);
    }
    
    // 删除尾部节点
    private DLinkedNode removeTail() {
        DLinkedNode node = tail.prev;
        removeNode(node);
        return node;
    }
}
```

### 完整执行图解

```
容量=2
操作序列：put(1,1), put(2,2), get(1), put(3,3), get(2)

========== put(1,1) ==========
┌─────────────────────────────────────────┐
│  新建节点(1,1)                           │
│  加入哈希表：{1→node1}                    │
│  添加到链表头部：[head, node1, tail]       │
│  size=1                                  │
└─────────────────────────────────────────┘
链表：head ↔ node1(1,1) ↔ tail

========== put(2,2) ==========
┌─────────────────────────────────────────┐
│  新建节点(2,2)                           │
│  加入哈希表：{1→node1, 2→node2}           │
│  添加到链表头部：[head, node2, node1, tail]│
│  size=2                                  │
└─────────────────────────────────────────┘
链表：head ↔ node2(2,2) ↔ node1(1,1) ↔ tail

========== get(1) ==========
┌─────────────────────────────────────────┐
│  哈希表找到node1                         │
│  返回值1                                 │
│  把node1移到头部                         │
│  链表：[head, node1, node2, tail]         │
└─────────────────────────────────────────┘
链表：head ↔ node1(1,1) ↔ node2(2,2) ↔ tail

========== put(3,3) ==========
┌─────────────────────────────────────────┐
│  key=3不存在，新建节点(3,3)               │
│  加入哈希表：{1→node1, 2→node2, 3→node3}  │
│  添加到链表头部：[head, node3, node1, node2, tail]│
│  size=3 > capacity=2，需要淘汰           │
│  删除尾部node2                           │
│  从哈希表删除key=2                       │
│  size=2                                  │
└─────────────────────────────────────────┘
链表：head ↔ node3(3,3) ↔ node1(1,1) ↔ tail
哈希表：{1→node1, 3→node3}

========== get(2) ==========
┌─────────────────────────────────────────┐
│  哈希表查找key=2                         │
│  不存在，返回-1                          │
└─────────────────────────────────────────┘
返回 -1 ✓
```

### 链表变化表

```
操作         链表（头部→尾部）              size
─────────────────────────────────────────────────
初始         head ↔ tail                  0
put(1,1)     head ↔ 1 ↔ tail              1
put(2,2)     head ↔ 2 ↔ 1 ↔ tail          2
get(1)       head ↔ 1 ↔ 2 ↔ tail          2
put(3,3)     head ↔ 3 ↔ 1 ↔ tail          2
             （2被淘汰）
get(2)       -                            2
             返回-1
```

---

## 关键点分析

### 为什么用伪头部和伪尾部？

```
不用伪节点：
- 添加第一个节点时，需要特殊处理head为null
- 删除最后一个节点时，需要特殊处理
- 代码有很多if判断

用伪节点：
- head和tail始终存在
- 添加/删除操作统一
- 代码更简洁
```

### 为什么链表节点要存key？

```
淘汰尾部节点时：
- 需要从哈希表中删除对应的key
- 链表节点只存value，不知道key是什么
- 所以节点要同时存key和value
```

---

## 边界 Case

**Case 1: 容量为0**

```
LRUCache(0)
put(1,1) → 直接返回，存不进去
get(1) → 返回-1
```

**Case 2: 重复put同一个key**

```
put(1,1)
put(1,2) → 更新value为2，移到头部
get(1) → 返回2
```

**Case 3: get不存在的key**

```
get(999) → 返回-1
```

**Case 4: 连续put直到满，再put新的**

```
容量=2
put(1,1), put(2,2), put(3,3)
→ 1被淘汰，缓存[3,2]
```

---

## 记忆口诀

```
LRU缓存容量限，满了淘汰最久用
哈希链表来组合，查询移动都O1
哈希存key到节点，链表按顺序排列
头部最近尾部久，get put都移头部
满了删除尾部节，缓存淘汰完成啦
```
