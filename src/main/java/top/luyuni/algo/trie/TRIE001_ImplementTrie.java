package top.luyuni.algo.trie;

import top.luyuni.algo.oj.core.JudgeEngine;
import top.luyuni.algo.oj.core.JudgeReporter;
import top.luyuni.algo.oj.core.JudgeResult;

import java.util.ArrayList;
import java.util.List;

public class TRIE001_ImplementTrie {
    
    // Trie 树节点
    class TrieNode {
        TrieNode[] children;
        boolean isEnd;
        
        TrieNode() {
            children = new TrieNode[26]; // 26 个小写字母
            isEnd = false;
        }
    }
    
    private TrieNode root;
    
    /** Initialize your data structure here. */
    public TRIE001_ImplementTrie() {
        root = new TrieNode();
    }
    
    /** Inserts a word into the trie. */
    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            int index = c - 'a';
            if (node.children[index] == null) {
                node.children[index] = new TrieNode();
            }
            node = node.children[index];
        }
        node.isEnd = true;
    }
    
    /** Returns if the word is in the trie. */
    public boolean search(String word) {
        TrieNode node = searchPrefix(word);
        return node != null && node.isEnd;
    }
    
    /** Returns if there is any word in the trie that starts with the given prefix. */
    public boolean startsWith(String prefix) {
        return searchPrefix(prefix) != null;
    }
    
    // 搜索前缀，返回前缀的最后一个节点
    private TrieNode searchPrefix(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            int index = c - 'a';
            if (node.children[index] == null) {
                return null;
            }
            node = node.children[index];
        }
        return node;
    }
    
    // ============ OJ 判题框架 ============
    
    /**
     * 使用 oj/core 工具进行评测
     */
    public static void main(String[] args) {
        TRIE001_ImplementTrie solution = new TRIE001_ImplementTrie();
        
        // 创建判题引擎
        JudgeEngine<String[], Object[]> engine = new JudgeEngine<>();
        
        // 添加测试用例
        engine.addTestCase("示例", 
            new String[]{"insert(apple)", "search(apple)", "search(app)", "startsWith(app)", "insert(app)", "search(app)"},
            new Object[]{null, true, false, true, null, true},
            "Trie 前缀树基本功能");
        
        // 执行判题
        List<JudgeResult> results = engine.judge(operations -> {
            TRIE001_ImplementTrie trie = new TRIE001_ImplementTrie();
            List<Object> resultList = new ArrayList<>();
            
            for (String op : operations) {
                if (op.startsWith("insert")) {
                    String word = op.substring(7, op.length() - 1);
                    trie.insert(word);
                    resultList.add(null);
                } else if (op.startsWith("search")) {
                    String word = op.substring(7, op.length() - 1);
                    resultList.add(trie.search(word));
                } else if (op.startsWith("startsWith")) {
                    String prefix = op.substring(11, op.length() - 1);
                    resultList.add(trie.startsWith(prefix));
                }
            }
            
            return resultList.toArray();
        });
        
        // 打印报告
        JudgeReporter.report("TRIE001 - 实现 Trie", results);
        
        // 统计结果
        boolean allPassed = results.stream().allMatch(JudgeResult::isAccepted);
        System.exit(allPassed ? 0 : 1);
    }
}
