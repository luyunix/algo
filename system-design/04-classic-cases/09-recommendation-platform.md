# 推荐平台架构设计（工程视角）

针对搜索推荐工程背景的深度解析，涵盖 TPP 平台、特征服务、在线推理等核心模块。

## 1. TPP DAG 执行引擎设计

### 1.1 典型推荐 DAG 结构

```
Input(用户ID, 场景ID)
    │
    ├─► Node1: 用户画像查询 (Redis) ──┐
    │                                 │
    ├─► Node2: 触发召回 (多路召回) ───┤
    │       ├─► I2I 召回              │
    │       ├─► 向量召回              │
    │       └─► 热门召回              │
    │                                 │
    └─► Node3: 物品特征查询 (FeatureService) ──┘
                │
                ▼
            Node4: 粗排 (LightGBM)
                │
                ▼
            Node5: 精排 (DNN/TorchScript)
                │
                ▼
            Node6: 重排 (多样性/MM)
                │
                ▼
            Output(推荐列表)
```

### 1.2 核心抽象

```java
// DAG 节点定义
public interface DAGNode {
    String getName();
    List<String> getDependencies();  // 依赖的节点名
    NodeResult execute(RequestContext ctx);
    long getTimeoutMs();             // 节点级超时
    NodeResult getFallback(RequestContext ctx);  // 降级逻辑
}

// 执行上下文
public class RequestContext {
    private String requestId;
    private User user;
    private Scene scene;
    private Map<String, NodeResult> nodeOutputs;  // 节点输出缓存
    private long startTime;

    public <T> T getDependencyOutput(String nodeName) {
        return (T) nodeOutputs.get(nodeName);
    }
}
```

### 1.3 并行调度策略

#### 依赖分析与拓扑排序

```java
public class DAGScheduler {
    private Map<String, DAGNode> nodeMap;
    private Map<String, List<String>> dependencies;  // node -> 依赖列表
    private Map<String, AtomicInteger> inDegree;     // 入度计数

    public void init(List<DAGNode> nodes) {
        // 构建依赖图
        for (DAGNode node : nodes) {
            nodeMap.put(node.getName(), node);
            inDegree.put(node.getName(), new AtomicInteger(node.getDependencies().size()));

            for (String dep : node.getDependencies()) {
                dependencies.computeIfAbsent(dep, k -> new ArrayList<>()).add(node.getName());
            }
        }
    }

    public Response execute(Request req) {
        RequestContext ctx = new RequestContext(req);
        ConcurrentLinkedQueue<DAGNode> readyQueue = new ConcurrentLinkedQueue<>();
        CountDownLatch completionLatch = new CountDownLatch(nodeMap.size());

        // 找到所有入度为0的节点（根节点）
        for (Map.Entry<String, AtomicInteger> entry : inDegree.entrySet()) {
            if (entry.getValue().get() == 0) {
                readyQueue.add(nodeMap.get(entry.getKey()));
            }
        }

        // 并行执行
        while (completionLatch.getCount() > 0) {
            List<DAGNode> batch = new ArrayList<>();
            while (!readyQueue.isEmpty() && batch.size() < maxConcurrency) {
                batch.add(readyQueue.poll());
            }

            if (batch.isEmpty()) {
                // 死锁检测：有节点未完成，但没有就绪节点
                if (completionLatch.getCount() > 0) {
                    throw new DAGException("Cycle detected or dependency error");
                }
                break;
            }

            // 并行执行一批节点
            List<CompletableFuture<Void>> futures = batch.stream()
                .map(node -> CompletableFuture.runAsync(() -> {
                    executeNode(node, ctx, readyQueue, completionLatch);
                }, executor))
                .collect(Collectors.toList());

            // 等待本批完成（或整体超时）
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(remainingTimeout(ctx), TimeUnit.MILLISECONDS)
                .join();
        }

        return buildResponse(ctx);
    }

    private void executeNode(DAGNode node, RequestContext ctx,
                           ConcurrentLinkedQueue<DAGNode> readyQueue,
                           CountDownLatch latch) {
        try {
            long start = System.currentTimeMillis();
            NodeResult result = executeWithTimeout(node, ctx);
            ctx.putOutput(node.getName(), result);

            // 更新依赖此节点的节点入度
            List<String> dependents = dependencies.get(node.getName());
            if (dependents != null) {
                for (String depNode : dependents) {
                    if (inDegree.get(depNode).decrementAndGet() == 0) {
                        readyQueue.add(nodeMap.get(depNode));
                    }
                }
            }
        } catch (Exception e) {
            ctx.recordError(node.getName(), e);
            // 使用降级结果
            ctx.putOutput(node.getName(), node.getFallback(ctx));
        } finally {
            latch.countDown();
        }
    }
}
```

