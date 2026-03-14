# A/B 实验平台

推荐系统的实验分层、分流算法、指标计算。

## 1. 核心概念

```
为什么需要分层实验？

场景：
- 算法同学要做召回实验
- 产品同学要做 UI 实验
- 运营同学要做运营位实验

问题：如果都用同一批流量，会互相干扰

解决方案：实验分层
- 每层独立分流
- 层与层之间流量正交
- 同一层内实验互斥
```

## 2. 实验分层架构

### 2.1 流量分层模型

```
总流量 (100%)
    │
    ├─► 层1：召回层（多路召回策略）
    │       ├─► 实验1-1：I2I 召回权重调整
    │       ├─► 实验1-2：新增向量召回路
    │       └─► 基准桶：默认策略
    │
    ├─► 层2：粗排层（粗排模型）
    │       ├─► 实验2-1：LightGBM 特征调整
    │       └─► 基准桶
    │
    ├─► 层3：精排层（深度模型）
    │       ├─► 实验3-1：DIN 模型
    │       ├─► 实验3-2：Transformer 模型
    │       └─► 基准桶
    │
    └─► 层4：重排层（多样性策略）
            ├─► 实验4-1：MMR 算法
            └─► 基准桶

特点：
- 用户在各层独立分桶
- 层间实验正交（可以同时生效）
- 层内实验互斥（只能命中一个）
```

### 2.2 流量正交性

```
正交性保证：

层1 使用 hash(user_id + layer1_salt) % 100
层2 使用 hash(user_id + layer2_salt) % 100

因为 salt 不同，即使层1 在实验组的用户，
在层2 也是均匀分布在所有桶中

数学证明：
hash(user_id + salt1) 和 hash(user_id + salt2) 相互独立
=> P(层1=A ∩ 层2=B) = P(层1=A) × P(层2=B)
```

## 3. 分流算法

### 3.1 基础 Hash 分流

```java
@Service
public class ExperimentService {

    public ExperimentResult route(Request request) {
        String userId = request.getUserId();
        List<ExperimentLayer> layers = getActiveLayers();

        Map<String, String> experimentMap = new HashMap<>();

        for (ExperimentLayer layer : layers) {
            // 计算用户在该层的分桶
            int bucket = hash(userId + layer.getSalt()) % 100;

            // 找到命中的实验
            Experiment hitExperiment = null;
            for (Experiment exp : layer.getExperiments()) {
                if (bucket >= exp.getStartBucket() && bucket < exp.getEndBucket()) {
                    hitExperiment = exp;
                    break;
                }
            }

            if (hitExperiment != null) {
                experimentMap.put(layer.getName(), hitExperiment.getName());
            }
        }

        return new ExperimentResult(experimentMap);
    }

    private int hash(String key) {
        // 使用 MurmurHash，分布更均匀
        return Hashing.murmur3_32().hashString(key, StandardCharsets.UTF_8).asInt();
    }
}
```

### 3.2 一致性 Hash 分流

```java
// 保证实验扩量时，已有用户仍在同一实验组
public class ConsistentHashRouter {

    public String route(String userId, List<Experiment> experiments) {
        // 构建虚拟节点环
        TreeMap<Integer, String> ring = new TreeMap<>();
        int virtualNodes = 150;  // 每个实验 150 个虚拟节点

        for (Experiment exp : experiments) {
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(exp.getName() + i);
                ring.put(hash, exp.getName());
            }
        }

        // 找到顺时针第一个节点
        int userHash = hash(userId);
        Map.Entry<Integer, String> entry = ring.ceilingEntry(userHash);
        if (entry == null) {
            entry = ring.firstEntry();
        }

        return entry.getValue();
    }
}
```

### 3.3 条件分流

```java
// 支持更复杂的分流条件
public class ConditionalRouter {

    public boolean match(Request request, DiversionRule rule) {
        switch (rule.getType()) {
            case USER_ID:
                return matchUserId(request.getUserId(), rule);

            case CITY:
                return rule.getValues().contains(request.getCity());

            case DEVICE:
                return rule.getValues().contains(request.getDeviceType());

            case NEW_USER:
                return request.isNewUser() == rule.isNewUser();

            case WHITELIST:
                return rule.getWhitelist().contains(request.getUserId());

            case COMBINE:
                // 组合条件
                return rule.getConditions().stream()
                    .allMatch(c -> match(request, c));

            default:
                return false;
        }
    }
}
```

## 4. 实验配置管理

### 4.1 实验配置模型

```yaml
# 实验配置示例
experiment:
  name: "din_model_test"
  layer: "ranking"
  status: "running"  # running / paused / stopped

  # 流量分配
  traffic:
    start_time: "2024-01-01 00:00:00"
    end_time: "2024-01-31 23:59:59"
    buckets:
      - name: "control"
        range: [0, 50]  # 基准桶 50%
        config:
          model: "dnn_v1"
      - name: "treatment"
        range: [50, 100]  # 实验桶 50%
        config:
          model: "din_v2"

  # 过滤条件
  filters:
    - type: "city"
      values: ["北京", "上海", "广州", "深圳"]
    - type: "new_user"
      value: false

  # 监控指标
  metrics:
    primary: "ctr"  # 核心指标
    secondary:
      - "cvr"
      - "duration"
      - "dau"
    guardrail:
      - "p99_latency"  # 守卫指标，不能恶化
```

