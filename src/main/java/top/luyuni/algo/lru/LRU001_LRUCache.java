package top.luyuni.algo.lru;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeReporter;
import top.luyuni.algo.oj.core.JudgeResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LRU001_LRUCache {

    // 双向链表节点
    class DLinkedNode {
        int key;
        int value;
        DLinkedNode prev;
        DLinkedNode next;

        DLinkedNode(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    private Map<Integer, DLinkedNode> cache;
    private int capacity;
    private int size;
    private DLinkedNode head; // 伪头部
    private DLinkedNode tail; // 伪尾部

    public LRU001_LRUCache(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        this.cache = new HashMap<>();

        // 初始化伪头部和伪尾部
        this.head = new DLinkedNode(0, 0);
        this.tail = new DLinkedNode(0, 0);
        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {
        if (!cache.containsKey(key)) {
            return -1;
        }

        DLinkedNode node = cache.get(key);
        moveToHead(node); // 访问后移到头部（最近使用）
        return node.value;
    }

    public void put(int key, int value) {
        DLinkedNode node = cache.get(key);

        if (node != null) {
            // 已存在，更新值并移到头部
            node.value = value;
            moveToHead(node);
        } else {
            // 不存在，创建新节点
            DLinkedNode newNode = new DLinkedNode(key, value);
            cache.put(key, newNode);
            addToHead(newNode);
            size++;

            if (size > capacity) {
                // 超出容量，移除最久未使用的（尾部）
                DLinkedNode removed = removeTail();
                cache.remove(removed.key);
                size--;
            }
        }
    }

    // 添加到头部
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

    // 移到头部
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

    // ============ OJ 判题框架 ============

    /**
     * 使用 oj/core 工具进行评测
     */
    public static void main(String[] args) {
        LRU001_LRUCache solution = new LRU001_LRUCache(2);

        // 创建判题引擎，输入是操作序列，输出是结果序列
        JudgeEngine<String[], Object[]> engine = new JudgeEngine<>();

        // 添加测试用例：一系列操作和期望结果
        engine.addTestCase("示例",
                new String[]{"put(1,1)", "put(2,2)", "get(1)", "put(3,3)", "get(2)", "put(4,4)", "get(1)", "get(3)", "get(4)"},
                new Object[]{null, null, 1, null, -1, null, -1, 3, 4},
                "LRU 缓存基本功能");

        // 执行判题
        List<JudgeResult> results = engine.judge(operations -> {
            LRU001_LRUCache cache = new LRU001_LRUCache(2);
            List<Object> results_list = new ArrayList<>();

            for (String op : operations) {
                if (op.startsWith("put")) {
                    String[] parts = op.substring(4, op.length() - 1).split(",");
                    int key = Integer.parseInt(parts[0]);
                    int value = Integer.parseInt(parts[1]);
                    cache.put(key, value);
                    results_list.add(null);
                } else if (op.startsWith("get")) {
                    int key = Integer.parseInt(op.substring(4, op.length() - 1));
                    results_list.add(cache.get(key));
                }
            }

            return results_list.toArray();
        });

        // 打印报告
        JudgeReporter.report("LRU001 - LRU 缓存", results);

        // 统计结果
        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
}
