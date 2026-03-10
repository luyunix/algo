package top.luyuni.algo.lfu;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeReporter;
import top.luyuni.algo.oj.core.JudgeResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LFU001_LFUCache {
    
    // 双向链表节点
    class DLinkedNode {
        int key;
        int value;
        int count; // 使用频率
        DLinkedNode prev;
        DLinkedNode next;
        
        DLinkedNode(int key, int value) {
            this.key = key;
            this.value = value;
            this.count = 1;
        }
    }
    
    // 双向链表
    class DoublyLinkedList {
        DLinkedNode head;
        DLinkedNode tail;
        int size;
        
        DoublyLinkedList() {
            head = new DLinkedNode(0, 0);
            tail = new DLinkedNode(0, 0);
            head.next = tail;
            tail.prev = head;
            size = 0;
        }
        
        void addFirst(DLinkedNode node) {
            node.prev = head;
            node.next = head.next;
            head.next.prev = node;
            head.next = node;
            size++;
        }
        
        void remove(DLinkedNode node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            size--;
        }
        
        DLinkedNode removeLast() {
            if (size == 0) return null;
            DLinkedNode node = tail.prev;
            remove(node);
            return node;
        }
        
        boolean isEmpty() {
            return size == 0;
        }
    }
    
    private Map<Integer, DLinkedNode> keyToNode;
    private Map<Integer, DoublyLinkedList> countToList;
    private int capacity;
    private int size;
    private int minCount;
    
    public LFU001_LFUCache(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        this.minCount = 0;
        this.keyToNode = new HashMap<>();
        this.countToList = new HashMap<>();
    }
    
    public int get(int key) {
        if (!keyToNode.containsKey(key)) {
            return -1;
        }
        
        DLinkedNode node = keyToNode.get(key);
        increaseCount(node);
        return node.value;
    }
    
    public void put(int key, int value) {
        if (capacity == 0) return;
        
        if (keyToNode.containsKey(key)) {
            // 已存在，更新值和频率
            DLinkedNode node = keyToNode.get(key);
            node.value = value;
            increaseCount(node);
        } else {
            // 新节点
            if (size >= capacity) {
                // 移除最不常用的（最久未使用的）
                DoublyLinkedList list = countToList.get(minCount);
                DLinkedNode removed = list.removeLast();
                keyToNode.remove(removed.key);
                size--;
            }
            
            DLinkedNode newNode = new DLinkedNode(key, value);
            keyToNode.put(key, newNode);
            addToCountList(newNode);
            size++;
            minCount = 1;
        }
    }
    
    // 增加节点的使用频率
    private void increaseCount(DLinkedNode node) {
        int oldCount = node.count;
        
        // 从旧频率的链表中移除
        DoublyLinkedList oldList = countToList.get(oldCount);
        oldList.remove(node);
        
        if (oldList.isEmpty() && oldCount == minCount) {
            minCount++;
        }
        
        // 加入新频率的链表
        node.count++;
        addToCountList(node);
    }
    
    // 添加到对应频率的链表
    private void addToCountList(DLinkedNode node) {
        int count = node.count;
        countToList.putIfAbsent(count, new DoublyLinkedList());
        countToList.get(count).addFirst(node);
    }
    
    // ============ OJ 判题框架 ============
    
    /**
     * 使用 oj/core 工具进行评测
     */
    public static void main(String[] args) {
        LFU001_LFUCache solution = new LFU001_LFUCache(2);
        
        // 创建判题引擎
        JudgeEngine<String[], Object[]> engine = new JudgeEngine<>();
        
        // 添加测试用例
        engine.addTestCase("示例", 
            new String[]{"put(1,1)", "put(2,2)", "get(1)", "put(3,3)", "get(2)", "get(3)", "put(4,4)", "get(1)", "get(3)", "get(4)"},
            new Object[]{null, null, 1, null, -1, 3, null, -1, 3, 4},
            "LFU 缓存基本功能");
        
        // 执行判题
        List<JudgeResult> results = engine.judge(operations -> {
            LFU001_LFUCache cache = new LFU001_LFUCache(2);
            List<Object> resultList = new ArrayList<>();
            
            for (String op : operations) {
                if (op.startsWith("put")) {
                    String[] parts = op.substring(4, op.length() - 1).split(",");
                    int key = Integer.parseInt(parts[0]);
                    int value = Integer.parseInt(parts[1]);
                    cache.put(key, value);
                    resultList.add(null);
                } else if (op.startsWith("get")) {
                    int key = Integer.parseInt(op.substring(4, op.length() - 1));
                    resultList.add(cache.get(key));
                }
            }
            
            return resultList.toArray();
        });
        
        // 打印报告
        JudgeReporter.report("LFU001 - LFU 缓存", results);
        
        // 统计结果
        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
}