### 4.2 动态配置推送

```java
@Service
public class ExperimentConfigManager {
    // 本地缓存
    private volatile ExperimentConfig currentConfig;

    // 配置中心（Nacos / Apollo）
    @Autowired
    private ConfigService configService;

    @PostConstruct
    public void init() {
        // 监听配置变更
        configService.addListener("experiment", new ConfigListener() {
            @Override
            public void onChange(String config) {
                ExperimentConfig newConfig = parseConfig(config);
                // 校验配置合法性
                if (validateConfig(newConfig)) {
                    currentConfig = newConfig;
                    log.info("Experiment config updated");
                }
            }
        });
    }

    public ExperimentConfig getConfig() {
        return currentConfig;
    }
}
```

## 5. 指标计算

### 5.1 实时指标 vs 离线指标

```
实时指标（秒级延迟）：
- 用于监控实验健康度
- 快速发现问题
- 不用于最终决策

离线指标（T+1）：
- 用于实验结论
- 更准确（去重、归因）
- 复杂指标（留存、LTV）
```

### 5.2 实时指标计算（Flink）

```java
public class ExperimentMetricJob {

    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 读取曝光流
        DataStream<Impression> impressions = env
            .addSource(new KafkaSource<>("impression-topic"));

        // 读取点击流
        DataStream<Click> clicks = env
            .addSource(new KafkaSource<>("click-topic"));

        // 关联实验标签
        DataStream<TaggedEvent> taggedImpressions = impressions
            .map(event -> {
                String experiment = experimentService.getExperiment(event.getUserId());
                return new TaggedEvent(event, experiment);
            });

        // 计算 CTR（滑动窗口）
        taggedImpressions
            .keyBy(TaggedEvent::getExperiment)
            .window(SlidingEventTimeWindows.of(Time.minutes(5), Time.minutes(1)))
            .aggregate(new CTRAggregateFunction())
            .addSink(new PrometheusSink());
    }
}

// CTR 聚合函数
public class CTRAggregateFunction implements AggregateFunction<Event, CTRAcc, CTRResult> {

    @Override
    public CTRAcc createAccumulator() {
        return new CTRAcc(0, 0);
    }

    @Override
    public CTRAcc add(Event event, CTRAcc acc) {
        if (event.getType().equals("impression")) {
            acc.impressionCount++;
        } else if (event.getType().equals("click")) {
            acc.clickCount++;
        }
        return acc;
    }

    @Override
    public CTRResult getResult(CTRAcc acc) {
        double ctr = acc.impressionCount == 0 ? 0 :
            (double) acc.clickCount / acc.impressionCount;
        return new CTRResult(ctr, acc.clickCount, acc.impressionCount);
    }
}
```

### 5.3 离线指标计算（Spark）

```python
# T+1 指标计算
from pyspark.sql import SparkSession
from scipy import stats

spark = SparkSession.builder.appName("ExperimentMetrics").getOrCreate()

# 读取实验埋点数据
events = spark.sql("""
    SELECT
        user_id,
        experiment_group,
        event_type,
        event_value,
        dt
    FROM experiment_log
    WHERE dt = '{date}'
""")

# 计算用户级指标
user_metrics = events.groupBy("user_id", "experiment_group").agg(
    sum(when(col("event_type") == "click", 1).otherwise(0)).alias("click_count"),
    sum(when(col("event_type") == "impression", 1).otherwise(0)).alias("impression_count"),
    sum(when(col("event_type") == "order", col("event_value")).otherwise(0)).alias("gmv")
).withColumn("ctr", col("click_count") / col("impression_count"))

# A/B 检验
def ab_test(control_metrics, treatment_metrics):
    """两样本 t 检验"""
    control_values = control_metrics.select("ctr").toPandas()["ctr"]
    treatment_values = treatment_metrics.select("ctr").toPandas()["ctr"]

    t_stat, p_value = stats.ttest_ind(treatment_values, control_values)

    # 计算置信区间
    diff = treatment_values.mean() - control_values.mean()
    se = np.sqrt(treatment_values.var() / len(treatment_values) +
                 control_values.var() / len(control_values))
    ci = stats.t.interval(0.95, len(treatment_values) + len(control_values) - 2,
                          loc=diff, scale=se)

    return {
        "uplift": diff / control_values.mean(),
        "p_value": p_value,
        "confidence_interval": ci,
        "significant": p_value < 0.05
    }

# 执行检验
control = user_metrics.filter(col("experiment_group") == "control")
treatment = user_metrics.filter(col("experiment_group") == "treatment")

result = ab_test(control, treatment)
print(f"CTR uplift: {result['uplift']:.2%}")
print(f"P-value: {result['p_value']:.4f}")
print(f"Significant: {result['significant']}")
```

