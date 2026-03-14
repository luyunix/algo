# 设计秒杀系统

## 需求分析

### 业务场景

- 商品库存有限（如 100 件）
- 大量用户同时抢购（如 100 万人）
- 时间严格（准时开始）
- 每人限购数量

### 非功能需求

| 需求 | 目标 |
|------|------|
| 可用性 | 99.99% |
| 一致性 | 不能超卖 |
| 性能 | 响应 < 200ms |
| 公平性 | 先到先得 |

## 挑战分析

```
正常场景：
100 库存，100 人购买 → 100 人成功

秒杀场景：
100 库存，100 万人抢购
- 10000 倍流量突增
- 数据库压力大
- 恶意刷单
- 库存超卖风险
```

## 系统架构

```
                    ┌─────────────┐
                    │  CDN/WAF    │
                    │ (静态化/防刷)│
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │  API Gateway│
                    │  (限流/鉴权) │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│   Web        │   │   Redis      │   │   Queue      │
│   Server     │   │   (库存)      │   │   (异步下单)  │
│   (集群)     │   │   (集群)      │   │   (Kafka)    │
└──────────────┘   └──────────────┘   └──────┬───────┘
                                              │
                                              ▼
                                       ┌──────────────┐
                                       │   Consumer   │
                                       │   (订单处理)  │
                                       └──────┬───────┘
                                              │
                                       ┌──────┴──────┐
                                       ▼             ▼
                                ┌──────────┐   ┌──────────┐
                                │   DB     │   │   DB     │
                                │ (订单)   │   │ (库存)   │
                                └──────────┘   └──────────┘
```

## 核心流程

### 1. 流量削峰

```
策略 1：页面静态化
- 商品详情页静态化到 CDN
- 用户直接访问 CDN，不经过服务器
- 开始后才暴露购买按钮

策略 2：验证码/答题
- 秒杀前要求输入验证码
- 分散用户点击时间
- 过滤机器人

策略 3：限流
- 网关层限流：每秒最多 N 个请求
- 用户限流：每个用户每秒最多 1 次
```

### 2. 库存扣减

#### 方案 1：数据库（不推荐）

```sql
UPDATE stock SET count = count - 1
WHERE product_id = 1 AND count > 0;
```

**问题**：
- 数据库压力大
- 行锁竞争严重
- 性能差

#### 方案 2：Redis 原子操作（推荐）

```
1. 预热：秒杀前将库存加载到 Redis
   SET stock:1001 100

2. 扣减：使用 DECR（原子操作）
   DECR stock:1001
   - 返回 >= 0：扣减成功，有资格下单
   - 返回 < 0：库存不足，返回失败

3. 控制并发：使用 Lua 脚本保证原子性
```

```lua
-- 扣减库存 + 记录用户
local stock = redis.call('get', KEYS[1])
if tonumber(stock) <= 0 then
    return 0  -- 库存不足
end

-- 检查用户是否已购买
local userKey = KEYS[2] .. ':' .. ARGV[1]
if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
    return -1  -- 已经购买过
end

-- 扣减库存 + 记录用户
redis.call('decr', KEYS[1])
redis.call('sadd', KEYS[2], ARGV[1])
return 1  -- 成功
```

### 3. 异步下单

```
成功扣减 Redis 库存后：
1. 返回用户"抢购成功"
2. 发送 MQ 消息异步创建订单
3. 用户跳转到订单页面等待

MQ 消费者：
1. 从消息中获取用户和商品信息
2. 创建订单（数据库）
3. 扣减实际库存（数据库）
4. 发送支付提醒

好处：
- 用户无需等待订单创建
- 平滑处理数据库压力
- 可以控制消费速率
```

### 4. 防超卖

```
多层校验：
1. Redis 预扣减：第一道防线，快速拒绝
2. 数据库唯一索引：(user_id, product_id)
3. 数据库乐观锁：version 字段
4. 对账机制：定时检查 Redis 和 DB 一致性
```

## 详细设计

### 秒杀链接隐藏

```
问题：提前知道秒杀链接，使用脚本刷单

解决：
1. 动态 URL：秒杀开始前 5 分钟才生成
2. 带签名：URL 包含时间戳和签名，过期失效
3. 单用户限频：Nginx limit_req
```

### 用户验证

