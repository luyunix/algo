# 系统安全

## 常见安全威胁

### OWASP Top 10

| 排名 | 威胁 | 说明 |
|------|------|------|
| 1 | 注入攻击 | SQL、命令、LDAP 注入 |
| 2 | 失效身份认证 | 弱密码、会话劫持 |
| 3 | 敏感数据泄露 | 明文存储、传输未加密 |
| 4 | XML外部实体 | XXE 攻击 |
| 5 | 失效访问控制 | 越权访问 |
| 6 | 安全配置错误 | 默认配置、错误处理泄露信息 |
| 7 | XSS | 跨站脚本攻击 |
| 8 | 不安全的反序列化 | 远程代码执行 |
| 9 | 使用已知漏洞组件 | 过时依赖 |
| 10 | 日志监控不足 | 无法及时发现攻击 |

## 防护策略

### 1. 输入验证

```
原则：永远不要信任用户输入

做法：
- 白名单验证：只允许已知安全的输入
- 参数化查询：防止 SQL 注入
- 转义输出：防止 XSS
- 限制输入长度：防止缓冲区溢出
```

**SQL 注入防护**：
```java
// 错误：拼接 SQL
String sql = "SELECT * FROM users WHERE name = '" + name + "'";

// 正确：参数化查询
String sql = "SELECT * FROM users WHERE name = ?";
PreparedStatement stmt = connection.prepareStatement(sql);
stmt.setString(1, name);
```

### 2. 身份认证

```
密码策略：
- 最小长度 8 位
- 包含大小写、数字、特殊字符
- 定期更换
- 不使用常见密码

多因素认证 (MFA)：
- 知识因子：密码
- 拥有因子：手机、U盾
- 生物因子：指纹、人脸

JWT (JSON Web Token)：
- 无状态认证
- 包含签名防篡改
- 设置合理的过期时间
```

### 3. 访问控制

```
RBAC (基于角色的访问控制)：
用户 ──► 角色 ──► 权限

ABAC (基于属性的访问控制)：
根据用户属性、资源属性、环境属性动态决策

最小权限原则：
只授予完成任务所需的最小权限
```

### 4. 传输安全

```
HTTPS：
- TLS 加密传输
- 防止中间人攻击
- 证书固定 (Certificate Pinning)

HSTS (HTTP Strict Transport Security)：
- 强制使用 HTTPS
- 防止 SSL 剥离攻击
```

### 5. 会话安全

```
会话 ID：
- 随机生成，长度足够
- 定期更换
- 安全存储（HttpOnly Cookie）

防护：
- CSRF Token：防止跨站请求伪造
- SameSite Cookie：限制第三方 Cookie
- 会话超时：自动失效
```

## 安全工具

### 依赖安全

```
OWASP Dependency Check：
- 扫描项目依赖的已知漏洞
- 集成到 CI/CD 流程

Snyk：
- 自动检测和修复漏洞
- 持续监控新漏洞
```

### 代码安全扫描

```
SonarQube：
- 代码质量分析
- 安全漏洞检测
- 技术债务评估

Checkmarx / Fortify：
- 静态应用安全测试 (SAST)
- 发现潜在安全漏洞
```

### 渗透测试

```
Burp Suite：
- Web 应用渗透测试
- 拦截和修改请求

OWASP ZAP：
- 开源 Web 应用扫描器
- 自动发现安全漏洞
```

## 安全设计原则

```
1. 纵深防御 (Defense in Depth)
   - 多层防护，单层被突破仍有其他层

2. 最小权限 (Least Privilege)
   - 只授予必要权限

3. 零信任 (Zero Trust)
   - 永不信任，始终验证
   - 即使内网也要认证

4. 安全默认 (Secure by Default)
   - 默认安全配置
   - 默认关闭不必要功能

5. 故障安全 (Fail Secure)
   - 故障时进入安全状态
   - 默认拒绝访问
```

## 面试要点

### Q1: 如何防止 SQL 注入？

**答**：
1. 使用参数化查询（PreparedStatement）
2. 使用 ORM 框架
3. 输入验证和过滤
4. 最小权限原则（数据库账号只授予必要权限）
5. 使用 WAF 防护

### Q2: 什么是 CSRF 攻击？如何防护？

**答**：
CSRF（跨站请求伪造）是攻击者诱导用户在已认证的网站上执行非预期操作。

防护方法：
1. CSRF Token：表单提交时携带服务端生成的 Token
2. SameSite Cookie：限制 Cookie 在跨站请求中的发送
3. 验证 Referer/Origin 头
4. 敏感操作二次确认

### Q3: 如何保证用户密码安全？

**答**：
1. 不存储明文密码，存储哈希值
2. 使用强哈希算法（bcrypt、scrypt、Argon2），避免 MD5/SHA1
3. 加盐（Salt），每个用户独立盐值
4. 密码策略：最小长度、复杂度要求
5. 使用 HTTPS 传输密码