### 1.4 执行模式对比

| 模式 | 实现 | 适用场景 | 优缺点 |
|------|------|----------|--------|
| 全并行 | 所有无依赖节点同时执行 | 节点间完全独立 | 资源占用大，延迟最低 |
| 分层并行 | 拓扑分层，层内并行 | 有复杂依赖关系 | 平衡资源与延迟 |
| 关键路径优先 | 识别关键路径优先调度 | 有 SLA 要求 | 优化端到端延迟 |
| 动态并行度 | 根据负载调整并发数 | 资源受限环境 | 自适应，复杂度高 |

### 1.5 关键路径优化

```java
// 识别关键路径（耗时最长的依赖链）
public class CriticalPathAnalyzer {
    public List<String> findCriticalPath(Map<String, DAGNode> nodes) {
        // 计算每个节点的最早开始时间和最晚完成时间
        Map<String, Long> earliestStart = new HashMap<>();
        Map<String, Long> latestFinish = new HashMap<>();

        // 前向遍历：计算最早开始时间
        topologicalSort(nodes).forEach(node -> {
            long maxDepFinish = node.getDependencies().stream()
                .mapToLong(dep -> earliestStart.get(dep) + nodes.get(dep).getEstimatedTime())
                .max().orElse(0);
            earliestStart.put(node.getName(), maxDepFinish);
        });

        // 找出耗时最长的路径
        return backtrackLongestPath(nodes, earliestStart);
    }
}

// 运行时关键路径优先调度
public class PriorityScheduler {
    private PriorityBlockingQueue<ScheduledNode> queue = new PriorityBlockingQueue<>(
        Comparator.comparingLong(n -> -n.getCriticalPathWeight())  // 关键路径权重高的优先
    );
}
```

## 2. 超时控制与熔断降级

### 2.1 多级超时策略

```java
public class TimeoutManager {
    // 全局超时
    private static final long GLOBAL_TIMEOUT = 100;  // 100ms SLA

    // 动态超时分配
    public long allocateTimeout(DAGNode node, RequestContext ctx) {
        long elapsed = System.currentTimeMillis() - ctx.getStartTime();
        long remaining = GLOBAL_TIMEOUT - elapsed;

        // 为剩余节点按比例分配超时
        int remainingNodes = countRemainingNodes(ctx);
        long baseTimeout = remaining / remainingNodes;

        // 关键路径节点分配更多时间
        if (isCriticalPath(node)) {
            return (long) (baseTimeout * 1.5);
        }

        return baseTimeout;
    }
}
```

### 2.2 熔断器实现

