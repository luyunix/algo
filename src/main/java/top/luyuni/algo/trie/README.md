# Trie 目录 - 前缀树/字典树 (Prefix Tree)

这个目录帮你彻底理解 Trie 树，从「什么是 Trie」到「Trie 的实战应用」。

## 为什么学 Trie？

Trie 树是解决「字符串前缀匹配」问题的神器：
- 自动补全（搜索框提示）
- 拼写检查
- IP 路由最长前缀匹配
- 单词搜索
- 统计前缀出现次数

## Trie 的核心概念

### 什么是 Trie？

```
Trie（发音 /traɪ/，取自 retrieval），又称前缀树或字典树

核心思想：
- 用边表示字符
- 从根到节点的路径表示一个字符串前缀
- 节点标记是否为单词结尾

示例：插入 "cat", "car", "dog"

        root
       /    \
      c      d
      |      |
      a      o
     / \      \
    t   r      g
   /     \
  #       #

# 表示单词结尾
- 根→c→a→t："cat" 是一个单词
- 根→c→a→r："car" 是一个单词
- 根→d→o→g："dog" 是一个单词
- 根→c→a："ca" 是前缀，但不是单词
```

### Trie 的节点结构

```java
class TrieNode {
    // 方式1：数组（只包含小写字母）
    TrieNode[] children = new TrieNode[26];
    boolean isEndOfWord;  // 是否是一个单词的结尾
    
    // 方式2：HashMap（字符范围大）
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfWord;
}
```

### Trie 的核心操作

```
1. insert(word)：插入单词
   - 从根开始，逐个字符向下
   - 没有对应子节点就创建
   - 最后一个字符标记 isEndOfWord = true

2. search(word)：查找单词
   - 从根开始，逐个字符向下
   - 中途找不到返回 false
   - 找到后检查 isEndOfWord

3. startsWith(prefix)：查找前缀
   - 和 search 类似，但不需要检查 isEndOfWord
   - 能走完前缀就返回 true
```

## Trie 的直观理解

```
想象你在查字典：
- 翻开字典，先看第一个字母
- 找到对应章节，看第二个字母
- ...
- 直到找到单词或确定没有

Trie 就像把字典的组织结构直接存成了树：
- 第一层：26个字母（或更多）
- 第二层：每个字母下的26个字母
- ...
```

## Java 中的 Trie 实现

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

## Trie 的经典应用场景

### 场景1：自动补全

```
输入 "ca"，提示 "cat", "car", "card"...

Trie 结构：
        root
         |
         c
         |
         a
        / \
       t   r
       #   \
            d
            #

从 "ca" 节点开始 DFS，找到所有 isEndOfWord=true 的路径
```

### 场景2：拼写检查

```
输入 "catt"，检查是否是单词

search("catt")：
- 根→c→a→t→t
- 最后一个节点不存在或不是单词结尾
- 返回 false，提示拼写错误
```

### 场景3：单词搜索 II（二维网格）

```
给定字母网格，找出所有在字典中的单词

网格：
o a a n
e t a e
i h k r
i f l v

字典："oath", "pea", "eat", "rain"

解法：
1. 把字典所有单词插入 Trie
2. 从网格每个格子开始 DFS
3. DFS 过程中在 Trie 中检查前缀是否存在
4. 如果找到一个单词，加入结果

优化：
- 找到单词后，把 Trie 中对应节点 isEndOfWord 设为 false（去重）
- 如果当前路径在 Trie 中不存在，提前剪枝
```

### 场景4：最长公共前缀

```
问题：求多个字符串的最长公共前缀

解法：
1. 把所有字符串插入 Trie
2. 从根开始遍历，找到第一个分叉点
3. 根到分叉点的路径就是最长公共前缀

示例：["flower", "flow", "flight"]

        root
         |
         f
         |
         l
        / \
       o   i
       |   |
       w   g
       
最长公共前缀："fl"
```

## Trie 的复杂度分析

```
设单词平均长度为 L，字符集大小为 K

时间复杂度：
- insert：O(L)
- search：O(L)
- startsWith：O(L)

空间复杂度：
- 最坏：O(N × L × K)，N 是单词数
- 实际：通常远小于最坏情况（很多共享前缀）

相比哈希表的优势：
- 哈希表 search 也是 O(L)，但无法高效查找前缀
- Trie 可以 O(L) 查找前缀，哈希表需要 O(N × L)
```

## 当前内容

| 文件 | 题目 | 难度 | 核心技巧 |
|------|------|------|----------|
| `TRIE001_ImplementTrie.java` | 实现 Trie 前缀树 | 中等 | Trie 基础 |
| `TRIE002_WordSearchII.java` | 单词搜索 II | 困难 | Trie + DFS |
| `TRIE003_LongestCommonPrefix.java` | 最长公共前缀 | 简单 | Trie 或横向扫描 |
| `TRIE004_AddAndSearchWord.java` | 添加与搜索单词 | 中等 | Trie + 通配符 |
| `TRIE005_MapSumPairs.java` | 键值映射 | 中等 | Trie 节点存值 |
| `TRIE006_ReplaceWords.java` | 单词替换 | 中等 | Trie 前缀替换 |
| `TRIE007_MaximumXOR.java` | 数组中两个数的最大异或值 | 中等 | Trie + 位运算 |

## Trie 学习建议

1. **先理解结构**：边表示字符，节点标记单词结尾
2. **能手写实现**：insert、search、startsWith 三个基础操作
3. **掌握经典题**：单词搜索 II 是 Trie 最经典的应用
4. **注意优化**：找到单词后去重，提前剪枝

## 记忆口诀

```
Trie树是前缀树，边表示字符要记住
从根到节点是前缀，标记结尾是单词
插入查找都容易，逐个字符往下走
前缀匹配最擅长，自动补全靠它啦
```
