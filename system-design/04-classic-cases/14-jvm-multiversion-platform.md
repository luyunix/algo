# JVM 多版本业务代码平台

基于 ClassLoader 隔离的业务代码快速上线平台，支持多版本同时在线、热部署、灰度发布。

## 1. 为什么需要多版本平台

```
传统发布的问题：
1. 发布周期长：编译、打包、部署、重启（30分钟+）
2. 回滚慢：发现问题后回滚需要重新部署
3. 无法灰度：全量发布，风险集中
4. 影响面广：重启导致服务中断

多版本平台的优势：
1. 秒级上线：代码推送后动态加载，无需重启
2. 即时回滚：路由切换即可回滚到旧版本
3. 细粒度灰度：按用户、按流量比例灰度
4. 零停机：旧版本在线时新版本已预热
```

## 2. 核心架构

```
┌─────────────────────────────────────────────────────────┐
│                     路由层 (Router)                      │
│  ┌──────────────────────────────────────────────────┐   │
│  │  版本路由策略                                      │   │
│  │  - 用户白名单 → 指定版本                           │   │
│  │  - 流量比例 → 按比例分配                           │   │
│  │  - 业务标识 → 按业务路由                           │   │
│  │  - 兜底策略 → 默认版本                             │   │
│  └──────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────┘
                         │
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Version 1   │  │  Version 2   │  │  Version 3   │
│ (ClassLoader │  │ (ClassLoader │  │ (ClassLoader │
│    v1)       │  │    v2)       │  │    v3)       │
│              │  │              │  │              │
│  UserBiz     │  │  UserBiz     │  │  UserBiz     │
│  OrderBiz    │  │  OrderBiz    │  │  OrderBiz    │
└──────────────┘  └──────────────┘  └──────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                  基础服务层 (Base)                        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │  数据库   │ │  Redis   │ │  MQ      │ │ 配置中心  │   │
│  │  (共享)   │ │  (共享)   │ │  (共享)  │ │ (共享)    │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
└─────────────────────────────────────────────────────────┘
```

## 3. ClassLoader 隔离机制

### 3.1 Java 类加载机制回顾

```
默认 ClassLoader 层级：

Bootstrap ClassLoader
        │
        ▼
Extension ClassLoader
        │
        ▼
Application ClassLoader (System)
        │
        ▼
    用户代码

双亲委派：
- 加载类时先委托父 ClassLoader
- 父加载器找不到才自己加载
- 保证核心类库一致性
```

### 3.2 自定义 ClassLoader 实现隔离

```java
public class VersionedClassLoader extends URLClassLoader {
    private final String version;
    private final long createTime;
    private final AtomicInteger activeCount = new AtomicInteger(0);

    public VersionedClassLoader(String version, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.version = version;
        this.createTime = System.currentTimeMillis();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 1. 先检查是否已加载
        Class<?> c = findLoadedClass(name);
        if (c != null) return c;

        // 2. 基础类库（java.*, javax.*）使用父加载器
        if (name.startsWith("java.") || name.startsWith("javax.")) {
            return super.loadClass(name, resolve);
        }

        // 3. 平台类（platform.*）使用父加载器
        if (name.startsWith("com.platform.")) {
            return super.loadClass(name, resolve);
        }

        // 4. 业务类自己加载（实现隔离）
        try {
            c = findClass(name);
            if (resolve) resolveClass(c);
            return c;
        } catch (ClassNotFoundException e) {
            // 找不到再委托父加载器
            return super.loadClass(name, resolve);
        }
    }

    public void incrementActive() {
        activeCount.incrementAndGet();
    }

    public void decrementActive() {
        activeCount.decrementAndGet();
    }

    public boolean isUnused() {
        return activeCount.get() == 0 &&
               System.currentTimeMillis() - createTime > TimeUnit.MINUTES.toMillis(5);
    }

    @Override
    public void close() throws IOException {
        super.close();
        System.out.println("ClassLoader for version " + version + " closed");
    }
}
```

### 3.3 版本管理器

