# 设计限流器 (Rate Limiter)

## 什么是限流

限流是控制服务接收请求速率的机制，防止突发流量压垮系统，保障服务稳定运行。

```
无限流：
突发 10万 QPS ──► 系统崩溃

有限流：
突发 10万 QPS ──► 限流器 ──► 系统按 1万 QPS 处理
                  └── 拒绝或排队 9万 QPS
```

## 限流算法

### 1. 固定窗口计数器 (Fixed Window)

```
原理：
- 将时间划分为固定窗口（如 1 秒）
- 每个窗口内计数请求数
- 超过阈值则拒绝

示例（限流 100/秒）：
时间线：0s    1s    2s    3s
窗口1： 0-1s 计数=100（满了）
窗口2： 1-2s 计数=50
窗口3： 2-3s 计数=80

问题：窗口边界突发
0.9s 来了 100 个请求，1.0s 又来了 100 个请求
实际上 0.1 秒内处理了 200 个请求
```

**实现**：
```java
public class FixedWindowRateLimiter {
    private final long windowSizeMs;
    private final int maxRequests;
    private final AtomicInteger counter = new AtomicInteger(0);
    private volatile long windowStart = System.currentTimeMillis();

    public synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();
        if (now - windowStart >= windowSizeMs) {
            counter.set(0);
            windowStart = now;
        }
        return counter.incrementAndGet() <= maxRequests;
    }
}
```

### 2. 滑动窗口日志 (Sliding Window Log)

```
原理：
- 记录每个请求的时间戳
- 检查过去 1 秒内的请求数
- 超过阈值则拒绝

优点：精确，无边界问题
缺点：内存占用大，需要存储所有时间戳
```

### 3. 滑动窗口计数器 (Sliding Window)

```
原理：
- 结合固定窗口和滑动窗口
- 将窗口细分为多个小窗口
- 计算最近 N 个小窗口的总和

示例（限流 100/秒，分 10 个小窗口）：
时间：0.0  0.1  0.2  0.3  0.4  0.5  0.6  0.7  0.8  0.9  1.0
计数：10   10   10   10   10   10   10   10   10   10   10

当前时间 0.95s，计算 [0.0, 0.95) 的总和 = 95
还可以接受 5 个请求
```

### 4. 漏桶算法 (Leaky Bucket)

```
原理：
- 请求像水一样流入桶
- 桶以固定速率漏水（处理请求）
- 桶满则溢出（拒绝请求）

        请求流入
            │
            ▼
    ┌─────────────┐
    │             │
    │     桶      │ ──► 固定速率流出（处理）
    │  (队列)      │
    │             │
    └─────────────┘
            │
            ▼
        桶满则拒绝

特点：
- 流出速率绝对均匀
- 可以缓存突发流量
- 适合需要平滑流量的场景
```

**实现**：
```java
public class LeakyBucketRateLimiter {
    private final int capacity;      // 桶容量
    private final int leakRate;      // 漏水速率（每秒）
    private double water = 0;        // 当前水量
    private long lastLeakTime = System.currentTimeMillis();

    public synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();
        // 先漏水
        long elapsed = now - lastLeakTime;
        water = Math.max(0, water - elapsed * leakRate / 1000.0);
        lastLeakTime = now;

        // 检查容量
        if (water + 1 <= capacity) {
            water += 1;
            return true;
        }
        return false;
    }
}
```

### 5. 令牌桶算法 (Token Bucket) - 最常用

```
原理：
- 桶以固定速率产生令牌
- 请求需要获取令牌才能执行
- 桶满则不再产生令牌
- 突发流量可以一次性消耗桶内令牌

    令牌以固定速率放入
            │
            ▼
    ┌─────────────┐
    │   令牌桶     │
    │  (有容量)    │
    └──────┬──────┘
           │
    请求 ──►▼───► 有令牌则通过，无令牌则拒绝

特点：
- 允许一定突发流量
- 长期速率固定
- 适合互联网场景
```

**实现**：
```java
public class TokenBucketRateLimiter {
    private final int capacity;      // 桶容量
    private final int refillRate;    // 每秒放入令牌数
    private double tokens;           // 当前令牌数
    private long lastRefillTime = System.currentTimeMillis();

    public synchronized boolean allowRequest() {
        refill();
        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        double tokensToAdd = (now - lastRefillTime) * refillRate / 1000.0;
        tokens = Math.min(capacity, tokens + tokensToAdd);
        lastRefillTime = now;
    }
}
```

