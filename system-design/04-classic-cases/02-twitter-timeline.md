# 设计 Twitter/微博 时间线

## 需求分析

### 功能需求

1. **发布推文**：用户可以发布推文
2. **关注/取关**：关注/取消关注其他用户
3. **时间线**：查看关注用户的推文，按时间倒序
4. **用户主页**：查看某个用户的所有推文

### 非功能需求

| 需求 | 目标 |
|------|------|
| 可用性 | 99.99% |
| 延迟 | 时间线加载 < 200ms |
| 一致性 | 最终一致（可接受短暂延迟）|

## 容量估算

### 假设

```
- MAU: 3 亿
- DAU: 1 亿
- 每人每天发 1 条推文
- 每人每天看 100 条推文
- 平均关注: 200 人
- 粉丝分布：90% 用户粉丝 < 100，10% 大 V 粉丝 100万+
```

### 计算

```
推文写入：
- QPS: 1亿 / 86400 ≈ 1157
- 峰值: 3500 QPS

时间线读取：
- QPS: 1亿 × 100 / 86400 ≈ 115,740
- 峰值: 350,000 QPS

存储：
- 推文: 500 bytes/条
- 日新增: 1亿 × 500B = 50 GB
- 5 年: 50 GB × 365 × 5 = 91 TB

关注关系：
- 3亿用户 × 200 关注 = 600 亿条关系
- 每条关系: 16 bytes
- 总存储: ~100 GB
```

## 数据库设计

```sql
-- 用户表
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    email VARCHAR(100),
    created_at TIMESTAMP,
    followers_count INT DEFAULT 0,
    following_count INT DEFAULT 0
);

-- 推文表（按 user_id 分片）
CREATE TABLE tweets (
    tweet_id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT,
    created_at TIMESTAMP,
    INDEX idx_user_time (user_id, created_at)
);

-- 关注关系表
CREATE TABLE follows (
    follower_id BIGINT NOT NULL,
    followee_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, followee_id),
    INDEX idx_followee (followee_id)
);

-- 时间线表（Fan Out On Write 方案）
CREATE TABLE timeline (
    user_id BIGINT NOT NULL,
    tweet_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    PRIMARY KEY (user_id, created_at, tweet_id),
    INDEX idx_user_time (user_id, created_at)
) ENGINE=InnoDB;
```

## 核心设计：时间线模型

### 方案 1：拉模型 (Pull / Fan Out On Read)

```
时间线生成流程：
1. 用户请求时间线
2. 获取关注列表（200 人）
3. 查询每个关注者的最新推文
4. 合并排序，取前 N 条

示例代码：
List<Long> followees = getFollowees(userId);
List<Tweet> tweets = new ArrayList<>();
for (Long followeeId : followees) {
    tweets.addAll(getRecentTweets(followeeId, 20));
}
tweets.sort(Comparator.comparing(Tweet::getCreatedAt).reversed());
return tweets.subList(0, 50);
```

**优点**：
- 写轻量：发推时只写推文表
- 省空间：不需要预生成时间线

**缺点**：
- 读沉重：需要查询多个用户
- 延迟高：关注人多时性能差
- 缓存复杂：难以有效缓存

### 方案 2：推模型 (Push / Fan Out On Write)

```
发推流程：
1. 用户 A（100 粉丝）发推
2. 将推文写入用户 A 的推文表
3. 推送到所有粉丝的 timeline 表
   - 写入 100 条记录

读取流程：
1. 直接从 timeline 表读取前 N 条
2. 单表查询，性能极高
```

**优点**：
- 读极快：单表查询
- 可缓存：时间线天然适合缓存

**缺点**：
- 写沉重：大 V 发推时需要写入数百万条
- 存储大：需要为每个用户存储时间线副本
- 延迟：大 V 发推时延迟高

### 方案 3：混合模型（Twitter 实际方案）

```
普通用户（粉丝 < 1000）：推模型
- 发推时立即推送到粉丝时间线
- 读时直接查时间线表

大 V（粉丝 >= 1000）：拉模型
- 发推时不推送到粉丝时间线
- 读时实时拉取大 V 的推文，与本地时间线合并
```

