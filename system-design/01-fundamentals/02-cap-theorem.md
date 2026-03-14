# CAP 定理

## 定理内容

CAP 定理指出：**分布式系统不可能同时满足一致性（Consistency）、可用性（Availability）和分区容错性（Partition Tolerance）这三个特性。**

```
        ┌─────────┐
       /    C     \
      / Consistency\
     /   (一致性)   \
    ├───────────────┤
   A │               │ P
Availability    Partition
(可用性)        Tolerance
(分区容错性)      \
    \              /
     \            /
      \__________/
```

## 三个特性的定义

### Consistency（一致性）
所有节点在同一时间看到的数据是一致的。即写入成功后，所有读取都能获取最新值。

```
场景：用户 A 写入数据 X=1

C 满足时：
  用户 B 读取 X → 得到 1
  用户 C 读取 X → 得到 1

C 不满足时（最终一致）：
  用户 B 读取 X → 可能得到旧值 0
  等待一段时间后 → 得到 1
```

### Availability（可用性）
每个请求都能在合理时间内获得响应，不保证数据最新。

```
场景：部分节点故障

A 满足时：
  请求仍然被处理，可能返回旧数据

A 不满足时：
  请求被阻塞或返回错误
```

### Partition Tolerance（分区容错性）
系统在网络分区（节点间通信中断）时仍能继续运行。

**关键认知：网络分区一定会发生，所以 P 是必须满足的。**
因此 CAP 定理实际上是：**在出现分区时，只能在 C 和 A 之间做选择。**

## CP vs AP 系统

### CP 系统（一致性优先）
- **特点**：放弃可用性，保证数据一致性
- **场景**：金融交易、库存扣减
- **实现**：ZooKeeper、etcd、HBase、Consul

```
用户请求 ──► 系统检测到分区 ──► 拒绝写入/读取
                              （保证不会读到脏数据）
```

### AP 系统（可用性优先）
- **特点**：放弃强一致性，保证可用性
- **场景**：社交网络、内容展示
- **实现**：Cassandra、DynamoDB、Eureka

```
用户请求 ──► 系统检测到分区 ──► 继续处理请求
                              （可能返回旧数据）
```

## 实际案例分析

| 场景 | 选择 | 原因 |
|------|------|------|
| 银行转账 | CP | 钱不能多也不能少，必须强一致 |
| 电商库存扣减 | CP | 超卖会导致业务损失 |
| 社交网络 Feed | AP | 暂时看到旧数据可以接受 |
| 用户头像更新 | AP | 延迟几秒看到新头像无影响 |
| 即时通讯已读状态 | AP | 最终一致即可 |

## CAP 的延伸：PACELC 定理

PACELC 定理是 CAP 的扩展，指出**即使没有分区，也要在延迟（Latency）和一致性（Consistency）之间做选择。**

```
If there is a Partition (P),
  how to tradeoff between Availability (A) and Consistency (C);
Else (E) when system is running normally,
  how to tradeoff between Latency (L) and Consistency (C).
```

| 系统 | 分区时 | 正常时 |
|------|--------|--------|
| DynamoDB | A | L |
| MongoDB | C | C |
| Cassandra | A | L |