```java
@Service
public class VersionManager {
    // 版本号 -> ClassLoader 映射
    private final ConcurrentHashMap<String, VersionedClassLoader> versionLoaders =
        new ConcurrentHashMap<>();

    // 版本号 -> 业务实例映射
    private final ConcurrentHashMap<String, Map<String, Object>> versionInstances =
        new ConcurrentHashMap<>();

    // 代码仓库路径
    @Value("${biz.code.base-path}")
    private String codeBasePath;

    /**
     * 加载新版本
     */
    public synchronized void loadVersion(String version, String codePath) throws Exception {
        // 1. 编译代码（如果是源码）
        File classDir = compileIfNecessary(codePath);

        // 2. 创建 ClassLoader
        URL[] urls = {classDir.toURI().toURL()};
        VersionedClassLoader classLoader = new VersionedClassLoader(version, urls,
            this.getClass().getClassLoader());

        // 3. 加载业务类并实例化
        Map<String, Object> instances = new HashMap<>();
        List<String> bizClasses = scanBizClasses(classDir);

        for (String className : bizClasses) {
            Class<?> clazz = classLoader.loadClass(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // 注入基础服务
            injectBaseServices(instance);

            instances.put(className, instance);
        }

        // 4. 预热（可选）
        warmup(instances);

        // 5. 注册版本
        versionLoaders.put(version, classLoader);
        versionInstances.put(version, instances);

        System.out.println("Version " + version + " loaded successfully");
    }

    /**
     * 获取指定版本的业务实例
     */
    @SuppressWarnings("unchecked")
    public <T> T getInstance(String version, String className) {
        Map<String, Object> instances = versionInstances.get(version);
        if (instances == null) {
            throw new VersionNotFoundException("Version " + version + " not found");
        }

        Object instance = instances.get(className);
        if (instance == null) {
            throw new BizClassNotFoundException("Class " + className + " not found in version " + version);
        }

        // 增加活跃计数
        VersionedClassLoader loader = versionLoaders.get(version);
        if (loader != null) {
            loader.incrementActive();
        }

        return (T) instance;
    }

    /**
     * 卸载版本
     */
    public synchronized void unloadVersion(String version) {
        VersionedClassLoader loader = versionLoaders.get(version);
        if (loader == null) return;

        // 检查是否还有活跃请求
        if (!loader.isUnused()) {
            throw new VersionInUseException("Version " + version + " is still in use");
        }

        // 移除引用
        versionInstances.remove(version);
        versionLoaders.remove(version);

        // 关闭 ClassLoader（触发类卸载）
        try {
            loader.close();
        } catch (IOException e) {
            log.error("Failed to close ClassLoader for version " + version, e);
        }

        // 建议 GC
        System.gc();
    }

    /**
     * 定时清理过期版本
     */
    @Scheduled(fixedRate = 60000) // 每分钟检查
    public void cleanupUnusedVersions() {
        for (Map.Entry<String, VersionedClassLoader> entry : versionLoaders.entrySet()) {
            if (entry.getValue().isUnused()) {
                unloadVersion(entry.getKey());
            }
        }
    }
}
```

## 4. 版本路由策略

### 4.1 路由上下文

```java
public class RouteContext {
    private String userId;
    private String bizCode;
    private String source;
    private Map<String, Object> attributes;

    // ThreadLocal 存储当前请求的版本
    private static final ThreadLocal<String> CURRENT_VERSION = new ThreadLocal<>();

    public static void setCurrentVersion(String version) {
        CURRENT_VERSION.set(version);
    }

    public static String getCurrentVersion() {
        return CURRENT_VERSION.get();
    }

    public static void clear() {
        CURRENT_VERSION.remove();
    }
}
```

### 4.2 路由策略实现

```java
public interface RouteStrategy {
    String route(RouteContext ctx);
}

/**
 * 白名单路由：指定用户使用新版本
 */
@Component
public class WhitelistRouteStrategy implements RouteStrategy {
    @Autowired
    private VersionConfigRepository configRepo;

    @Override
    public String route(RouteContext ctx) {
        String userId = ctx.getUserId();

        // 查询用户的版本白名单配置
        VersionConfig config = configRepo.findByUserId(userId);
        if (config != null && config.getAssignedVersion() != null) {
            return config.getAssignedVersion();
        }

        return null; // 未命中，继续下一个策略
    }
}

/**
 * 流量比例路由
 */
@Component
public class TrafficRatioRouteStrategy implements RouteStrategy {
    @Autowired
    private VersionRouteRepository routeRepo;

    private final Random random = new Random();

    @Override
    public String route(RouteContext ctx) {
        // 根据业务线获取路由配置
        List<VersionRoute> routes = routeRepo.findByBizCode(ctx.getBizCode());

        int total = routes.stream().mapToInt(VersionRoute::getRatio).sum();
        int hit = random.nextInt(total);

        int current = 0;
        for (VersionRoute route : routes) {
            current += route.getRatio();
            if (hit < current) {
                return route.getVersion();
            }
        }

        return routes.get(routes.size() - 1).getVersion(); // 兜底
    }
}

/**
 * 业务标识路由
 */
@Component
public class BizCodeRouteStrategy implements RouteStrategy {
    @Override
    public String route(RouteContext ctx) {
        // 不同业务线固定使用不同版本
        String bizCode = ctx.getBizCode();

        switch (bizCode) {
            case "ORDER":
                return "v2.0"; // 订单业务用新版本
            case "USER":
                return "v1.5"; // 用户业务用旧版本
            default:
                return null;
        }
    }
}
```