```java
List<Tweet> getTimeline(Long userId) {
    // 1. 获取普通关注者的时间线（推模型）
    List<Long> normalFollowees = getNormalFollowees(userId);
    List<Tweet> normalTweets = getTimelineFromCache(userId, normalFollowees);

    // 2. 获取大 V 的最新推文（拉模型）
    List<Long> celebFollowees = getCelebFollowees(userId);
    List<Tweet> celebTweets = fetchFromCelebs(celebFollowees);

    // 3. 合并排序
    List<Tweet> allTweets = mergeAndSort(normalTweets, celebTweets);
    return allTweets.subList(0, 50);
}
```

## 系统架构

```
                    ┌─────────────┐
                    │  Load Balancer│
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  Write Service│   │  Read Service │   │ Graph Service│
│  (发推、关注)  │   │  (时间线读取)  │   │ (关注关系)   │
└──────┬───────┘   └──────┬───────┘   └──────┬───────┘
       │                  │                  │
       └──────────────────┼──────────────────┘
                          │
       ┌──────────────────┼──────────────────┐
       ▼                  ▼                  ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  Tweet DB    │   │ Timeline DB  │   │   Graph DB   │
│ (推文存储)    │   │ (时间线缓存)  │   │ (关注关系)   │
│   (分片)      │   │   (分片)      │   │  (图数据库)  │
└──────────────┘   └──────────────┘   └──────────────┘
       │                  │
       └──────────────────┘
                          ▼
                   ┌──────────────┐
                   │    Redis     │
                   │ (热点缓存)    │
                   └──────────────┘
```

## 缓存策略

### 多级缓存

```
1. 本地缓存 (Caffeine)：
   - 用户自己的时间线
   - 热点用户的推文
   - TTL: 1 分钟

2. Redis 缓存：
   - timeline:user:{userId} → 最近 200 条推文ID
   - tweet:{tweetId} → 推文内容
   - user:{userId}:tweets → 用户最近推文
   - TTL: 1 小时

3. CDN：
   - 用户头像、图片等媒体资源
```

### 缓存更新

```
推模型下：
1. 用户发推
2. 写入 Tweet DB
3. 推送到粉丝 timeline
4. 更新 Redis：删除或追加缓存
5.  fans 下次读取时重建缓存
```

## 优化策略

### 1. 异步推送

```
大 V 发推时，不立即推送到所有粉丝：
1. 写入推文表
2. 发送 MQ 消息
3. 消费者异步推送到粉丝时间线
4. 设置优先级：活跃用户优先推送
```

### 2. 时间线分段

```
将时间线分为多个桶：
- 近 24 小时：热数据，内存缓存
- 24小时-7天：温数据，Redis
- 7天前：冷数据，数据库

用户翻页时按需加载
```

### 3. 限流与降级

```
大 V 发推限流：
- 每分钟最多发 N 条
- 避免瞬间大量写入

读取降级：
- 高并发时，只返回缓存数据
- 不实时合并大 V 推文
```

## 图数据库优化

```
关注关系适合用图数据库存储：

Neo4j / DGraph 查询：
MATCH (u:User {id: $userId})-[:FOLLOWS]->(followee)
RETURN followee.id

优势：
- 查询关注列表快
- 支持二度、三度关系查询
- 支持共同关注、可能认识的人等推荐
```

## 面试要点

### Q1: Twitter 为什么要用混合模型，而不是纯推或纯拉？

**答**：
- 纯推模型：大 V 发推时需要推送给数百万粉丝，延迟太高，写入量太大
- 纯拉模型：普通用户读取时需要查询几百个关注者，查询量太大，延迟太高
- 混合模型：普通用户用推模型保证读性能，大 V 用拉模型避免写放大，两者取长补短

### Q2: 如何解决推模型下的存储空间问题？

**答**：
1. 只存储最近 N 条（如 1000 条）
2. 冷数据归档到对象存储
3. 不活跃用户不推送（延迟到读时再拉取）
4. 使用压缩存储（推文 ID 列表而不是完整内容）

### Q3: 如果大 V 突然发了一条热门推文，如何防止系统被压垮？

**答**：
1. 异步推送：不立即推送到所有粉丝，通过 MQ 慢慢推送
2. 合并读取：大 V 推文实时拉取，不占用推送带宽
3. 缓存预热：大 V 的推文优先缓存
4. 降级策略：高并发时只返回缓存数据，不保证实时性
