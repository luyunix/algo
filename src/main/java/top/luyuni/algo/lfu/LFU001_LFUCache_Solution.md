# LFU001 - LFU 缓存 题解

## 思路是怎么想出来的

### 第一步：明确问题

```
问题：实现一个容量有限的缓存，淘汰使用次数最少的key
- 如果多个key次数相同，淘汰最久未用的
- 需要维护每个key的使用次数

示例：容量=2
put(1,1): [1]  count(1)=1
put(2,2): [1,2]  count(1)=1, count(2)=1
get(1): 返回1，count(1)=2
put(3,3): 满了，count(2)=1最少，淘汰2，加入3
```

### 第二步：分析需要维护的信息

```
需要维护：
1. key → value 映射（和LRU一样）
2. key → 使用次数
3. 相同次数的key列表（按时间排序）
4. 当前最小使用次数

数据结构选择：
- 哈希表1：key → node（包含key, value, count）
- 哈希表2：count → 双向链表（相同count的节点）
- 变量：minCount

为什么每个count一个链表？
- 需要快速找到相同count的所有key
- 在这些key中按时间排序
- 淘汰时O(1)找到最久的
```

### 第三步：设计算法

```
get(key)：
1. 找到node，返回值
2. node的count增加
3. 把node从原count链表移到新count链表
4. 如果原链表空了且是minCount，minCount++

put(key, value)：
1. 如果key存在：更新value，同get增加count
2. 如果key不存在：
   a. 如果满了，淘汰minCount链表的尾部
   b. 新建node，count=1
   c. 加入count=1的链表头部
   d. minCount=1
```

---

## 解法：双哈希表 + 多个双向链表

```java
class LFUCache {
    class Node {
        int key, value, count;
        Node prev, next;
        public Node(int key, int value) {
            this.key = key;
            this.value = value;
            this.count = 1;
        }
    }
    
    class DLinkedList {
        Node head, tail;
        int size;
        
        public DLinkedList() {
            head = new Node(0, 0);
            tail = new Node(0, 0);
            head.next = tail;
            tail.prev = head;
            size = 0;
        }
        
        public void addToHead(Node node) {
            node.prev = head;
            node.next = head.next;
            head.next.prev = node;
            head.next = node;
            size++;
        }
        
        public void remove(Node node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            size--;
        }
        
        public Node removeTail() {
            if (size == 0) return null;
            Node node = tail.prev;
            remove(node);
            return node;
        }
    }
    
    private Map<Integer, Node> nodeMap;
    private Map<Integer, DLinkedList> freqMap;
    private int capacity, size, minCount;
    
    public LFUCache(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        this.minCount = 0;
        nodeMap = new HashMap<>();
        freqMap = new HashMap<>();
    }
    
    public int get(int key) {
        Node node = nodeMap.get(key);
        if (node == null) return -1;
        increaseCount(node);
        return node.value;
    }
    
    public void put(int key, int value) {
        if (capacity == 0) return;
        
        Node node = nodeMap.get(key);
        
        if (node != null) {
            node.value = value;
            increaseCount(node);
        } else {
            if (size == capacity) {
                DLinkedList list = freqMap.get(minCount);
                Node tailNode = list.removeTail();
                nodeMap.remove(tailNode.key);
                size--;
            }
            
            node = new Node(key, value);
            nodeMap.put(key, node);
            DLinkedList list = freqMap.computeIfAbsent(1, k -> new DLinkedList());
            list.addToHead(node);
            minCount = 1;
            size++;
        }
    }
    
    private void increaseCount(Node node) {
        int count = node.count;
        DLinkedList list = freqMap.get(count);
        list.remove(node);
        
        if (count == minCount && list.size == 0) {
            minCount++;
        }
        
        node.count++;
        DLinkedList newList = freqMap.computeIfAbsent(count + 1, k -> new DLinkedList());
        newList.addToHead(node);
    }
}
```

### 完整执行图解

