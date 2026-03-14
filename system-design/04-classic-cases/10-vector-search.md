# 向量检索与索引系统

针对十亿级向量库的毫秒级检索架构设计。

## 1. 核心挑战

```
规模：
- 十亿级向量（10^9）
- 向量维度：128/256/768 维（Float32）
- 单机内存：100M 向量 × 256 维 × 4B = 100GB

性能要求：
- 延迟：P99 < 10ms
- 吞吐：单机 1000+ QPS
- 召回率：Top20 召回率 > 95%
```

## 2. 向量索引算法

### 2.1 算法对比

| 算法 | 原理 | 内存占用 | 查询速度 | 召回率 | 适用场景 |
|------|------|----------|----------|--------|----------|
| 暴力搜索 | 全量计算距离 | 100% | 慢 | 100% | 小规模验证 |
| IVF-PQ | 倒排 + 乘积量化 | 5-10% | 快 | 85-95% | 超大库 |
| HNSW | 图索引 | 200-300% | 最快 | 95-99% | 高召回要求 |
| LSH | 哈希分桶 | 10-20% | 中等 | 70-85% | 快速原型 |

### 2.2 HNSW 详解

```
Hierarchical Navigable Small World

原理：
- 构建多层图结构
- 上层稀疏，快速定位
- 下层稠密，精确搜索

        Layer 2 (稀疏):     o───────────o
                            │
        Layer 1:            o─────┬───o
                            │     │
        Layer 0 (稠密):     o─o─o─o─o─o
                            详细图结构

搜索过程：
1. 从顶层随机节点开始
2. 贪心搜索到最近邻
3. 作为下一层入口
4. 逐层向下直到 Layer 0

参数：
- M：每个节点最大连接数（默认 16）
- efConstruction：构建时搜索范围（默认 100）
- efSearch：查询时搜索范围（默认 50，越大召回率越高）
```

### 2.3 乘积量化 (Product Quantization)

```
原理：将高维向量分解为多个子向量，分别量化

步骤：
1. 向量分解：
   256 维向量 → 8 个子向量（每段 32 维）

2. 码本训练（K-means）：
   每个子空间训练 256 个聚类中心（8 bits）

3. 向量编码：
   原始：256 × 4B = 1024 bytes
   量化：8 × 1B = 8 bytes
   压缩率：128x

4. 非对称距离计算 (ADC)：
   query 与量化后的向量计算近似距离
   d(q, x) ≈ √Σ(d(q_i, c_j)^2)
```

## 3. 十亿级索引架构

### 3.1 分片策略

```
┌─────────────────────────────────────────────────────┐
│                  请求接入层                          │
│              (查询路由 + 结果聚合)                    │
└──────────────────┬──────────────────────────────────┘
                   │
    ┌──────────────┼──────────────┬──────────────┐
    ▼              ▼              ▼              ▼
┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐
│Shard 0  │  │Shard 1  │  │Shard 2  │  │Shard 3  │
│(用户0-25亿)│ │(25-50亿) │ │(50-75亿) │ │(75-100亿)│
└────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘
     │            │            │            │
┌────▼────┐  ┌────▼────┐  ┌────▼────┐  ┌────▼────┐
│Index Node│  │Index Node│  │Index Node│  │Index Node│
│(HNSW)    │  │(HNSW)    │  │(HNSW)    │  │(HNSW)    │
└─────────┘  └─────────┘  └─────────┘  └─────────┘

分片方式：
1. 按用户 ID Hash 分片（用户向量）
2. 按物品类目分片（商品向量）
3. 按向量相似度聚类分片（需动态调整）
```

### 3.2 存储架构

