# 服务发现与注册

## 为什么需要服务发现

在微服务架构中，服务实例动态变化（扩缩容、故障重启），需要一种机制让服务消费者找到服务提供者。

```
传统方式（硬编码）：
UserService ──► http://192.168.1.10:8080
问题：IP 变化需要修改代码重启

服务发现：
UserService ──► 服务注册中心 ──► 获取可用实例列表
                    │
              ┌─────┴─────┐
              ▼           ▼
        [实例1]       [实例2]
        192.168.1.10  192.168.1.11
```

## 核心功能

| 功能 | 说明 |
|------|------|
| 服务注册 | 服务启动时向注册中心注册自己的信息 |
| 服务发现 | 消费者从注册中心获取服务实例列表 |
| 健康检查 | 检测服务实例是否可用 |
| 负载均衡 | 在多个实例间分配请求 |
| 故障转移 | 自动剔除故障实例 |

## 服务注册模式

### 客户端发现 (Client-Side)

```
服务消费者自己从注册中心获取实例列表，选择目标：

┌─────────┐     1. 注册      ┌─────────┐
│ Service │ ───────────────►│Registry │
│   A     │                 │ (Eureka)│
└─────────┘                 └────┬────┘
                                 │
┌─────────┐     2. 获取列表     │
│ Service │◄────────────────────┘
│   B     │
└────┬────┘
     │ 3. 直接调用
     ▼
┌─────────┐
│ Service │
│   A     │
└─────────┘

代表：Eureka、Nacos、Consul + Ribbon
优点：简单直接，无中间代理
缺点：客户端需要集成 SDK
```

### 服务端发现 (Server-Side)

```
通过负载均衡器（代理）转发请求：

Client ──► LB/Proxy ──► 路由到实例
             │
       从注册中心获取实例

代表：Kubernetes Service、AWS ELB、Nginx + Consul Template
优点：客户端无感知，语言无关
缺点：增加网络跳数，代理可能成为瓶颈
```

## 常见注册中心对比

| 特性 | Eureka | Consul | ZooKeeper | Nacos | etcd |
|------|--------|--------|-----------|-------|------|
| 开发方 | Netflix | HashiCorp | Apache | 阿里 | CNCF |
| 一致性 | AP | CP | CP | AP+CP | CP |
| 健康检查 | 心跳 | 多种方式 | 临时节点 | 心跳+探测 | 租约 |
| 多数据中心 | 支持 | 原生支持 | 需配置 | 支持 | - |
| 性能 | 高 | 中 | 中 | 高 | 高 |
| 生态 | Spring Cloud | 通用 | Dubbo | Spring Cloud/Dubbo | K8s |
| 控制台 | 简单 | 完善 | 无 | 完善 | 第三方 |

## Eureka 详解

### 架构

```
┌─────────────────────────────────────┐
│         Eureka Server 集群          │
│  ┌─────────┐      ┌─────────┐      │
│  │ Node 1  │◄────►│ Node 2  │      │
│  └─────────┘      └─────────┘      │
└─────────────────────────────────────┘
         ▲                  ▲
         │ 注册/心跳/获取    │
         │                  │
   ┌─────┴─────┐      ┌─────┴─────┐
   │ Service A │      │ Service B │
   │ (Provider)│      │(Consumer) │
   └───────────┘      └───────────┘
```

### 关键特性

```
AP 架构：
- 优先保证可用性
- 节点间数据异步复制
- 某节点宕机不影响服务发现

自我保护机制：
- 网络分区时，保留过期实例
- 防止误删健康实例
- 可配置开启/关闭

心跳机制：
- 客户端每 30 秒发送心跳
- 90 秒未收到心跳，剔除实例
```

### 代码示例

```java
// 服务注册（自动）
@EnableEurekaClient
@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

// 服务发现 + 调用
@RestController
public class OrderController {
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/order/{id}")
    public Order getOrder(@PathVariable Long id) {
        // 通过服务名调用，自动负载均衡
        User user = restTemplate.getForObject(
            "http://USER-SERVICE/user/" + id,
            User.class
        );
        // ...
    }
}
```