```
1. 登录验证：必须登录才能抢购
2. 风控检测：
   - 同一 IP 请求数限制
   - 同一设备请求数限制
   - 账号黑名单
3. 验证码：
   - 点击前需要完成验证码
   - 滑块验证码、点选验证码
```

### 库存预热

```
秒杀开始前：
1. 将商品库存加载到 Redis
   SET stock:{productId} {count}
2. 将已购买用户集合清空
   DEL buyers:{productId}
3. 预热完毕标记
   SET ready:{productId} 1

预热时机：
- 秒杀前 5-10 分钟
- 或者通过配置中心实时推送
```

### 削峰填谷

```
漏斗设计：

100万请求
    │
    ▼
┌─────────┐  丢弃 90%  10万
│   CDN   │ ──────────►
│  静态化  │
└────┬────┘
     │
     ▼
┌─────────┐  丢弃 50%  5万
│   验证码 │ ──────────►
│  限流   │
└────┬────┘
     │
     ▼
┌─────────┐  丢弃 80%  1万（实际库存的 10 倍）
│  Redis  │ ──────────► 有资格下单
│  库存   │
└─────────┘

层层过滤，最终只有少量请求到达数据库
```

## 代码示例

### 秒杀服务

```java
@Service
public class SeckillService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public SeckillResult seckill(Long userId, Long productId) {
        // 1. 检查是否已购买
        String userKey = "seckill:buyers:" + productId;
        Boolean hasBought = redisTemplate.opsForSet()
            .isMember(userKey, userId.toString());
        if (Boolean.TRUE.equals(hasBought)) {
            return SeckillResult.fail("您已经抢购过了");
        }

        // 2. Redis 扣减库存（Lua 脚本保证原子性）
        String stockKey = "seckill:stock:" + productId;
        Long stock = redisTemplate.opsForValue().decrement(stockKey);

        if (stock == null || stock < 0) {
            // 库存不足，恢复库存
            redisTemplate.opsForValue().increment(stockKey);
            return SeckillResult.fail("商品已售罄");
        }

        // 3. 记录用户
        redisTemplate.opsForSet().add(userKey, userId.toString());

        // 4. 发送 MQ 异步下单
        SeckillMessage message = new SeckillMessage(userId, productId);
        kafkaTemplate.send("seckill-order", message);

        return SeckillResult.success("抢购成功，正在生成订单...");
    }
}
```

### 订单消费者

```java
@Component
public class OrderConsumer {
    @KafkaListener(topics = "seckill-order")
    public void consume(SeckillMessage message) {
        try {
            // 1. 创建订单
            Order order = createOrder(message.getUserId(), message.getProductId());

            // 2. 扣减数据库库存
            boolean success = stockService.decreaseStock(
                message.getProductId(), 1);

            if (!success) {
                // 回滚订单
                orderService.cancelOrder(order.getId());
                // 补偿 Redis 库存
                redisTemplate.opsForValue()
                    .increment("seckill:stock:" + message.getProductId());
            }
        } catch (Exception e) {
            // 异常处理，记录日志，人工介入
            log.error("订单处理失败", e);
        }
    }
}
```

## 异常处理

### 库存不一致处理

```
场景：Redis 扣减成功，但数据库扣减失败

处理：
1. 对账任务定时检查 Redis 和 DB 库存差异
2. 差异超过阈值报警
3. 人工介入或自动补偿
```

### 消息丢失处理

```
1. 生产者确认：Kafka acks=all
2. 消费者手动提交 offset
3. 业务幂等：数据库唯一索引防止重复消费
4. 死信队列：处理失败的消息转入死信队列
```

## 面试要点

### Q1: 如何保证不超卖？

**答**：
1. Redis 原子操作预扣减库存
2. 数据库唯一索引防止重复下单
3. 数据库乐观锁二次校验
4. 定时对账检查数据一致性

### Q2: 如何应对 100 万并发？

**答**：
1. 页面静态化，流量走 CDN
2. 验证码、答题分散流量
3. 网关限流，丢弃大部分请求
4. Redis 抗并发，快速过滤
5. MQ 异步削峰，平滑处理订单
6. 层层过滤，最终只有库存数倍请求到达 DB

### Q3: 如何防止机器人刷单？

**答**：
1. 验证码：图形验证码、滑块验证码
2. 限流：IP 限流、用户限流、设备限流
3. 风控：行为分析、设备指纹
4. 链接隐藏：动态 URL，无法提前准备脚本