```java
public class VectorIndexShard {
    // HNSW 索引（内存）
    private HnswIndex hnswIndex;

    // 原始向量存储（Memory Mapped File）
    private MappedByteBuffer vectorStore;

    // ID 映射表（向量 ID -> 业务 ID）
    private Long2ObjectMap<String> idMapping;

    public SearchResult search(float[] queryVector, int topK) {
        // 1. HNSW 检索出候选集（扩大 2-3 倍）
        int ef = (int) (topK * 2.5);
        int[] candidateIds = hnswIndex.search(queryVector, ef);

        // 2. 精确计算距离（ rerank ）
        List<SearchResult.Item> results = Arrays.stream(candidateIds)
            .mapToObj(id -> {
                float[] vector = getVectorFromStore(id);
                float distance = cosineSimilarity(queryVector, vector);
                return new SearchResult.Item(idMapping.get(id), distance);
            })
            .sorted(Comparator.comparingDouble(SearchResult.Item::getScore).reversed())
            .limit(topK)
            .collect(Collectors.toList());

        return new SearchResult(results);
    }
}
```

## 4. 增量更新与热更新

### 4.1 双 Buffer 切换

```java
public class HotSwapIndex {
    private volatile VectorIndex activeIndex;
    private VectorIndex backupIndex;

    // 增量更新
    public void incrementUpdate(List<VectorUpdate> updates) {
        // 1. 在备份索引上应用更新
        for (VectorUpdate update : updates) {
            if (update.getType() == UpdateType.ADD) {
                backupIndex.addVector(update.getId(), update.getVector());
            } else if (update.getType() == UpdateType.DELETE) {
                backupIndex.markDeleted(update.getId());
            } else if (update.getType() == UpdateType.UPDATE) {
                backupIndex.updateVector(update.getId(), update.getVector());
            }
        }

        // 2. 重建索引优化图结构
        backupIndex.optimize();

        // 3. 原子切换
        VectorIndex oldIndex = activeIndex;
        activeIndex = backupIndex;
        backupIndex = oldIndex;

        // 4. 清理旧索引
        oldIndex.clear();
    }

    public SearchResult search(float[] vector, int topK) {
        // 始终读取 activeIndex，无锁
        return activeIndex.search(vector, topK);
    }
}
```

### 4.2 实时索引方案

```
方案对比：

┌─────────────────┬─────────────────┬─────────────────┐
│   双 Buffer     │   增量 HNSW     │   日志合并      │
├─────────────────┼─────────────────┼─────────────────┤
│ 定时全量重建    │ 支持实时插入    │ LSM Tree 思想   │
│ 内存占用 2x     │ 图质量逐渐下降  │ 多层索引结构    │
│ 切换时抖动      │ 需要定期重建    │ 查询需合并结果  │
│ 实现简单        │ 适合中频更新    │ 适合高频更新    │
└─────────────────┴─────────────────┴─────────────────┘

推荐：
- 低频更新（日级）：双 Buffer
- 中频更新（小时级）：增量 HNSW
- 高频更新（分钟级）：日志合并（如 Milvus 的 LSM 实现）
```

## 5. 混合检索

### 5.1 向量 + 倒排过滤

```
场景：在特定类目下做向量检索

方案 1：先过滤后检索（Post-filtering）
- 在全库检索 TopK × N
- 再按属性过滤
- 缺点：可能结果不足

方案 2：先检索后过滤（Pre-filtering）
- 构建带标签的向量索引
- 查询时指定过滤条件
- Milvus、Faiss 支持

方案 3：多索引合并
- 按标签分片建立多个索引
- 查询时路由到对应索引
- 适合标签数量有限的场景
```

### 5.2 多路召回融合