## 算法对比

| 算法 | 突发流量 | 内存占用 | 平滑性 | 复杂度 | 适用场景 |
|------|----------|----------|--------|--------|----------|
| 固定窗口 | 边界问题 | 低 | 差 | 低 | 简单场景 |
| 滑动窗口 | 好 | 高 | 好 | 中 | 精确限流 |
| 漏桶 | 缓存 | 中 | 最好 | 中 | 严格平滑 |
| 令牌桶 | 允许突发 | 低 | 好 | 低 | 通用场景 |

## 分布式限流

### 方案 1：Redis + Lua

```lua
-- 令牌桶限流（Redis + Lua 保证原子性）
local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refillRate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local lastTokens = redis.call('hget', key, 'tokens')
local lastTime = redis.call('hget', key, 'timestamp')

if lastTokens == false then
    lastTokens = capacity
    lastTime = now
end

local delta = math.max(0, now - tonumber(lastTime))
local tokens = math.min(capacity, tonumber(lastTokens) + delta * refillRate / 1000)

local allowed = 0
if tokens >= 1 then
    tokens = tokens - 1
    allowed = 1
end

redis.call('hset', key, 'tokens', tokens)
redis.call('hset', key, 'timestamp', now)

return allowed
```

### 方案 2：Redis Cell 模块

```bash
# Redis 4.0 可加载模块
CL.THROTTLE user:123 15 30 60 1
#            key    容量 速率 周期 本次请求数
# 允许 15 个突发，每 60 秒产生 30 个令牌
```

### 方案 3：Sentinel 限流

```
阿里开源 Sentinel：
- 控制台动态配置限流规则
- 支持多种限流策略
- 支持熔断降级
- 实时监控
```

## 限流策略

### 限流维度

| 维度 | 示例 | 粒度 |
|------|------|------|
| 全局 | 整个系统 10000 QPS | 粗 |
| API | /api/order 500 QPS | 中 |
| 用户 | 每个用户 100 QPS | 细 |
| IP | 每个 IP 50 QPS | 细 |
| 组合 | 用户 + API | 更细 |

### 限流响应

```
1. 直接拒绝：
   HTTP 429 Too Many Requests
   { "error": "Rate limit exceeded", "retry_after": 60 }

2. 排队等待：
   - 进入消息队列
   - 稍后处理
   - 适合非实时任务

3. 降级服务：
   - 返回简化版数据
   - 返回缓存数据
   - 关闭非核心功能
```

## 架构设计

```
                    ┌─────────────┐
                    │  API Gateway │
                    │   (限流层)    │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
        ┌─────────┐  ┌─────────┐  ┌─────────┐
        │ Service │  │ Service │  │ Service │
        │   A     │  │   B     │  │   C     │
        └────┬────┘  └────┬────┘  └────┬────┘
             │            │            │
             └────────────┼────────────┘
                          ▼
                   ┌─────────────┐
                   │    Redis    │
                   │  (限流计数)  │
                   └─────────────┘
```

## 面试要点

### Q1: 令牌桶和漏桶有什么区别？

**答**：
- 令牌桶：以固定速率产生令牌，请求获取令牌才能执行，允许突发流量（桶内令牌可以一次性用完）
- 漏桶：以固定速率处理请求，请求先进入桶中排队，流出速率绝对均匀，可以缓存突发流量但处理速率固定
- 令牌桶更适合互联网场景，允许一定突发；漏桶适合需要严格平滑的场景

### Q2: 如何实现集群级别的限流？

**答**：
1. 使用 Redis 存储计数，Lua 脚本保证原子性
2. 使用令牌桶算法，将令牌信息存储在 Redis
3. 或者使用集中式限流服务（如 Sentinel）
4. 每个节点先本地限流，再请求全局限流

### Q3: 限流和熔断有什么区别？

**答**：
- 限流：预防性措施，控制请求速率，防止系统过载
- 熔断：补救性措施，当错误率达到阈值时，快速失败，防止故障扩散
- 两者通常配合使用，限流是第一道防线，熔断是第二道防线
