# LRU 目录 - 最近最少使用缓存 (Least Recently Used)

这个目录帮你彻底理解 LRU 缓存，从「为什么要用LRU」到「手写实现」。

## 为什么学 LRU？

LRU 是面试最高频的算法题之一，也是实际工程中常用的缓存淘汰策略：
- Redis 缓存淘汰策略之一
- 操作系统页面置换算法
- 浏览器缓存机制
- CPU 高速缓存

## LRU 的核心概念

### 什么是 LRU？

```
LRU（Least Recently Used）：最近最少使用

核心思想：
- 缓存容量有限
- 当缓存满了，淘汰最久没有被使用的数据
- 就像收拾书桌，把最久没看的书先收起来

示例：容量=3
操作序列：put(1,1), put(2,2), put(3,3), get(1), put(4,4)

过程：
1. put(1,1): [1]
2. put(2,2): [1, 2]
3. put(3,3): [1, 2, 3]  ← 满了
4. get(1): 返回1，1变成最近使用 [2, 3, 1]
5. put(4,4): 满了，淘汰最久未用的2，加入4 [3, 1, 4]
```

### 为什么用哈希表 + 双向链表？

```
需要支持的操作：
1. get(key)：查询数据
   - 需要O(1)时间找到key对应的value
   - 需要O(1)时间把key移到最近使用

2. put(key, value)：插入/更新数据
   - 需要O(1)时间插入
   - 需要O(1)时间把key移到最近使用
   - 缓存满时，需要O(1)时间淘汰最久未用的

数据结构选择：
- 哈希表：O(1)查询、插入
- 双向链表：O(1)移动节点、删除节点

组合：
- 哈希表：key → 链表节点
- 双向链表：按使用顺序排列，头部最近使用，尾部最久未用
```

### 双向链表的结构

```
┌─────────┐     ┌─────────┐     ┌─────────┐
│  Head   │←────│  Node1  │←────│  Node2  │←──── ...
│ (dummy) │────→│  key=1  │────→│  key=2  │────→ ...
│         │     │  val=10 │     │  val=20 │
└─────────┘     └─────────┘     └─────────┘

最近使用 ←────────────────────────→ 最久未用

为什么是双向链表？
- 删除节点需要知道前驱节点
- 单链表删除需要O(n)找前驱
- 双向链表删除O(1)
```

## Java 中的 LRU 实现

### 方式1：LinkedHashMap（最简单）

```java
class LRUCache extends LinkedHashMap<Integer, Integer> {
    private int capacity;
    
    public LRUCache(int capacity) {
        // accessOrder=true 表示按访问顺序排序
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }
    
    public int get(int key) {
        return super.getOrDefault(key, -1);
    }
    
    public void put(int key, int value) {
        super.put(key, value);
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
        // 当大小超过容量时，移除最老的元素
        return size() > capacity;
    }
}
```

### 方式2：手写实现（面试常考）

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

## 操作过程图解

### get 操作

```
缓存：[1, 2, 3, 4]（1最近使用，4最久未用）

get(3):
┌─────────────────────────────────────────┐
│  1. 哈希表找到节点3                       │
│  2. 返回值                                │
│  3. 把节点3移到头部                       │
└─────────────────────────────────────────┘

结果：[3, 1, 2, 4]
      ↑
    最近使用
```

### put 操作（key已存在）

```
缓存：[1, 2, 3, 4]

put(2, 20):
┌─────────────────────────────────────────┐
│  1. 哈希表找到节点2                       │
│  2. 更新值为20                            │
│  3. 把节点2移到头部                       │
└─────────────────────────────────────────┘

结果：[2, 1, 3, 4]
```

### put 操作（key不存在，缓存未满）

```
缓存：[1, 2, 3]  容量=4

put(4, 40):
┌─────────────────────────────────────────┐
│  1. 新建节点4                             │
│  2. 加入哈希表                            │
│  3. 添加到链表头部                        │
└─────────────────────────────────────────┘

结果：[4, 1, 2, 3]
```

### put 操作（key不存在，缓存已满）

```
缓存：[1, 2, 3]  容量=3

put(4, 40):
┌─────────────────────────────────────────┐
│  1. 新建节点4                             │
│  2. 加入哈希表                            │
│  3. 添加到链表头部                        │
│  4. 缓存满了（size=4>3）                  │
│  5. 删除尾部节点（最久未用的3）            │
│  6. 从哈希表删除key=3                     │
└─────────────────────────────────────────┘

结果：[4, 1, 2]
被删除：3
```

## 当前内容

| 文件 | 题目 | 难度 | 核心技巧 |
|------|------|------|----------|
| `LRU001_LRUCache.java` | LRU 缓存 | 中等 | 哈希表 + 双向链表 |
| `LRU002_LFUCache.java` | LFU 缓存 | 困难 | 双哈希表 + 多个双向链表 |

## LRU 学习建议

1. **先理解原理**：为什么要淘汰最久未用的
2. **再理解数据结构**：为什么用哈希表+双向链表
3. **能手写实现**：面试常考，LinkedHashMap写法简单但不保险
4. **注意边界**：容量为0、重复put、get不存在等

## 记忆口诀

```
LRU缓存容量限，满了淘汰最久用
哈希链表来组合，查询移动都O1
哈希存key到节点，链表按顺序排列
头部最近尾部久，get put都移头部
满了删除尾部节，缓存淘汰完成啦
```