```java
public class HybridSearchService {
    // 向量检索
    @Autowired
    private VectorSearchService vectorSearch;

    // 倒排检索（ES/Solr）
    @Autowired
    private InvertedIndexService invertedSearch;

    // 图检索（Neo4j）
    @Autowired
    private GraphSearchService graphSearch;

    public SearchResult hybridSearch(SearchRequest req) {
        // 多路并发召回
        CompletableFuture<SearchResult> vectorFuture =
            CompletableFuture.supplyAsync(() -> vectorSearch.search(req));

        CompletableFuture<SearchResult> textFuture =
            CompletableFuture.supplyAsync(() -> invertedSearch.search(req));

        CompletableFuture<SearchResult> graphFuture =
            CompletableFuture.supplyAsync(() -> graphSearch.search(req));

        // 等待所有结果
        CompletableFuture.allOf(vectorFuture, textFuture, graphFuture).join();

        // 融合排序（RRF 算法）
        return reciprocalRankFusion(
            vectorFuture.join(),
            textFuture.join(),
            graphFuture.join()
        );
    }

    // RRF (Reciprocal Rank Fusion)
    private SearchResult reciprocalRankFusion(SearchResult... results) {
        Map<String, Double> scoreMap = new HashMap<>();
        int k = 60;  // RRF 常数

        for (SearchResult result : results) {
            for (int i = 0; i < result.getItems().size(); i++) {
                String id = result.getItems().get(i).getId();
                double score = 1.0 / (k + i + 1);
                scoreMap.merge(id, score, Double::sum);
            }
        }

        // 按融合分数排序
        return sortByScore(scoreMap);
    }
}
```

## 6. 工程实践

### 6.1 查询优化

```java
public class QueryOptimizer {
    // 自适应 ef 参数
    public int calculateEf(int topK, float recallRequirement) {
        // 召回率要求越高，ef 越大
        if (recallRequirement >= 0.99) {
            return topK * 4;
        } else if (recallRequirement >= 0.95) {
            return topK * 2;
        } else {
            return (int) (topK * 1.5);
        }
    }

    // 批量查询优化
    public List<SearchResult> batchSearch(List<float[]> queries, int topK) {
        // 1. 按查询向量相似度分组
        List<List<float[]>> groups = groupSimilarQueries(queries);

        // 2. 每组共享搜索过程
        List<SearchResult> results = new ArrayList<>();
        for (List<float[]> group : groups) {
            results.addAll(searchWithSharedTraversal(group, topK));
        }

        return results;
    }
}
```

### 6.2 内存优化

```
1. 量化存储：
   - Float32 (4B) → Float16 (2B) → Int8 (1B)
   - PQ 量化到 1/64 - 1/128

2. 内存映射：
   - 向量数据放磁盘，按需加载
   - 热点向量常驻内存

3. 分层存储：
   - L0：本地内存（HNSW 图结构）
   - L1：远程内存（Redis/Memcache）
   - L2：对象存储（S3/OSS）
```

## 7. 面试要点

### Q1: HNSW 索引怎么增量更新？

**答**：
- HNSW 原生支持增量插入，直接添加到图中即可
- 删除操作采用标记删除（软删除），定期重建清理
- 高频更新场景使用双 Buffer 架构：在备份索引上批量更新，完成后原子切换
- 或者使用 LSM-Tree 思路的多层索引结构（如 Milvus 实现）

### Q2: 十亿级向量，单机放不下怎么办？

**答**：
1. 分片：按用户 ID 或物品类目将向量分散到多个节点
2. 量化：使用 PQ 压缩，降低内存占用到 1/64
3. 冷热分离：热点向量常驻内存，冷向量放磁盘按需加载
4. 近似算法：IVF-PQ 配合 GPU 加速

### Q3: 向量检索结果怎么和倒排过滤结合？

**答**：
- Pre-filtering：构建带过滤条件的索引（如 Milvus 的 collection partition）
- Post-filtering：先扩大检索范围（TopK × 3），再过滤
- 多索引：按过滤条件分片建立多个索引，查询时路由
- 混合方案：向量粗排 + 倒排精排

### Q4: 怎么评估向量索引的质量？

**答**：
- 召回率：TopK 中正确的比例（相比暴力搜索）
- 响应延迟：P99 查询时间
- 吞吐：每秒查询数
- 内存占用：每百万向量占用内存
- 构建时间：索引构建耗时