## Consul 详解

### 架构

```
┌─────────────────────────────────────┐
│           Consul 集群               │
│  ┌─────────┐      ┌─────────┐      │
│  │ Server  │◄────►│ Server  │      │
│  │  (Leader)│      │ (Follower)    │
│  └─────────┘      └─────────┘      │
└─────────────────────────────────────┘
         ▲                  ▲
         │ Gossip 协议       │
         │                  │
   ┌─────┴─────┐      ┌─────┴─────┐
   │  Client   │      │  Client   │
   │  (Agent)  │      │  (Agent)  │
   └───────────┘      └───────────┘
```

### 关键特性

```
CP 架构：
- 基于 Raft 协议
- 强一致性
- Leader 选举期间不可用

健康检查：
- Script check：执行脚本
- HTTP check：HTTP 请求
- TCP check：TCP 连接
- TTL check：客户端主动上报
- gRPC check：gRPC 健康检查

多数据中心：
- WAN Gossip：跨数据中心通信
- 本地优先，失败时跨中心
```

## Nacos 详解

### 特点

```
阿里开源，功能最全面：
- 服务发现（AP/CP 可切换）
- 配置中心
- 服务管理控制台
- 权重、流量控制

数据模型：
Namespace ──► Group ──► Service ──► Cluster ──► Instance
（环境隔离）  （分组）    （服务名）    （集群）     （实例）
```

### 配置示例

```yaml
# application.yml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: dev
        group: DEFAULT_GROUP
      config:
        server-addr: 127.0.0.1:8848
        file-extension: yaml
```

## 健康检查机制

### 客户端心跳

```
服务主动定期向注册中心发送心跳：

Eureka：HTTP PUT /eureka/apps/{appName}/{instanceId}
Nacos：HTTP PUT /nacos/v1/ns/instance/beat
间隔：5秒
超时：15秒剔除
```

### 服务端探测

```
注册中心主动检测服务健康：

Consul：
- HTTP：GET /health，期望 200
- TCP：尝试建立连接
- Script：执行自定义脚本

Nacos：
- HTTP 探测
- MySQL 探测
```

### 长连接心跳

```
gRPC/Nacos 2.0：
- 建立长连接（gRPC stream）
- 双向心跳检测
- 更实时、更节省资源
```

## 服务发现的最佳实践

### 1. 高可用部署

```
Eureka：
- 至少 3 节点
- 客户端配置多个地址
- 开启自我保护（生产环境）

Consul：
- 3-5 个 Server 节点
- 每个服务节点部署 Client Agent
```

### 2. 优雅上下线

```
服务下线：
1. 停止接收新请求（平滑关闭）
2. 向注册中心注销
3. 等待正在处理的请求完成
4. 关闭进程

Kubernetes：
- PreStop Hook：注销服务
- terminationGracePeriodSeconds：等待处理完成
```

### 3. 缓存机制

```
客户端缓存服务列表：
- 减少注册中心压力
- 网络故障时有兜底
- 定期更新（Eureka 默认 30 秒）
```

## 面试常见问题

### Q1: Eureka 和 Consul 的区别？

**答**：
- Eureka 是 AP 架构，优先保证可用性，适合网络分区频繁的环境；Consul 是 CP 架构，保证强一致性
- Eureka 只有心跳检测；Consul 支持多种健康检查方式
- Consul 原生支持多数据中心；Eureka 需要额外配置
- Eureka 是 Spring Cloud 生态；Consul 更通用

### Q2: 服务注册中心如何实现高可用？

**答**：
- 多节点部署，避免单点故障
- 客户端配置多个注册中心地址
- 数据多副本同步（CP 系统）或异步复制（AP 系统）
- 网络分区时，AP 系统保留过期数据，CP 系统选举新 Leader

### Q3: 如果注册中心挂了，服务还能互相调用吗？

**答**：
- 客户端有本地缓存，可以基于缓存继续调用
- 但新服务无法注册，故障实例无法剔除
- 所以注册中心需要高可用部署
