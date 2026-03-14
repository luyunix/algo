# 实时特征计算

Flink 实时特征工程：Exactly-Once、双流 Join、状态管理。

## 1. 实时特征场景

```
场景：用户点击后，多久能在推荐中生效？

实时特征类型：
├── 计数类：用户近 5 分钟点击次数、页面浏览 PV
├── 序列类：用户最近点击的 10 个商品
├── 统计类：商品近 1 小时点击率
├── 聚合类：用户实时兴趣标签（滑动窗口 TopK）
└── 衍生类：点击率 = 点击数 / 曝光数

延迟要求：
- 近实时：秒级（Kappa 架构）
- 准实时：分钟级（Lambda 架构）
```

## 2. 架构设计

### 2.1 Lambda 架构

```
         ┌─────────────────────────────────────┐
         │           Kafka 数据源               │
         │    (点击流、曝光流、收藏流)          │
         └──────────┬──────────────────────────┘
                    │
        ┌───────────┴───────────┐
        ▼                       ▼
┌───────────────┐       ┌───────────────┐
│   实时流      │       │   离线批处理   │
│  (Flink)      │       │  (Spark)      │
│               │       │               │
│ 实时特征计算   │       │ T+1 特征校正   │
│ 秒级延迟       │       │ 保证最终一致   │
└───────┬───────┘       └───────┬───────┘
        │                       │
        ▼                       ▼
┌───────────────┐       ┌───────────────┐
│  Redis/HBase  │       │   Hive/HDFS   │
│  在线查询      │       │ 离线训练       │
└───────────────┘       └───────────────┘
        │                       │
        └───────────┬───────────┘
                    ▼
            ┌───────────────┐
            │   模型训练    │
            │  在线服务      │
            └───────────────┘
```

### 2.2 Kappa 架构（纯实时）

```
Kafka ──► Flink ──► Redis/HBase

特点：
- 只有流处理层
- 通过重放历史数据完成离线计算
- 架构简化，但成本较高
```

## 3. Flink 状态管理

### 3.1 状态类型

```java
// KeyedState：每个 Key 有自己的状态
public class UserClickCountFunction extends KeyedProcessFunction<String, ClickEvent, Feature> {
    private ValueState<Integer> clickCountState;
    private ListState<String> clickSequenceState;

    @Override
    public void open(Configuration parameters) {
        // 声明状态
        clickCountState = getRuntimeContext().getState(
            new ValueStateDescriptor<>("clickCount", Types.INT)
        );
        clickSequenceState = getRuntimeContext().getListState(
            new ListStateDescriptor<>("clickSequence", Types.STRING)
        );
    }

    @Override
    public void processElement(ClickEvent event, Context ctx, Collector<Feature> out) {
        // 更新计数
        Integer count = clickCountState.value();
        if (count == null) count = 0;
        clickCountState.update(count + 1);

        // 更新序列（保留最近 10 个）
        List<String> sequence = new ArrayList<>();
        clickSequenceState.get().forEach(sequence::add);
        sequence.add(event.getItemId());
        if (sequence.size() > 10) {
            sequence.remove(0);
        }
        clickSequenceState.update(sequence);

        // 输出特征
        out.collect(new Feature(event.getUserId(), "click_count", count + 1));
    }
}
```

### 3.2 状态后端

| 后端 | 存储位置 | 适用场景 | 限制 |
|------|----------|----------|------|
| MemoryStateBackend | JVM Heap | 本地测试 | 状态小，恢复快照 |
| FsStateBackend | 本地磁盘 + 异步快照 | 大状态、长窗口 | 受磁盘限制 |
| RocksDBStateBackend | RocksDB（本地磁盘）| 超大状态 | 读写性能较低 |

```java
// RocksDB 配置
RocksDBStateBackend rocksDbBackend = new RocksDBStateBackend(
    "hdfs://checkpoints",
    true  // 增量快照
);

// 配置状态 TTL
StateTtlConfig ttlConfig = StateTtlConfig
    .newBuilder(Time.hours(24))
    .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
    .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
    .cleanupIncrementally(10, true)
    .build();
```

## 4. Exactly-Once 保证

### 4.1 Checkpoint 机制

```
Checkpoint 流程：

1. JobManager 发送 Checkpoint Barrier
   ├─► Source 收到 Barrier，快照偏移量
   ├─► Barrier 随数据流向下游
   ├─► 每个算子收到 Barrier 后快照状态
   └─► Sink 收到 Barrier 后提交事务

2. 两阶段提交（2PC）
   - 预提交：写入数据但不提交
   - Checkpoint 完成时统一提交
```

