# TRIE001 - 实现 Trie (前缀树) 题解

## 思路是怎么想出来的

### 第一步：明确问题

```
问题：实现一个 Trie 类，支持 insert、search、startsWith 三个操作

示例：
insert("apple")
search("apple") → true
search("app") → false（"app"不是单词，只是前缀）
startsWith("app") → true（"apple"的前缀是"app"）
insert("app")
search("app") → true
```

### 第二步：设计数据结构

```
关键洞察：
- 需要快速判断一个字符串是否存在
- 需要快速判断一个前缀是否存在
- 多个字符串可能有相同前缀，需要共享存储

Trie 结构：
- 每个节点有多个子节点（每个字符一个）
- 从根到节点的路径表示一个前缀
- 节点标记是否为单词结尾

节点设计：
class TrieNode {
    TrieNode[] children = new TrieNode[26];  // 假设只有小写字母
    boolean isEndOfWord;  // 是否是一个完整单词的结尾
}
```

### 第三步：设计三个操作

```
insert(word)：
- 从根开始，逐个字符向下
- 没有对应子节点就创建
- 最后一个字符标记 isEndOfWord = true

search(word)：
- 从根开始，逐个字符向下
- 中途找不到返回 false
- 找到后检查 isEndOfWord（必须是一个完整单词）

startsWith(prefix)：
- 和 search 类似，但不需要检查 isEndOfWord
- 能走完前缀就返回 true
```

---

## 解法：Trie 基础实现

```java
class Trie {
    class TrieNode {
        TrieNode[] children = new TrieNode[26];
        boolean isEndOfWord;
    }
    
    private TrieNode root;
    
    public Trie() {
        root = new TrieNode();
    }
    
    // 插入单词
    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            int index = c - 'a';
            if (node.children[index] == null) {
                node.children[index] = new TrieNode();
            }
            node = node.children[index];
        }
        node.isEndOfWord = true;
    }
    
    // 查找单词
    public boolean search(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            int index = c - 'a';
            if (node.children[index] == null) {
                return false;
            }
            node = node.children[index];
        }
        return node.isEndOfWord;
    }
    
    // 查找前缀
    public boolean startsWith(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            int index = c - 'a';
            if (node.children[index] == null) {
                return false;
            }
            node = node.children[index];
        }
        return true;
    }
}
```

### 完整执行图解

```
操作序列：insert("apple"), search("apple"), search("app"), startsWith("app"), insert("app"), search("app")

========== insert("apple") ==========
┌─────────────────────────────────────────┐
│  从根开始                               │
│  'a': 创建子节点                         │
│  'p': 创建子节点                         │
│  'p': 创建子节点                         │
│  'l': 创建子节点                         │
│  'e': 创建子节点，标记isEndOfWord=true   │
└─────────────────────────────────────────┘

Trie结构：
        root
         |
         a
         |
         p
         |
         p
         |
         l
         |
         e(#)
         
(#)表示isEndOfWord=true

========== search("apple") ==========
┌─────────────────────────────────────────┐
│  从根开始                               │
│  'a'→'p'→'p'→'l'→'e'                   │
│  路径存在，且e.isEndOfWord=true          │
│  返回true                               │
└─────────────────────────────────────────┘
返回 true ✓

========== search("app") ==========
┌─────────────────────────────────────────┐
│  从根开始                               │
│  'a'→'p'→'p'                            │
│  路径存在，但p.isEndOfWord=false         │
│  （"app"只是前缀，不是单词）              │
│  返回false                              │
└─────────────────────────────────────────┘
返回 false ✓

========== startsWith("app") ==========
┌─────────────────────────────────────────┐
│  从根开始                               │
│  'a'→'p'→'p'                            │
│  路径存在                               │
│  （不需要检查isEndOfWord）               │
│  返回true                               │
└─────────────────────────────────────────┘
返回 true ✓

========== insert("app") ==========
┌─────────────────────────────────────────┐
│  从根开始                               │
│  'a'→'p'→'p'（已存在）                  │
│  标记p.isEndOfWord=true                  │
└─────────────────────────────────────────┘

Trie结构：
        root
         |
         a
         |
         p
         |
         p(#)  ← 现在也是单词结尾
         |
         l
         |
         e(#)

========== search("app") ==========
┌─────────────────────────────────────────┐
│  从根开始                               │
│  'a'→'p'→'p'                            │
│  路径存在，且p.isEndOfWord=true          │
│  返回true                               │
└─────────────────────────────────────────┘
返回 true ✓
```

### Trie 结构变化表

```
操作              Trie结构（简化）
─────────────────────────────────────────
初始              root
insert("apple")   root→a→p→p→l→e(#)
search("apple")   无变化，返回true
search("app")     无变化，返回false
startsWith("app") 无变化，返回true
insert("app")     root→a→p→p(#)→l→e(#)
search("app")     无变化，返回true
```

---

## 关键点分析

### 为什么用数组而不是 HashMap？

```
数组 children[26]：
- 优点：访问 O(1)，无哈希冲突，内存连续
- 缺点：只适用于字符集固定且小的情况（如小写字母）

HashMap<Character, TrieNode>：
- 优点：适用于任意字符集
- 缺点：有一定开销

本题只有小写字母，用数组更高效。
```

### search 和 startsWith 的区别

```
search：
- 路径必须存在
- 最后一个节点的 isEndOfWord 必须为 true

startsWith：
- 路径必须存在
- 不需要检查 isEndOfWord

示例：
insert("apple")
search("app") → false（"app"不是单词）
startsWith("app") → true（"app"是前缀）
```

---

## 边界 Case

**Case 1: 空字符串**

```
insert("")
search("") → true（根节点的isEndOfWord）
```

**Case 2: 重复插入**

```
insert("a")
insert("a")
search("a") → true
无异常，重复标记isEndOfWord
```

**Case 3: 查找不存在的单词**

```
insert("apple")
search("apply") → false（走到'p'后，'l'≠'p'）
```

**Case 4: 前缀不存在**

```
insert("apple")
startsWith("bpple") → false（第一个字符就不匹配）
```

---

## 复杂度分析

```
设单词长度为 L

时间复杂度：
- insert：O(L)
- search：O(L)
- startsWith：O(L)

空间复杂度：
- 最坏：O(N × L × 26)，N 是单词数
- 实际：远小于最坏（很多共享前缀）
```

---

## 记忆口诀

```
Trie树是前缀树，边表示字符要记住
从根到节点是前缀，标记结尾是单词
插入查找都容易，逐个字符往下走
search要查结尾符，startsWith只查路径
```