### 4.3 路由过滤器（Spring Boot 集成）

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class VersionRouteFilter implements Filter {
    @Autowired
    private List<RouteStrategy> strategies;

    @Autowired
    private VersionManager versionManager;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            // 1. 构建路由上下文
            RouteContext ctx = buildContext(httpRequest);

            // 2. 执行路由策略链
            String version = null;
            for (RouteStrategy strategy : strategies) {
                version = strategy.route(ctx);
                if (version != null) break;
            }

            // 3. 兜底：使用默认版本
            if (version == null) {
                version = "v1.0";
            }

            // 4. 检查版本是否存在
            if (!versionManager.exists(version)) {
                version = "v1.0"; // 回退到稳定版本
            }

            // 5. 设置当前线程版本
            RouteContext.setCurrentVersion(version);

            // 6. 在响应头中标注版本（方便调试）
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setHeader("X-Biz-Version", version);

            chain.doFilter(request, response);

        } finally {
            // 清理 ThreadLocal
            RouteContext.clear();
        }
    }

    private RouteContext buildContext(HttpServletRequest request) {
        RouteContext ctx = new RouteContext();
        ctx.setUserId(request.getHeader("X-User-Id"));
        ctx.setBizCode(request.getHeader("X-Biz-Code"));
        ctx.setSource(request.getHeader("X-Source"));
        return ctx;
    }
}
```

## 5. 业务代码调用示例

```java
@RestController
@RequestMapping("/api/biz")
public class BizController {

    @Autowired
    private VersionManager versionManager;

    @GetMapping("/process")
    public Response process(@RequestParam String param) {
        // 获取当前请求路由到的版本
        String version = RouteContext.getCurrentVersion();

        // 获取该版本的业务实例
        BizService bizService = versionManager.getInstance(version,
            "com.biz.UserServiceImpl");

        // 执行业务逻辑
        try {
            Object result = bizService.process(param);
            return Response.success(result);
        } finally {
            // 减少活跃计数
            VersionedClassLoader loader = versionManager.getLoader(version);
            if (loader != null) {
                loader.decrementActive();
            }
        }
    }
}

/**
 * 业务接口（平台定义，各版本实现）
 */
public interface BizService {
    Object process(String param);
}

/**
 * 业务实现（用户编写，动态加载）
 */
public class UserServiceImpl implements BizService {
    @Autowired  // 平台注入
    private UserDAO userDAO;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Object process(String param) {
        // 业务逻辑
        return userDAO.query(param);
    }
}
```

## 6. 关键技术问题

### 6.1 静态变量隔离

```
问题：
- 每个版本的类有自己的静态变量
- 如果静态变量是共享资源（如连接池），会重复创建

解决方案：
1. 基础服务由平台管理，通过依赖注入提供给业务类
2. 业务代码只写业务逻辑，不管理资源
3. 平台提供统一的 ServiceLocator

代码：
public class ServiceLocator {
    private static final Map<String, Object> services = new ConcurrentHashMap<>();

    public static void register(String name, Object service) {
        services.put(name, service);
    }

    public static <T> T getService(String name) {
        return (T) services.get(name);
    }
}

// 在 VersionManager 初始化时注册基础服务
ServiceLocator.register("userDAO", userDAO);
ServiceLocator.register("redisTemplate", redisTemplate);
```

### 6.2 类卸载与内存泄漏

```
问题：
- ClassLoader 只有在没有引用时才能被 GC
- 如果有线程、连接未关闭，会导致类无法卸载
- 反复加载卸载可能导致 PermGen/Metaspace OOM

解决方案：
1. 跟踪活跃请求数（activeCount）
2. 等待所有请求完成后再卸载
3. 使用 try-finally 确保计数正确
4. 定时检查并强制 GC
5. 限制版本数量（如最多保留 5 个版本）
```

### 6.3 线程上下文传递

```java
/**
 * 异步场景下的版本传递
 */
public class VersionAwareExecutor {
    private ExecutorService executor;

    public void submit(Runnable task) {
        // 捕获当前版本
        String version = RouteContext.getCurrentVersion();

        executor.submit(() -> {
            // 在子线程中恢复版本上下文
            RouteContext.setCurrentVersion(version);
            try {
                task.run();
            } finally {
                RouteContext.clear();
            }
        });
    }
}