```java
public class CircuitBreaker {
    private State state = State.CLOSED;
    private AtomicInteger failureCount = new AtomicInteger(0);
    private AtomicInteger successCount = new AtomicInteger(0);
    private long lastFailureTime;

    // 配置参数
    private int failureThreshold = 5;      // 连续失败阈值
    private long timeout = 60000;          // 熔断持续时间
    private int halfOpenMaxCalls = 3;      // 半开状态测试请求数

    public <T> T execute(Callable<T> callable, Callable<T> fallback) throws Exception {
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime > timeout) {
                state = State.HALF_OPEN;
                resetCounts();
            } else {
                return fallback.call();
            }
        }

        try {
            T result = callable.call();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            return fallback.call();
        }
    }

    private void onFailure() {
        failureCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();

        if (failureCount.get() >= failureThreshold) {
            state = State.OPEN;
        }
    }

    private void onSuccess() {
        successCount.incrementAndGet();

        if (state == State.HALF_OPEN) {
            if (successCount.get() >= halfOpenMaxCalls) {
                state = State.CLOSED;
                resetCounts();
            }
        } else {
            failureCount.set(0);
        }
    }
}
```

### 2.3 节点降级策略

```java
public interface FallbackStrategy {
    NodeResult getFallback(RequestContext ctx);
}

// 缓存降级
public class CacheFallback implements FallbackStrategy {
    @Override
    public NodeResult getFallback(RequestContext ctx) {
        // 返回上次成功结果（Staleness 容忍）
        return cache.getLastSuccess(ctx.getRequestId());
    }
}

// 默认值降级
public class DefaultFallback implements FallbackStrategy {
    @Override
    public NodeResult getFallback(RequestContext ctx) {
        // 返回中性值（如平均分、热门物品）
        return NodeResult.builder()
            .score(0.5)
            .features(Collections.emptyMap())
            .build();
    }
}

// 简化模型降级
public class LiteModelFallback implements FallbackStrategy {
    @Override
    public NodeResult getFallback(RequestContext ctx) {
        // 使用轻量级模型（LR 代替 DNN）
        return liteModel.predict(ctx);
    }
}
```

## 3. 特征服务架构

### 3.1 三级缓存架构

```java
@Component
public class FeatureService {
    // L1: 本地缓存（Caffeine）- 微秒级
    private LoadingCache<String, Feature> localCache = Caffeine.newBuilder()
        .maximumSize(100_000)
        .expireAfterWrite(10, TimeUnit.SECONDS)
        .recordStats()
        .build(this::loadFromL2);

    // L2: Redis 集群 - 毫秒级
    @Autowired
    private StringRedisTemplate redisTemplate;

    // L3: 远程特征存储 - 10ms级
    @Autowired
    private FeatureStoreClient featureStore;

    public FeatureVector getFeatures(String entityId, List<String> featureNames) {
        FeatureVector result = new FeatureVector();
        List<String> missingFeatures = new ArrayList<>();

        // L1 查询
        for (String name : featureNames) {
            String key = buildKey(entityId, name);
            Feature feature = localCache.getIfPresent(key);
            if (feature != null) {
                result.put(name, feature);
            } else {
                missingFeatures.add(name);
            }
        }

        // L2 批量查询
        if (!missingFeatures.isEmpty()) {
            List<String> keys = missingFeatures.stream()
                .map(name -> buildKey(entityId, name))
                .collect(Collectors.toList());

            List<String> values = redisTemplate.opsForValue().multiGet(keys);

            for (int i = 0; i < missingFeatures.size(); i++) {
                String value = values.get(i);
                if (value != null) {
                    Feature feature = deserialize(value);
                    result.put(missingFeatures.get(i), feature);
                    localCache.put(keys.get(i), feature);  // 回填 L1
                }
            }
        }

        // L3 查询（剩余未命中）
        List<String> stillMissing = featureNames.stream()
            .filter(name -> !result.contains(name))
            .collect(Collectors.toList());

        if (!stillMissing.isEmpty()) {
            Map<String, Feature> remoteFeatures = featureStore.batchQuery(entityId, stillMissing);
            result.putAll(remoteFeatures);

            // 异步回填 L2
            asyncBackfillToRedis(entityId, remoteFeatures);
        }

        return result;
    }
}
```

### 3.2 请求合并（Request Collapsing）