### 4.2 Kafka Exactly-Once

```java
// Flink Kafka Source：从上次 Checkpoint 的 Offset 恢复
FlinkKafkaConsumer<ClickEvent> source = new FlinkKafkaConsumer<>(
    "click-topic",
    new ClickEventDeserializationSchema(),
    properties
);
source.setStartFromGroupOffsets();
source.setCommitOffsetsOnCheckpoints(true);  // Checkpoint 时提交 Offset

// Flink Kafka Sink：事务写入
FlinkKafkaProducer<Feature> sink = new FlinkKafkaProducer<>(
    "feature-topic",
    new FeatureSerializer(),
    properties,
    FlinkKafkaProducer.Semantic.EXACTLY_ONCE  // 精确一次语义
);
```

### 4.3 Redis Sink 的幂等性

```java
// 方案 1：覆盖写入（天然幂等）
public class RedisSink extends RichSinkFunction<Feature> {
    private Jedis jedis;

    @Override
    public void invoke(Feature value, Context context) {
        // 使用 HSET，相同 Key 覆盖
        jedis.hset(
            "user:" + value.getUserId(),
            value.getFeatureName(),
            value.getFeatureValue()
        );
    }
}

// 方案 2：事务 + Checkpoint
public class RedisExactlyOnceSink extends TwoPhaseCommitSinkFunction<Feature, RedisTransaction, Void> {
    @Override
    protected void invoke(RedisTransaction transaction, Feature value, Context context) {
        transaction.hset(
            "user:" + value.getUserId(),
            value.getFeatureName(),
            value.getFeatureValue()
        );
    }

    @Override
    protected void preCommit(RedisTransaction transaction) {
        // 预提交：执行但不关闭事务
        transaction.exec();
    }

    @Override
    protected void commit(RedisTransaction transaction) {
        // 正式提交：关闭事务
        transaction.close();
    }

    @Override
    protected void abort(RedisTransaction transaction) {
        // 回滚
        transaction.discard();
    }
}
```

## 5. 双流 Join

### 5.1 窗口 Join

```java
// 点击流 Join 曝光流（Interval Join）
DataStream<Click> clicks = ...;
DataStream<Impression> impressions = ...;

clicks
    .keyBy(Click::getItemId)
    .intervalJoin(impressions.keyBy(Impression::getItemId))
    .between(Time.milliseconds(-5), Time.milliseconds(5))  // 时间窗口
    .process(new ProcessJoinFunction<Click, Impression, ClickWithImpression>() {
        @Override
        public void processElement(Click click, Impression impression, Context ctx, Collector<ClickWithImpression> out) {
            // 计算点击率特征
            out.collect(new ClickWithImpression(
                click.getUserId(),
                click.getItemId(),
                click.getTimestamp(),
                impression.getPosition()
            ));
        }
    });
```

### 5.2 维表 Join（异步）

```java
// 点击流 Join 用户画像（HBase 维表）
public class AsyncUserInfoJoin extends AsyncFunction<Click, EnrichedClick> {
    private transient AsyncHBaseClient hbaseClient;

    @Override
    public void open(Configuration parameters) {
        hbaseClient = new AsyncHBaseClient(...);
    }

    @Override
    public void asyncInvoke(Click click, ResultFuture<EnrichedClick> resultFuture) {
        // 异步查询 HBase
        CompletableFuture<UserProfile> profileFuture = hbaseClient.asyncGet(
            "user_profile",
            click.getUserId()
        );

        profileFuture.thenAccept(profile -> {
            resultFuture.complete(Collections.singletonList(
                new EnrichedClick(click, profile)
            ));
        });
    }
}

// 使用
DataStream<EnrichedClick> enriched = AsyncDataStream.unorderedWait(
    clickStream,
    new AsyncUserInfoJoin(),
    1000,  // 超时 1s
    TimeUnit.MILLISECONDS,
    100    // 并发数
);
```

## 6. 实时特征一致性

### 6.1 问题：实时 vs 离线特征不一致

```
原因：
1. 计算逻辑不同（Java vs SQL）
2. 时间窗口不同（事件时间 vs 处理时间）
3. 数据来源不同（Kafka vs Hive）
4. 状态容错（Flink 重启导致重复/丢失）
```

### 6.2 解决方案

