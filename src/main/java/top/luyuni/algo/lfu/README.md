# LFU 目录 - 最不经常使用缓存 (Least Frequently Used)

这个目录帮你彻底理解 LFU 缓存，从「LRU vs LFU」到「复杂实现」。

## 为什么学 LFU？

LFU 是另一种缓存淘汰策略，与 LRU 相比有不同的适用场景：

- LRU：淘汰最久未使用（考虑时间）
- LFU：淘汰使用次数最少（考虑频率）

## LRU vs LFU

### 对比示例

```
缓存容量 = 2

操作序列：A, A, A, B, C

LRU策略：
1. 访问A: [A]
2. 访问A: [A]（A移到最近使用）
3. 访问A: [A]
4. 访问B: [A, B]
5. 访问C: 满了，A最久未用，淘汰A，加入C
   结果: [B, C]

LFU策略：
1. 访问A: [A]  A次数=1
2. 访问A: [A]  A次数=2
3. 访问A: [A]  A次数=3
4. 访问B: [A, B]  A=3, B=1
5. 访问C: 满了，B次数最少(1)，淘汰B，加入C
   结果: [A, C]  A=3, C=1

区别：
- LRU淘汰了A（虽然A被访问了3次，但最近没访问）
- LFU淘汰了B（B只被访问了1次）

适用场景：
- LRU：局部性原理，最近访问的很可能再访问
- LFU：频率高的更重要，如热门视频、热门文章
```

## LFU 的核心概念

### 需要维护的信息

```
1. key → value 的映射（和LRU一样）
2. key → 使用次数 的映射
3. 相同使用次数的 key 列表（需要按时间排序，淘汰最久的）
4. 当前最小使用次数

数据结构选择：
- 哈希表1：key → [value, count, node]
- 哈希表2：count → 双向链表（相同count的节点，按时间排序）
- 变量：minCount（最小使用次数）
```

### 为什么需要多个双向链表？

```
不同使用次数的节点分开存储：

count=3: [A]  ← 使用3次的节点
count=2: []   ← 使用2次的节点
count=1: [B, C]  ← 使用1次的节点，B比C早

当需要淘汰时：
1. 找最小count（这里是1）
2. 在该count的链表中，淘汰最久的（B）

为什么每个count一个链表？
- 需要快速找到相同count的所有节点
- 需要在这些节点中按时间排序
- 淘汰时O(1)找到最久的
```

## LFU 的实现

```java
class LFUCache {
    // 节点类
    class Node {
        int key, value, count;
        Node prev, next;
        public Node(int key, int value) {
            this.key = key;
            this.value = value;
            this.count = 1;  // 初始次数为1
        }
    }
    
    // 双向链表类
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
        
        // 添加到头部
        public void addToHead(Node node) {
            node.prev = head;
            node.next = head.next;
            head.next.prev = node;
            head.next = node;
            size++;
        }
        
        // 删除节点
        public void remove(Node node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            size--;
        }
        
        // 删除尾部（最久未用）
        public Node removeTail() {
            if (size == 0) return null;
            Node node = tail.prev;
            remove(node);
            return node;
        }
    }
    
    private Map<Integer, Node> nodeMap;      // key → node
    private Map<Integer, DLinkedList> freqMap; // count → 链表
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
        
        // 增加使用次数
        increaseCount(node);
        return node.value;
    }
    
    public void put(int key, int value) {
        if (capacity == 0) return;
        
        Node node = nodeMap.get(key);
        
        if (node != null) {
            // 更新值
            node.value = value;
            increaseCount(node);
        } else {
            // 新建节点
            if (size == capacity) {
                // 淘汰最小count的尾部节点
                DLinkedList list = freqMap.get(minCount);
                Node tailNode = list.removeTail();
                nodeMap.remove(tailNode.key);
                size--;
            }
            
            // 添加新节点
            node = new Node(key, value);
            nodeMap.put(key, node);
            DLinkedList list = freqMap.computeIfAbsent(1, k -> new DLinkedList());
            list.addToHead(node);
            minCount = 1;  // 新节点count=1
            size++;
        }
    }
    
    // 增加节点的使用次数
    private void increaseCount(Node node) {
        int count = node.count;
        DLinkedList list = freqMap.get(count);
        list.remove(node);  // 从原链表移除
        
        // 如果原链表空了且是最小count，更新minCount
        if (count == minCount && list.size == 0) {
            minCount++;
        }
        
        // 加入新count的链表
        node.count++;
        DLinkedList newList = freqMap.computeIfAbsent(count + 1, k -> new DLinkedList());
        newList.addToHead(node);
    }
}
```

## 操作过程图解

### get 操作

```
缓存：
count=2: [A]
count=1: [B, C]
minCount=1

get(A):
┌─────────────────────────────────────────┐
│  1. 找到节点A，count=2                   │
│  2. 从count=2的链表移除A                 │
│  3. A的count变成3                        │
│  4. 加入count=3的链表头部                │
│  5. minCount不变（还是1）                │
└─────────────────────────────────────────┘

结果：
count=3: [A]
count=2: []
count=1: [B, C]
```

### put 操作（新key，缓存未满）

```
缓存：
count=2: [A]
count=1: [B]
容量=3，当前size=2

put(C, value):
┌─────────────────────────────────────────┐
│  1. 新建节点C，count=1                   │
│  2. 加入count=1的链表头部                │
│  3. minCount=1                           │
│  4. size=3                               │
└─────────────────────────────────────────┘

结果：
count=2: [A]
count=1: [C, B]
```

### put 操作（新key，缓存已满）

```
缓存：
count=2: [A]
count=1: [B, C]  ← B最久
容量=3，当前size=3
minCount=1

put(D, value):
┌─────────────────────────────────────────┐
│  1. 缓存满了，需要淘汰                   │
│  2. 找minCount=1的链表                   │
│  3. 淘汰尾部节点B                        │
│  4. 从nodeMap删除B                       │
│  5. 新建节点D，count=1                   │
│  6. 加入count=1的链表头部                │
│  7. minCount=1                           │
└─────────────────────────────────────────┘

结果：
count=2: [A]
count=1: [D, C]  ← B被淘汰
```

### put 操作（更新已有key）

```
缓存：
count=2: [A]
count=1: [B, C]

put(B, newValue):
┌─────────────────────────────────────────┐
│  1. 找到节点B，更新value                 │
│  2. 增加B的count（同get操作）            │
│  3. B从count=1移到count=2                │
└─────────────────────────────────────────┘

结果：
count=2: [B, A]
count=1: [C]
```

## 当前内容

| 文件                     | 题目     | 难度 | 核心技巧          |
|------------------------|--------|----|---------------|
| `LFU001_LFUCache.java` | LFU 缓存 | 困难 | 双哈希表 + 多个双向链表 |

## LFU vs LRU 复杂度对比

| 操作  | LRU         | LFU         |
|-----|-------------|-------------|
| get | O(1)        | O(1)        |
| put | O(1)        | O(1)        |
| 空间  | O(capacity) | O(capacity) |

虽然都是O(1)，但LFU常数更大，实现更复杂。

## LFU 学习建议

1. **先理解LRU**：LRU是基础，LFU是进阶
2. **理解多链表结构**：每个count一个链表是关键
3. **注意minCount更新**：淘汰和增加次数时都要更新
4. **手写实现**：面试考LFU较少，但考了就是难题

## 记忆口诀

```
LFU看频率，少的被淘汰
双哈希表结构，key到节点映射
频率到链表，相同次数在一起
get put加次数，换链表来维护
满了淘汰最小频，尾部最久被淘汰
```