```java
// 将多个并发请求合并为批量查询
public class BatchingFeatureClient {
    private Disruptor<FeatureRequest> disruptor;
    private Map<String, CompletableFuture<Feature>> pendingRequests;

    public CompletableFuture<Feature> queryAsync(String entityId, String featureName) {
        String key = buildKey(entityId, featureName);

        // 检查是否已有相同请求在处理
        CompletableFuture<Feature> future = pendingRequests.get(key);
        if (future != null) {
            return future;  // 复用已有请求
        }

        // 创建新请求
        future = new CompletableFuture<>();
        pendingRequests.put(key, future);

        // 发送到批量队列
        disruptor.publishEvent((event, sequence) -> {
            event.setEntityId(entityId);
            event.setFeatureName(featureName);
            event.setFuture(future);
        });

        return future;
    }

    // 定时批量处理
    @Scheduled(fixedRate = 5)  // 5ms 批量窗口
    public void batchProcess() {
        List<FeatureRequest> batch = drainPendingRequests();

        // 按实体ID分组批量查询
        Map<String, List<FeatureRequest>> grouped = batch.stream()
            .collect(Collectors.groupingBy(FeatureRequest::getEntityId));

        grouped.forEach((entityId, requests) -> {
            List<String> featureNames = requests.stream()
                .map(FeatureRequest::getFeatureName)
                .collect(Collectors.toList());

            Map<String, Feature> results = featureStore.batchQuery(entityId, featureNames);

            // 回填结果
            requests.forEach(req -> {
                Feature feature = results.get(req.getFeatureName());
                req.getFuture().complete(feature);
                pendingRequests.remove(buildKey(entityId, req.getFeatureName()));
            });
        });
    }
}
```

### 3.3 特征一致性保障

```java
// 在线/离线特征一致性
public class ConsistentFeatureExtractor {
    // 统一特征配置
    private FeatureConfig config;

    // 统一特征计算逻辑（Java 实现）
    public Feature computeFeature(User user, Item item, FeatureType type) {
        switch (type) {
            case USER_ITEM_CROSS:
                return computeCrossFeature(user, item);
            case SEQUENCE:
                return computeSequenceFeature(user.getBehaviorHistory());
            case STATISTICS:
                return computeStatFeature(user.getUserId(), item.getItemId());
            default:
                throw new UnsupportedOperationException();
        }
    }

    // 离线使用相同逻辑（通过 JNI 或 GraalVM Native Image）
    // 确保在线和离线产出一致
}

// 特征版本管理
public class FeatureVersionManager {
    // 支持多版本特征并存
    public FeatureVector getFeatures(String entityId, List<String> features, String version) {
        FeatureSchema schema = schemaRegistry.get(version);

        return features.stream()
            .map(name -> {
                FeatureDefinition def = schema.getDefinition(name);
                return featureStore.query(entityId, name, def.getStorageFormat());
            })
            .collect(FeatureVector.collector());
    }
}
```

## 4. 模型服务化

### 4.1 推理优化

```java
@Service
public class ModelInferenceService {
    // 模型管理
    private Map<String, Model> modelVersions;
    private volatile String activeVersion;

    // TensorRT/ONNX Runtime 封装
    private OrtEnvironment environment;
    private OrtSession session;

    public InferenceResult predict(FeatureVector features) {
        // 特征预处理
        OnnxTensor inputTensor = prepareInput(features);

        // 执行推理
        OrtSession.Result results = session.run(Collections.singletonMap("input", inputTensor));

        // 后处理
        return postProcess(results);
    }

    // 动态批处理（Dynamic Batching）
    public void enableDynamicBatching(int maxBatchSize, int maxLatencyMs) {
        BatchingScheduler scheduler = new BatchingScheduler(
            maxBatchSize,
            maxLatencyMs,
            this::batchPredict  // 批量推理回调
        );
    }

    private List<InferenceResult> batchPredict(List<InferenceRequest> requests) {
        // 合并为批次输入
        OnnxTensor batchInput = combineInputs(requests);

        // 单次批量推理
        OrtSession.Result batchResults = session.run(
            Collections.singletonMap("input", batchInput)
        );

        // 拆分结果
        return splitResults(batchResults, requests.size());
    }
}
```