```
容量=2
操作序列：put(1,1), put(2,2), get(1), put(3,3), get(2)

========== put(1,1) ==========
┌─────────────────────────────────────────┐
│  新建node(1,1)，count=1                  │
│  nodeMap: {1→node1}                      │
│  freqMap[1]: [node1]                     │
│  minCount=1                              │
└─────────────────────────────────────────┘

========== put(2,2) ==========
┌─────────────────────────────────────────┐
│  新建node(2,2)，count=1                  │
│  nodeMap: {1→node1, 2→node2}             │
│  freqMap[1]: [node2, node1]（头部最新）   │
│  minCount=1                              │
└─────────────────────────────────────────┘

========== get(1) ==========
┌─────────────────────────────────────────┐
│  找到node1，返回1                        │
│  node1的count从1变成2                    │
│  从freqMap[1]移除node1                   │
│  freqMap[1]: [node2]                     │
│  加入freqMap[2]: [node1]                 │
│  minCount不变（还是1，因为freqMap[1]非空）│
└─────────────────────────────────────────┘

当前状态：
freqMap[1]: [node2]  count(2)=1
freqMap[2]: [node1]  count(1)=2
minCount=1

========== put(3,3) ==========
┌─────────────────────────────────────────┐
│  满了（size=2），需要淘汰                │
│  minCount=1，找freqMap[1]                │
│  淘汰尾部node2                           │
│  nodeMap删除key=2                        │
│  新建node(3,3)，count=1                  │
│  加入freqMap[1]: [node3]                 │
│  minCount=1                              │
└─────────────────────────────────────────┘

当前状态：
freqMap[1]: [node3]  count(3)=1
freqMap[2]: [node1]  count(1)=2
minCount=1

========== get(2) ==========
┌─────────────────────────────────────────┐
│  nodeMap查找key=2                        │
│  不存在（被淘汰了）                       │
│  返回-1                                  │
└─────────────────────────────────────────┘
返回 -1 ✓
```

### 状态变化表

```
操作         nodeMap              freqMap              minCount
────────────────────────────────────────────────────────────────
初始         {}                   {}                   -
put(1,1)     {1→n1}               {1:[n1]}             1
put(2,2)     {1→n1,2→n2}          {1:[n2,n1]}          1
get(1)       {1→n1,2→n2}          {1:[n2],2:[n1]}      1
put(3,3)     {1→n1,3→n3}          {1:[n3],2:[n1]}      1
             （2被淘汰）
get(2)       -                    -                    -
             返回-1
```

---

## 关键点分析

### 为什么需要minCount？

```
淘汰时：
- 需要找到使用次数最少的key
- 如果没有minCount，需要遍历所有freqMap的key找最小值
- O(n)时间，不满足O(1)要求

维护minCount：
- 新节点加入，minCount=1
- 节点count增加，如果原count等于minCount且链表空了，minCount++
- 淘汰时直接用minCount找链表
```

### 为什么新节点count=1？

```
put操作就算使用了一次：
- put(1,1)后，count(1)=1
- 如果get(1)，count变成2
- 这样设计符合直觉：插入就算一次使用
```

---

## 边界 Case

**Case 1: 容量为0**
```
LFUCache(0)
任何put都直接返回
```

**Case 2: 相同count淘汰最久的**
```
put(1,1), put(2,2), put(3,3)
→ count(1)=1, count(2)=1, count(3)=1
→ 3加入时，1和2都是count=1
→ 淘汰最久的1（在链表尾部）
```

**Case 3: 更新已有key的value**
```
put(1,1)
put(1,2) → 更新value，count增加
```

**Case 4: count增加后minCount变化**
```
put(1,1), put(2,2)
get(1), get(1) → count(1)=3
get(2) → count(2)=2
此时freqMap[1]为空
minCount变成2
```

---

## LRU vs LFU 对比

| 特性 | LRU | LFU |
|------|-----|-----|
| 淘汰策略 | 最久未用 | 使用次数最少 |
| 数据结构 | 哈希表+1个链表 | 哈希表+多个链表 |
| 实现复杂度 | 中等 | 较复杂 |
| 适用场景 | 局部性原理 | 频率差异大 |
| 面试频率 | 极高 | 中等 |

---

## 记忆口诀

```
LFU看频率，少的被淘汰
双哈希表结构，key到节点映射
频率到链表，相同次数在一起
get put加次数，换链表来维护
满了淘汰最小频，尾部最久被淘汰
```