// 支持 CompletableFuture
public class VersionAwareCompletableFuture {
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        String version = RouteContext.getCurrentVersion();

        return CompletableFuture.supplyAsync(() -> {
            RouteContext.setCurrentVersion(version);
            try {
                return supplier.get();
            } finally {
                RouteContext.clear();
            }
        });
    }
}
```

## 7. 平台控制台设计

```
┌─────────────────────────────────────────────────────────┐
│                   版本管理控制台                         │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  [上传代码包]  [发布新版本]  [回滚]  [版本对比]           │
│                                                          │
│  版本列表：                                               │
│  ┌───────────────────────────────────────────────────┐  │
│  │ 版本号 │ 状态   │ 流量比例 │ 操作                 │  │
│  ├───────────────────────────────────────────────────┤  │
│  │ v3.0   │ 运行中 │  10%     │ [扩量] [下线] [查看] │  │
│  │ v2.5   │ 运行中 │  90%     │ [缩容] [下线]        │  │
│  │ v2.0   │ 待卸载 │   0%     │ [恢复] [强制卸载]    │  │
│  └───────────────────────────────────────────────────┘  │
│                                                          │
│  灰度配置：                                               │
│  ┌───────────────────────────────────────────────────┐  │
│  │ 用户白名单：user1, user2, user3                   │  │
│  │ 业务线：ORDER (100% v3.0), USER (100% v2.5)       │  │
│  │ 地域：北京 (50% v3.0), 上海 (10% v3.0)            │  │
│  └───────────────────────────────────────────────────┘  │
│                                                          │
│  实时监控：                                               │
│  ┌───────────────────────────────────────────────────┐  │
│  │ 版本   │ QPS   │ 延迟(P99) │ 错误率 │ 活跃请求    │  │
│  ├───────────────────────────────────────────────────┤  │
│  │ v3.0   │ 1200  │   15ms    │ 0.1%   │    45       │  │
│  │ v2.5   │ 9800  │   12ms    │ 0.05%  │   320       │  │
│  └───────────────────────────────────────────────────┘  │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

## 8. 面试要点

### Q1: 多个 ClassLoader 加载同一个类，JVM 怎么区分？

**答**：
- JVM 中类的唯一标识是：**ClassLoader + 类全名**
- 不同的 ClassLoader 加载的同一个类，在 JVM 中是不同的 Class 对象
- 它们之间不能互相转换（ClassCastException）
- 这就是实现多版本隔离的基础

### Q2: 怎么防止内存泄漏？类能正常卸载吗？

**答**：
1. 跟踪每个 ClassLoader 的活跃请求数（activeCount）
2. 卸载前检查活跃数是否为 0，且超过冷却时间
3. 确保没有线程、连接持有类的引用
4. 调用 ClassLoader.close() 释放资源
5. 触发 System.gc() 建议卸载
6. 限制同时存在的版本数量

### Q3: 如果业务代码里有静态缓存，怎么保证多版本间不冲突？

**答**：
- 每个版本的静态缓存是独立的（因为类隔离）
- 但这可能导致内存占用过大
- 解决方案：
  1. 静态缓存只存业务数据，不存大对象
  2. 共享缓存通过平台提供的 ServiceLocator 获取
  3. 平台提供统一的缓存服务（如 Caffeine 实例）
  4. 业务代码通过依赖注入使用缓存，不自己创建

### Q4: 发布新版本后，正在执行的请求会受影响吗？

**答**：
- 不会受影响，因为请求绑定了当时的版本上下文
- 新请求按新的路由规则分配到新版本
- 旧版本会等待所有活跃请求完成后才卸载
- 通过 ThreadLocal 保证同一次请求内始终使用同一版本

### Q5: 如果新版本有问题，怎么快速回滚？

**答**：
1. 在控制台修改路由策略（白名单、流量比例）
2. 将流量切回旧版本（毫秒级生效）
3. 新版本不再接收新请求
4. 等待活跃请求降为 0 后卸载
5. 整个过程不需要重启 JVM，秒级完成

## 9. 与业界方案对比

| 方案 | 原理 | 优点 | 缺点 |
|------|------|------|------|
| **本方案** | ClassLoader 隔离 | 轻量、秒级发布、细粒度灰度 | 需要处理类隔离问题 |
| OSGi | 模块化框架 | 成熟生态 | 重量级、学习成本高 |
| Kubernetes | 容器隔离 | 完全隔离、资源独立 | 部署慢、资源占用大 |
| JVM-Sandbox | Instrument 插桩 | 无侵入 | 只能修改方法体，不能加字段 |
| DCEVM/JRebel | JVM 热替换 | 开发友好 | 不适合生产多版本 |
