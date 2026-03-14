# 系统设计完整学习指南

## 学习路线图

```
第 1-2 周：基础概念 + 核心组件
├─ 01-fundamentals/
│  ├─ 什么是系统设计（4S框架）
│  ├─ CAP 定理
│  ├─ 性能指标（延迟、吞吐、可用性）
│  └─ 一致性模型
├─ 02-core-components/
│  ├─ 负载均衡（算法、L4/L7）
│  ├─ 缓存（策略、问题、Redis）
│  ├─ 数据库（SQL/NoSQL、分库分表）
│  ├─ 消息队列（Kafka、模式）
│  └─ CDN（加速、回源策略）

第 3-4 周：分布式系统
└─ 03-distributed-systems/
   ├─ 分布式 ID（雪花算法、号段模式）
   ├─ 分布式锁（Redis、ZooKeeper）
   ├─ 分布式事务（2PC、TCC、Saga）
   └─ 服务发现（Eureka、Consul、Nacos）

第 5-6 周：经典案例（每天一个）
└─ 04-classic-cases/
   ├─ 01 短链接系统（TinyURL）
   ├─ 02 Twitter 时间线（推拉模型）
   ├─ 03 即时通讯系统（IM、WebSocket）
   ├─ 04 限流器（令牌桶、漏桶）
   ├─ 05 搜索引擎（倒排索引）
   ├─ 06 文件存储系统（对象存储）
   ├─ 07 秒杀系统（Redis 扣减、MQ）
   ├─ 08 推荐系统（召回、排序）
   └─ 【搜索推荐工程专项】
      ├─ 09 推荐平台架构（TPP DAG 执行引擎）
      ├─ 10 向量检索与索引系统
      ├─ 11 实时特征计算
      ├─ 12 A/B 实验平台
      ├─ 13 搜索系统设计（Query理解、倒排索引）
      └─ 14 JVM 多版本业务代码平台（ClassLoader 隔离、热部署）

第 7-8 周：进阶主题
└─ 05-advanced/
   ├─ 微服务架构（拆分、熔断、Service Mesh）
   ├─ 容器与编排（Docker、Kubernetes）
   ├─ 可观测性（日志、指标、追踪）
   └─ 系统安全（OWASP、防护策略）

面试准备：速查表
└─ cheatsheet/
   ├─ 系统设计面试模板（4S框架）
   └─ 常用计算（QPS、存储、带宽）
```

## 学习建议

### 第一阶段（基础）
- 理解每个组件的原理和适用场景
- 记住关键数字（延迟、QPS、存储）
- 能够手绘架构图

### 第二阶段（分布式）
- 深入理解分布式系统的难点
- 掌握一致性、可用性的权衡
- 理解各种分布式协议的优缺点

### 第三阶段（实战）
- 每个案例都要自己画一遍架构图
- 做容量估算练习
- 思考扩展性和故障处理

### 第四阶段（面试）
- 用 4S 框架练习回答
- 限时模拟面试
- 反复优化表达

## 推荐资源

### 书籍
- 《Designing Data-Intensive Applications》（DDIA）- 必读
- 《System Design Interview》
- 《微服务设计》