### 4.2 模型热更新

```java
@Component
public class ModelHotSwapManager {
    private AtomicReference<Model> currentModel = new AtomicReference<>();

    // 蓝绿部署
    public void hotSwap(String newModelPath) {
        // 1. 加载新模型到内存
        Model newModel = loadModel(newModelPath);

        // 2. 预热（Warmup）
        warmup(newModel);

        // 3. 原子切换
        Model oldModel = currentModel.getAndSet(newModel);

        // 4. 优雅关闭旧模型
        if (oldModel != null) {
            oldModel.closeGracefully();
        }
    }

    // 金丝雀发布
    public void canaryDeploy(String newModelPath, double trafficRatio) {
        Model newModel = loadModel(newModelPath);

        // 按流量比例分流
        routingRule.setTrafficSplit(Map.of(
            "v1", 1 - trafficRatio,
            "v2", trafficRatio
        ));

        // 监控指标
        metrics.monitor("model.v2.latency", "model.v2.error_rate");

        // 自动回滚或全量
        if (metrics.isHealthy("model.v2", Duration.ofMinutes(10))) {
            fullDeploy(newModelPath);
        } else {
            rollback("v1");
        }
    }
}
```

## 5. 面试追问与回答

### Q1: DAG 中有环怎么办？

```
回答要点：
1. 构建 DAG 时进行拓扑排序检测环
2. 使用 DFS 遍历，如果访问到已在栈中的节点，说明有环
3. 检测到环后：
   - 配置阶段：报错，拒绝启动
   - 运行时：打破环（选择权重最低的边删除）
4. 实际生产中，DAG 结构通常由平台配置，不允许用户随意配置成环
```

### Q2: 某个节点超时了，下游节点怎么处理？

```
回答要点：
1. 超时节点返回降级结果（默认值/缓存值）
2. 下游节点通过 ctx.getDependencyOutput() 获取结果
3. 如果依赖结果是降级值，下游节点可以：
   - 继续执行（使用默认值）
   - 链式降级（下游也降级）
   - 跳过执行（非关键节点）
4. 在 RequestContext 中标记哪些节点降级了，最终返回时上报监控
```

### Q3: 特征请求怎么做批量聚合？

```
回答要点：
1. 时间窗口聚合：5ms 内的请求合并为一次批量查询
2. 使用 Disruptor 实现高性能队列
3. 按实体 ID 分组，减少 Redis/FeatureStore 的 round trip
4. 异步 Future 返回，不阻塞业务线程
5. 批量大小限制：防止某一批次过大导致延迟飙升
```

### Q4: 怎么保证同一次请求内特征的一致性？

```
回答要点：
1. 版本号机制：所有特征查询带上版本号，确保读取同一版本数据
2. 快照读：在请求开始时创建特征快照（针对可变的实时特征）
3. 写时复制（COW）：特征更新时创建新版本，旧版本继续服务未完成请求
4. 时间戳对齐：所有特征使用请求时间戳，而非查询时间戳
```

## 6. 性能优化清单

| 优化点 | 方法 | 预期收益 |
|--------|------|----------|
| 特征缓存 | 三级缓存 + 布隆过滤器防穿透 | 延迟降低 80% |
| 请求合并 | 5ms 窗口批量查询 | 减少 90% RPC 调用 |
| 并行执行 | 拓扑排序 + 线程池 | 延迟降低 50% |
| 模型推理 | TensorRT + 动态批处理 | 吞吐提升 5-10x |
| 零拷贝 | Netty ByteBuf + Protobuf | 减少 GC 压力 |
| 协程 | Kotlin Coroutine / Project Loom | 减少线程切换 |

---

更多专题：
- [向量检索与索引](./10-vector-search.md)
- [实时特征计算](./11-realtime-features.md)
- [A/B 实验平台](./12-ab-testing.md)
