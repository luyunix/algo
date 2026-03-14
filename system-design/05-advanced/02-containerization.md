# 容器化与编排

## Docker 基础

### 什么是容器

```
容器是轻量级的虚拟化技术，共享宿主机内核，隔离进程、网络、文件系统。

虚拟机 vs 容器：

虚拟机：
App A ──► Guest OS A ──► Hypervisor ──► Host OS ──► Hardware
App B ──► Guest OS B ──►

容器：
App A ──► Container A ──► Docker Engine ──► Host OS ──► Hardware
App B ──► Container B ──►
（共享内核）

优势：
- 启动快（秒级 vs 分钟级）
- 资源占用少
- 镜像体积小
- 性能接近原生
```

### 核心概念

| 概念 | 说明 |
|------|------|
| 镜像 (Image) | 只读模板，包含应用和依赖 |
| 容器 (Container) | 镜像的运行实例 |
| 仓库 (Registry) | 存储和分发镜像的服务 |
| Dockerfile | 定义镜像构建步骤 |

### Dockerfile 示例

```dockerfile
# 基础镜像
FROM openjdk:11-jdk-slim

# 工作目录
WORKDIR /app

# 复制文件
COPY target/app.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 常用命令

```bash
# 构建镜像
docker build -t myapp:1.0 .

# 运行容器
docker run -d -p 8080:8080 --name myapp myapp:1.0

# 查看容器
docker ps

# 停止容器
docker stop myapp

# 查看日志
docker logs -f myapp

# 进入容器
docker exec -it myapp /bin/bash
```

## Kubernetes 核心概念

### 架构

```
┌─────────────────────────────────────────┐
│           Master 节点                   │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐  │
│  │ API Server│ │Scheduler│ │   etcd   │  │
│  └─────────┘ └─────────┘ └─────────┘  │
│  ┌─────────┐                           │
│  │Controller│                          │
│  └─────────┘                           │
└─────────────────────────────────────────┘
                    │
    ┌───────────────┼───────────────┐
    ▼               ▼               ▼
┌─────────┐   ┌─────────┐   ┌─────────┐
│ Worker  │   │ Worker  │   │ Worker  │
│  Node 1 │   │  Node 2 │   │  Node 3 │
│┌───────┐│   │┌───────┐│   │┌───────┐│
││Pod    ││   ││Pod    ││   ││Pod    ││
││Pod    ││   ││Pod    ││   ││Pod    ││
│└───────┘│   │└───────┘│   │└───────┘│
└─────────┘   └─────────┘   └─────────┘
```

### 核心组件

| 组件 | 职责 |
|------|------|
| API Server | 暴露 Kubernetes API，处理请求 |
| etcd | 分布式键值存储，存储集群状态 |
| Scheduler | 调度 Pod 到合适的 Node |
| Controller | 维护集群状态，处理故障 |
| Kubelet | 在每个 Node 上运行，管理 Pod |
| Kube-Proxy | 网络代理，实现 Service |

### 核心资源

#### Pod

```
Pod 是 Kubernetes 的最小调度单位，可以包含一个或多个容器。

特点：
- 共享网络和存储
- 同一 Pod 的容器 localhost 互通
- 通常一个 Pod 一个主容器
```

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: myapp-pod
  labels:
    app: myapp
spec:
  containers:
  - name: myapp
    image: myapp:1.0
    ports:
    - containerPort: 8080
```

#### Deployment

```
管理 Pod 的副本集，支持滚动更新和回滚。
```

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
      - name: myapp
        image: myapp:1.0
        ports:
        - containerPort: 8080
```

#### Service

```
提供稳定的网络访问端点，将流量分发到 Pod。
```

```yaml
apiVersion: v1
kind: Service
metadata:
  name: myapp-service
spec:
  selector:
    app: myapp
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

Service 类型：
- ClusterIP：集群内部访问
- NodePort：通过节点端口暴露
- LoadBalancer：云厂商负载均衡
- ExternalName：DNS 别名

#### ConfigMap & Secret

```yaml
# ConfigMap：存储配置
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  database.url: "jdbc:mysql://db:3306/mydb"
  log.level: "INFO"

# Secret：存储敏感信息
apiVersion: v1
kind: Secret
metadata:
  name: db-secret
type: Opaque
data:
  username: YWRtaW4=  # base64 编码
  password: cGFzc3dvcmQ=
```

## 应用部署

### 滚动更新

```
Deployment 默认策略：
1. 创建新 ReplicaSet
2. 逐步增加新 Pod 数量
3. 逐步减少旧 Pod 数量
4. 旧 ReplicaSet 保留（用于回滚）
```

```bash
# 更新镜像
kubectl set image deployment/myapp myapp=myapp:2.0

# 查看更新状态
kubectl rollout status deployment/myapp

# 回滚
kubectl rollout undo deployment/myapp

# 查看历史
kubectl rollout history deployment/myapp
```

### 健康检查

```yaml
spec:
  containers:
  - name: myapp
    image: myapp:1.0
    livenessProbe:  # 存活检查，失败则重启
      httpGet:
        path: /health
        port: 8080
      initialDelaySeconds: 30
      periodSeconds: 10
    readinessProbe:  # 就绪检查，失败则从 Service 摘除
      httpGet:
        path: /ready
        port: 8080
      initialDelaySeconds: 5
      periodSeconds: 5
```

### 资源限制

```yaml
spec:
  containers:
  - name: myapp
    image: myapp:1.0
    resources:
      requests:  # 请求资源（调度依据）
        memory: "256Mi"
        cpu: "250m"  # 0.25 核
      limits:    # 资源上限
        memory: "512Mi"
        cpu: "500m"
```

## 面试要点

### Q1: Docker 和虚拟机有什么区别？

**答**：
- 虚拟机有完整的操作系统，容器共享宿主机内核
- 容器更轻量，启动更快，资源占用更少
- 虚拟机隔离性更强，安全性更高
- 容器适合微服务，虚拟机适合强隔离场景

### Q2: Kubernetes 中 Pod 和 Container 的关系？

**答**：
- Pod 是 Kubernetes 的最小调度单位
- 一个 Pod 可以包含一个或多个 Container
- 同一 Pod 的 Container 共享网络和存储（localhost 互通）
- 通常一个 Pod 只有一个主 Container，辅助 Container 做日志收集、监控等 Sidecar

### Q3: Kubernetes 如何实现服务发现？

**答**：
- Service 提供稳定的 ClusterIP/DNS 名称
- Kube-Proxy 维护 iptables/IPVS 规则，将流量转发到后端 Pod
- 通过 Label Selector 关联 Pod，自动感知 Pod 变化
- 支持环境变量和 DNS 两种方式提供服务发现
