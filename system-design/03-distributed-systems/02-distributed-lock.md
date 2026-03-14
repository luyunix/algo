# 分布式锁

## 为什么需要分布式锁

在分布式系统中，多个进程/节点可能同时访问共享资源，需要互斥机制保证数据一致性。

```
单机锁（synchronized/Lock）：
- 基于 JVM 内存，只对同一进程内的线程有效

分布式锁：
- 跨进程、跨机器的互斥机制
- 基于外部协调服务（Redis、ZooKeeper 等）
```

## 分布式锁的要求

| 特性 | 说明 |
|------|------|
| 互斥性 | 同一时刻只有一个客户端能获取锁 |
| 防死锁 | 客户端宕机后，锁能自动释放 |
| 可重入 | 同一客户端可以重复获取锁 |
| 阻塞/非阻塞 | 支持阻塞等待或非阻塞获取 |
| 高可用 | 锁服务本身不能单点故障 |

## 基于 Redis 的实现

### 基本版本（SETNX + EXPIRE）

```bash
# 获取锁
SETNX lock:order:123 "client-1"    # 设置成功返回 1，失败返回 0
EXPIRE lock:order:123 30           # 设置 30 秒过期

# 释放锁
DEL lock:order:123
```

**问题**：SETNX 和 EXPIRE 不是原子操作，如果执行完 SETNX 后宕机，锁永不释放。

### 改进版本（原子命令）

```bash
# Redis 2.6.12+ 支持
SET lock:order:123 "client-1" NX EX 30

# 参数说明
NX - 只在 key 不存在时才设置
EX - 设置过期时间（秒）
PX - 设置过期时间（毫秒）
```

### 安全释放（防误删）

```lua
-- unlock.lua
if redis.call("get", KEYS[1]) == ARGV[1] then
    return redis.call("del", KEYS[1])
else
    return 0
end
```

```java
// Java 调用
String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "return redis.call('del', KEYS[1]) else return 0 end";
redis.eval(script, Collections.singletonList(lockKey),
           Collections.singletonList(clientId));
```

### Redisson 实现

Redisson 是 Redis 的 Java 客户端，提供了完善的分布式锁实现：

```java
// 获取锁
RLock lock = redisson.getLock("order:123");
lock.lock();  // 阻塞获取

try {
    // 执行业务逻辑
} finally {
    lock.unlock();
}
```

**看门狗机制**：
```
1. 加锁时默认设置 30 秒过期时间
2. 启动定时任务，每 10 秒（1/3 过期时间）检查一次
3. 如果业务还未执行完，自动续期到 30 秒
4. 业务完成后，停止看门狗，释放锁
```

### Redis 锁的问题

| 问题 | 描述 | 解决方案 |
|------|------|----------|
| 单点故障 | Redis 宕机导致锁失效 | Redis Cluster / RedLock |
| 主从延迟 | 主从切换时可能丢失锁 | 等待主从同步完成 / RedLock |
| 时钟漂移 | 不同机器时钟不一致 | 使用单调时钟 / ZooKeeper |

### RedLock 算法

```
在 N 个独立的 Redis 节点上加锁：
1. 记录开始时间 T1
2. 依次向 N 个节点发送加锁请求
3. 统计在过期时间前成功加锁的节点数
4. 如果成功节点数 >= N/2 + 1，且耗时 < 锁有效期，则加锁成功
5. 如果失败，向所有节点发送解锁请求
```

**争议**：RedLock 被 Redis 作者提出，但被分布式系统专家质疑（Martin Kleppmann）。

## 基于 ZooKeeper 的实现

### 原理

利用 ZooKeeper 的临时顺序节点实现：

```
1. 每个客户端在 /locks/order 下创建临时顺序节点
   /locks/order/lock-00000001
   /locks/order/lock-00000002
   /locks/order/lock-00000003

2. 序号最小的节点获得锁

3. 未获得锁的节点监听前一个节点的删除事件
   lock-00000002 监听 lock-00000001
   lock-00000003 监听 lock-00000002

4. 前一个节点删除后，后一个节点被唤醒获取锁

5. 客户端断开连接，临时节点自动删除（防死锁）
```

### Curator 实现

```java
// 使用 Curator 框架
InterProcessMutex lock = new InterProcessMutex(client, "/locks/order");

if (lock.acquire(10, TimeUnit.SECONDS)) {
    try {
        // 执行业务逻辑
    } finally {
        lock.release();
    }
}
```

### 优缺点

```
优点：
- 天然防死锁（临时节点）
- 支持可重入
- 支持阻塞等待
- 强一致性（ZAB 协议）

缺点：
- 性能不如 Redis
- 需要维护 ZooKeeper 集群
- 会话超时可能导致锁提前释放
```

## 基于 Etcd 的实现

### 原理

```
1. 使用 revision 机制保证顺序
2. 创建 key 时获取全局唯一的 revision
3. revision 最小的获得锁
4. 支持 Watch 机制监听前一个 revision
5. 租约（Lease）机制防止死锁
```

```bash
# 创建带租约的 key
etcdctl lease grant 30  # 30 秒租约
lease keep-alive <lease-id>  # 续约

etcdctl put lock/order "client-1" --lease=<lease-id>
```

### 优缺点

```
优点：
- 基于 Raft，强一致性
- 支持租约自动续期
- 性能优于 ZooKeeper
- 云原生友好（Kubernetes 使用）

缺点：
- 相对较新，生态不如 ZK 成熟
```

## 方案对比

| 特性 | Redis | ZooKeeper | Etcd |
|------|-------|-----------|------|
| 性能 | 极高 | 中等 | 高 |
| 一致性 | 最终一致 | 强一致 | 强一致 |
| 防死锁 | 过期时间 | 临时节点 | 租约 |
| 可重入 | 需自己实现 | 支持 | 需自己实现 |
| 阻塞等待 | 需自己实现 | 支持 Watch | 支持 Watch |
| 可用性 | 高（Cluster） | 高 | 高 |
| 实现复杂度 | 中等 | 简单（Curator） | 中等 |

## 使用建议

| 场景 | 推荐方案 |
|------|----------|
| 追求极致性能 | Redis + Redisson |
| 强一致性要求 | ZooKeeper / Etcd |
| 长期持有锁 | ZooKeeper（避免 Redis 过期） |
| 高并发、短时锁 | Redis |
| 云原生环境 | Etcd |

## 面试常见问题

### Q1: Redis 分布式锁和 ZooKeeper 分布式锁怎么选？

**答**：
- Redis：性能高，适合高并发、短时锁场景，但需要处理时钟漂移和主从切换问题
- ZooKeeper：一致性好，天然防死锁，适合对一致性要求高的场景，但性能相对较低

### Q2: 什么是看门狗机制？

**答**：看门狗是 Redisson 实现的自动续期机制。加锁后启动定时任务，每 1/3 过期时间检查一次，如果业务还未完成，自动将锁的过期时间重置为初始值，防止业务执行过程中锁过期。

### Q3: 分布式锁的 key 如何设计？

**答**：
- 包含业务标识：lock:order:orderId
- value 包含客户端唯一标识：UUID + 线程ID
- 设置合理的过期时间（大于业务最大执行时间）
