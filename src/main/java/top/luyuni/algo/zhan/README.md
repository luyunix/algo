# ZHAN 目录 - 栈专题 (Stack)

这个目录帮你彻底理解栈，从「后进先出」到「栈的算法魔法」。

## 为什么学栈？

栈是解决「对称性问题」和「延迟处理」的神器：
- 括号匹配（编译器基础）
- 表达式求值（计算器实现）
- 浏览器前进后退
- 递归的非递归实现
- 单调栈求下一个更大元素

## 栈的核心概念

### 什么是栈？

```
栈（Stack）是一种后进先出（LIFO）的数据结构：

想象一叠盘子：
- 放盘子：只能放在最上面
- 取盘子：只能取最上面的
- 最后放的盘子，最先被取走

核心操作：
- push：入栈，O(1)
- pop：出栈，O(1)
- peek/top：看栈顶，O(1)
- isEmpty：判空，O(1)
```

### 栈的直观理解

```
初始：空栈 []

push(1): [1]
push(2): [1, 2]  ← 2在栈顶
push(3): [1, 2, 3]

pop(): 返回3，栈变成 [1, 2]
pop(): 返回2，栈变成 [1]

peek(): 返回1，栈不变 [1]
```

### 栈的存储实现

```java
// 数组实现（固定容量）
class ArrayStack {
    private int[] stack;
    private int top;
    
    public ArrayStack(int capacity) {
        stack = new int[capacity];
        top = -1;
    }
    
    public void push(int x) {
        stack[++top] = x;
    }
    
    public int pop() {
        return stack[top--];
    }
    
    public int peek() {
        return stack[top];
    }
}

// 链表实现（动态容量）
class ListStack {
    private ListNode head;
    
    public void push(int x) {
        ListNode node = new ListNode(x);
        node.next = head;
        head = node;
    }
    
    public int pop() {
        int val = head.val;
        head = head.next;
        return val;
    }
}
```

## Java中的栈实现

```java
// 方式1：Stack类（不推荐，遗留类）
Stack<Integer> stack = new Stack<>();

// 方式2：Deque接口（推荐）
Deque<Integer> stack = new ArrayDeque<>();

// 常用操作
stack.push(1);      // 入栈
stack.pop();        // 出栈
stack.peek();       // 看栈顶
stack.isEmpty();    // 判空
stack.size();       // 栈大小
```

## 栈的经典应用场景

### 场景1：括号匹配

```
问题：判断字符串中的括号是否匹配
示例："{[()]}" 匹配，"{[(])}" 不匹配

思路：
1. 遇到左括号，入栈
2. 遇到右括号，出栈匹配
3. 如果不匹配或栈空，返回false
4. 最后栈必须为空

过程：
"{[()]}"

{: push {，栈: [{]
[: push [，栈: [{, []
(: push (，栈: [{, [, (]
): pop (，匹配，栈: [{, []]
]: pop [，匹配，栈: [{]
}: pop {，匹配，栈: []

栈空，匹配成功 ✓
```

### 场景2：表达式求值

```
问题：计算 "3 + 5 * 2 - 4 / 2"

思路（双栈法）：
- 数字栈：存操作数
- 运算符栈：存运算符
- 遇到数字入数字栈
- 遇到运算符，如果优先级<=栈顶，先计算栈顶的

过程：
数字栈: []    运算符栈: []

3: 数字栈[3]
+: 运算符栈[+]
5: 数字栈[3, 5]
*: 优先级>+，运算符栈[+, *]
2: 数字栈[3, 5, 2]
-: 优先级<*，先算5*2=10
   数字栈[3, 10]，运算符栈[+]
   优先级=+，先算3+10=13
   数字栈[13]，运算符栈[-]
4: 数字栈[13, 4]
/: 优先级>-，运算符栈[-, /]
2: 数字栈[13, 4, 2]

结束，计算剩余：4/2=2，然后13-2=11
```

### 场景3：单调栈（重点！）

```
问题：求数组中每个元素的下一个更大元素
示例：[2, 1, 2, 4, 3]
结果：[4, 2, 4, -1, -1]

单调栈思路：
- 维护一个单调递减栈
- 遍历数组，如果当前元素>栈顶，栈顶元素的下一个更大元素就是当前元素
- 否则当前元素入栈

过程：
栈: []，遍历 2
2: 栈空，push 2，栈: [2]

1: 1 < 2，push 1，栈: [2, 1]

2: 2 > 1，pop 1，1的下一个更大是2
   2 == 2，push 2，栈: [2, 2]

4: 4 > 2，pop 2，2的下一个更大是4
   4 > 2，pop 2，2的下一个更大是4
   栈空，push 4，栈: [4]

3: 3 < 4，push 3，栈: [4, 3]

结束，栈中元素都没有下一个更大的，填-1
```

### 场景4：递归转非递归

```
递归本质就是用栈实现的！

递归求阶乘：
int factorial(int n) {
    if (n == 1) return 1;
    return n * factorial(n - 1);
}

栈实现：
int factorial(int n) {
    Stack<Integer> stack = new Stack<>();
    while (n > 1) {
        stack.push(n);
        n--;
    }
    
    int result = 1;
    while (!stack.isEmpty()) {
        result *= stack.pop();
    }
    return result;
}
```

## 当前内容

| 文件 | 题目 | 难度 | 核心技巧 |
|------|------|------|----------|
| `ZHAN001_ValidParentheses.java` | 有效的括号 | 入门 | 栈匹配 |
| `ZHAN002_MinStack.java` | 最小栈 | 入门 | 辅助栈 |
| `ZHAN003_DailyTemperatures.java` | 每日温度 | 中等 | 单调栈 |
| `ZHAN004_NextGreaterElement.java` | 下一个更大元素I | 中等 | 单调栈 |
| `ZHAN005_LargestRectangle.java` | 柱状图中最大的矩形 | 困难 | 单调栈 |
| `ZHAN006_EvaluateReversePolish.java` | 逆波兰表达式求值 | 中等 | 栈计算 |
| `ZHAN007_ImplementQueueUsingStacks.java` | 用栈实现队列 | 入门 | 双栈（输入+输出） |
| `ZHAN008_DecodeString.java` | 字符串解码 | 中等 | 双栈 |
| `ZHAN009_SimplifyPath.java` | 简化路径 | 中等 | 栈处理 |

## 栈学习建议

1. **先掌握基础**：push/pop/peek操作
2. **再理解应用**：括号匹配、表达式求值
3. **重点攻单调栈**：面试高频，求下一个更大/更小元素
4. **理解递归和栈的关系**：递归本质就是栈

## 记忆口诀

```
栈是后进先出，push pop 要记熟
括号匹配用栈存，左进右出看配对
单调栈里存递减，遇到大的就弹出
下一个更大元素，单调栈里找答案
```