```java
// 方案 1：统一计算逻辑（GraalVM）
public class UnifiedFeatureCalculator {
    // 在线和离线使用同一份 Java 代码
    public static double calculateCTR(long clicks, long impressions) {
        if (impressions == 0) return 0.0;
        return (double) clicks / impressions;
    }
}

// 方案 2：离线校正实时特征
public class FeatureCorrectionJob {
    // T+1 用 Spark 重新计算，覆盖实时特征
    public void correctFeatures(String date) {
        Dataset<Row> realtimeFeatures = spark.read()
            .format("hbase")
            .option("table", "realtime_features")
            .load();

        Dataset<Row> offlineFeatures = spark.sql(
            "SELECT user_id, feature_name, feature_value " +
            "FROM offline_feature_table " +
            "WHERE dt = '" + date + "'"
        );

        // 合并：离线覆盖实时
        Dataset<Row> corrected = realtimeFeatures
            .join(offlineFeatures, "user_id")
            .select(offlineFeatures.col("*"));

        corrected.write()
            .format("hbase")
            .option("table", "corrected_features")
            .save();
    }
}

// 方案 3：特征快照（请求级一致）
public class FeatureSnapshotService {
    // 在请求开始时创建特征快照
    public FeatureSnapshot createSnapshot(String userId, long timestamp) {
        return FeatureSnapshot.builder()
            .userId(userId)
            .timestamp(timestamp)
            .features(featureStore.queryAsOf(userId, timestamp))
            .build();
    }
}
```

## 7. 性能优化

### 7.1 数据倾斜处理

```java
// 方案 1：加盐 + 局部聚合
public class SkewedClickProcess extends ProcessFunction<Click, Click> {
    private static final int SALT_COUNT = 100;

    @Override
    public void processElement(Click click, Context ctx, Collector<Click> out) {
        // 为热点 Key 加盐
        if (isHotKey(click.getItemId())) {
            int salt = ThreadLocalRandom.current().nextInt(SALT_COUNT);
            click.setItemId(click.getItemId() + "_" + salt);
        }
        out.collect(click);
    }
}

// 方案 2：两阶段聚合
DataStream<Feature> result = clicks
    // 第一阶段：局部聚合（加盐 Key）
    .map(new AddSaltMapper())
    .keyBy(Click::getSaltedItemId)
    .window(TumblingProcessingTimeWindows.of(Time.minutes(1)))
    .aggregate(new PartialAggregateFunction())
    // 第二阶段：全局聚合（去盐）
    .keyBy(Feature::getItemId)
    .process(new FinalAggregateFunction());
```

### 7.2 状态清理

```java
// 使用 TTL 自动清理过期状态
StateTtlConfig ttlConfig = StateTtlConfig
    .newBuilder(Time.hours(24))
    .cleanupIncrementally(5, true)  // 增量清理
    .build();

// 或手动清理（定时器）
@Override
public void onTimer(long timestamp, OnTimerContext ctx, Collector<Feature> out) {
    // 清理过期状态
    if (timestamp % CLEANUP_INTERVAL == 0) {
        cleanExpiredState();
    }
}
```

## 8. 面试要点

### Q1: Flink 做实时特征，怎么保证 Exactly-Once？

**答**：
1. Checkpoint 机制：定期快照状态和 Offset
2. Source：Kafka Consumer 从 Checkpoint 的 Offset 恢复
3. Sink：两阶段提交（2PC），预提交数据，Checkpoint 完成后再正式提交
4. 幂等写入：Redis HSET、HBase Put 天然幂等
5. 故障恢复时，从最近一次成功的 Checkpoint 重启

### Q2: 用户点击后，多久能在推荐中生效？

**答**：
- 纯实时：秒级（Flink 处理延迟 + Redis 写入延迟）
- 一般场景：5-30 秒（考虑聚合窗口和检查点间隔）
- 优化手段：
  - 减小 Checkpoint 间隔（权衡性能）
  - 使用 Async I/O 减少 Sink 延迟
  - 热点特征本地缓存

### Q3: 实时特征和离线特征不一致怎么解决？

**答**：
1. 统一计算逻辑：在线（Flink Java）和离线（Spark Java）共用一套代码
2. 离线校正：T+1 用 Spark 重新计算，覆盖实时特征
3. 特征快照：请求时记录特征版本号，训练时读取对应版本
4. 容忍不一致：推荐系统对轻微不一致有容忍度，只要趋势一致即可

### Q4: 流式 Join 怎么做？点击流 Join 曝光流？

**答**：
1. Interval Join：指定时间窗口（如曝光后 5 分钟内点击）
2. 使用 RocksDB 状态后端存储窗口数据
3. 设置 State TTL 清理过期数据
4. 数据倾斜时，热点 Key 加盐处理
5. 或使用异步维表 Join（HBase/Redis），配合缓存减少查询