## 6. 统计检验

### 6.1 AA 检验

```
目的：验证实验平台本身没有偏差

方法：
1. 将流量均分为 A1、A2 两组（配置完全相同）
2. 运行一段时间
3. 检验 A1、A2 指标是否有显著差异

判断：
- P-value > 0.05：通过，平台无偏差
- P-value < 0.05：失败，检查分流逻辑
```

### 6.2 MDE（Minimum Detectable Effect）

```
MDE：最小可检测效应

公式：
MDE = (Z_(1-α/2) + Z_(1-β)) × σ × √(2/n)

参数：
- α：显著性水平（通常 0.05）
- β：统计功效（通常 0.2，即 80% 功效）
- σ：标准差
- n：样本量

意义：
- 给定样本量，能检测到的最小提升幅度
- 如果预期提升 < MDE，实验无法得出显著结论

样本量计算器：
n = 2 × (Z_(1-α/2) + Z_(1-β))² × σ² / MDE²
```

### 6.3 指标置信区间

```java
public class ConfidenceIntervalCalculator {

    public Interval calculate(double controlMean, double controlVar, int controlN,
                              double treatmentMean, double treatmentVar, int treatmentN) {
        // 均值差
        double diff = treatmentMean - controlMean;

        // 标准误
        double se = Math.sqrt(controlVar / controlN + treatmentVar / treatmentN);

        // 95% 置信区间
        double z = 1.96;  // 正态分布 95% 分位数
        double lower = diff - z * se;
        double upper = diff + z * se;

        return new Interval(lower, upper);
    }
}
```

## 7. 实验生命周期管理

### 7.1 状态流转

```
创建 -> 审核 -> 调试 -> 运行 -> 分析 -> 决策 -> 关闭

各阶段：
- 创建：配置实验参数
- 审核：检查流量冲突、样本量是否足够
- 调试：白名单验证
- 运行：自动监控告警
- 分析：自动生成报告
- 决策：全量 / 下线
```

### 7.2 互斥检测

```java
@Service
public class ExperimentValidator {

    public ValidationResult validate(Experiment newExp) {
        List<String> errors = new ArrayList<>();

        // 1. 检查同层流量溢出
        Layer layer = layerService.getByName(newExp.getLayer());
        int totalTraffic = layer.getExperiments().stream()
            .mapToInt(Experiment::getTrafficPercent)
            .sum();

        if (totalTraffic + newExp.getTrafficPercent() > 100) {
            errors.add("Layer traffic overflow");
        }

        // 2. 检查时间重叠的同类型实验
        List<Experiment> overlapping = experimentRepo.findOverlapping(
            newExp.getStartTime(),
            newExp.getEndTime(),
            newExp.getType()
        );

        if (!overlapping.isEmpty()) {
            errors.add("Overlapping experiments: " +
                overlapping.stream().map(Experiment::getName).collect(Collectors.joining(", ")));
        }

        // 3. 检查样本量是否足够（MDE）
        int requiredSample = calculateRequiredSample(newExp.getExpectedUplift());
        int availableSample = estimateSampleSize(newExp.getTrafficPercent(),
                                                 newExp.getDuration());

        if (availableSample < requiredSample) {
            errors.add("Insufficient sample size. Required: " + requiredSample +
                ", Available: " + availableSample);
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }
}
```

## 8. 面试要点

### Q1: 实验分层怎么做正交？

**答**：
1. 每层使用不同的 hash salt，如 `hash(user_id + layer_id)`
2. 数学上保证层间分流相互独立
3. 用户在每层独立分桶，层 A 的实验组用户在层 B 均匀分布
4. 同一层内实验互斥，流量不重叠

### Q2: 互斥实验怎么设计？

**答**：
1. 放在同一层内，分桶范围不重叠
2. 或使用互斥标签，标注哪些实验不能同时运行
3. 时间维度错开，不重叠的时间段运行
4. 人群维度划分，不同用户群体运行不同实验

### Q3: 指标计算怎么做？AA 检验是什么？

**答**：
1. 实时指标：Flink 窗口聚合，用于监控
2. 离线指标：Spark T+1 计算，用于决策
3. AA 检验：将流量分为两组相同配置，验证平台无偏差
4. 统计检验：两样本 t 检验，计算 P-value 和置信区间

### Q4: 怎么确定实验跑多久？

**答**：
1. 计算 MDE（最小可检测效应）
2. 根据预期提升和 MDE，计算所需样本量
3. 根据日活和流量比例，估算所需天数
4. 考虑周期性（至少包含一个完整的周周期）
5. 一般原则：至少跑 1-2 周，样本量足够支撑检测预期提升

### Q5: 实验结果显著就一定能全量吗？

**答**：不一定，还要看：
1. 守卫指标是否恶化（如延迟、错误率）
2. 长期指标是否正向（留存、LTV）
3. 不同人群的差异（是否对某类用户有害）
4. 业务逻辑是否合理（统计显著 ≠ 业务显著）