### 网站
- [System Design Primer](https://github.com/donnemartin/system-design-primer)
- [ByteByteGo](https://bytebytego.com/)
- [High Scalability](http://highscalability.com/)

### 视频
- YouTube: System Design Interview
- B站: 各大厂技术分享

## 目录结构

```
system-design/
├── 01-fundamentals/          # 基础概念
│   ├── 01-what-is-system-design.md
│   ├── 02-cap-theorem.md
│   ├── 03-performance-metrics.md
│   └── 04-consistency-models.md
├── 02-core-components/       # 核心组件
│   ├── 01-load-balancer.md
│   ├── 02-caching.md
│   ├── 03-database.md
│   ├── 04-message-queue.md
│   └── 05-cdn.md
├── 03-distributed-systems/   # 分布式系统
│   ├── 01-distributed-id.md
│   ├── 02-distributed-lock.md
│   ├── 03-distributed-transaction.md
│   └── 04-service-discovery.md
├── 04-classic-cases/         # 经典案例
│   ├── 01-tinyurl.md
│   ├── 02-twitter-timeline.md
│   ├── 03-chat-system.md
│   ├── 04-rate-limiter.md
│   ├── 05-search-engine.md
│   ├── 06-file-storage.md
│   ├── 07-seckill.md
│   ├── 08-recommendation.md
│   ├── 09-recommendation-platform.md  # 【推荐工程专项】
│   ├── 10-vector-search.md             # 【推荐工程专项】
│   ├── 11-realtime-features.md         # 【推荐工程专项】
│   ├── 12-ab-testing.md                # 【推荐工程专项】
│   ├── 13-search-system.md             # 【推荐工程专项】
│   └── 14-jvm-multiversion-platform.md # 【推荐工程专项】【你的项目】
├── 05-advanced/              # 进阶主题
│   ├── 01-microservices.md
│   ├── 02-containerization.md
│   ├── 03-observability.md
│   └── 04-security.md
└── cheatsheet/               # 速查表
    ├── system-design-template.md
    └── common-calculations.md
```

## 如何开始

1. 从 `01-fundamentals/01-what-is-system-design.md` 开始
2. 按顺序学习基础概念
3. 每学完一个主题，用自己的话总结
4. 进入经典案例阶段时，先自己思考再看书中的方案
5. 使用速查表进行面试模拟

## 针对搜索推荐工程背景的学习重点

如果你和我一样在做搜索推荐工程（TPP、索引平台、推荐平台），面试重点和普通 SDE 不同：

### 必须深入掌握（P7/P8 级别）

| 主题 | 核心考察点 | 对应文档 |
|------|-----------|---------|
| **在线服务架构** | TPP DAG 执行引擎、节点调度、超时熔断、特征缓存 | 04-classic-cases/09-recommendation-platform.md |
| **向量检索** | HNSW 原理、十亿级索引、增量更新、混合检索 | 04-classic-cases/10-vector-search.md |
| **实时特征** | Flink Exactly-Once、双流 Join、特征一致性 | 04-classic-cases/11-realtime-features.md |
| **A/B 实验** | 分层分流、流量正交、MDE 计算、AA 检验 | 04-classic-cases/12-ab-testing.md |
| **搜索系统** | Query理解、倒排索引、BM25、向量召回、相关性排序 | 04-classic-cases/13-search-system.md |
| **JVM 多版本平台** | ClassLoader 隔离、热部署、灰度发布、版本路由 | 04-classic-cases/14-jvm-multiversion-platform.md |

### 面试准备策略

1. **准备 3-5 个深度 Case Study**（必须包含 JVM 多版本平台）
   - **JVM 多版本平台**（你的核心项目）：ClassLoader 隔离、热部署、灰度发布
   - 性能优化（QPS/延迟提升 X%）
   - 架构重构（技术债治理）
   - 实时化改造（离线转实时）
   - 故障处理（应急 + 复盘）

2. **技术深度问题（必问）**
   - "你们实时特征用的 Flink，如果任务失败怎么保证特征不丢失？"
   - "推荐系统从请求到返回要过几十个模型，整体超时怎么控制？"
   - "向量索引更新时，怎么做到在线服务不中断？"

3. **软技能考察**
   - 团队分工与管理
   - 跨团队协作（推动算法落地）
   - 业务价值量化（技术 ROI）

### 算法理解（不要求写模型，但要懂）

```
需要理解：
├── 推荐系统整体 pipeline（召回→粗排→精排→重排）
├── 常用模型特点（LR、GBDT、DIN、Transformer）
├── 特征工程（离散/连续/序列特征处理）
└── 模型评估（AUC、GAUC、NDCG）

不需要：
└── 手推反向传播、手撕 Transformer（除非面算法岗）
```

祝学习顺利！
