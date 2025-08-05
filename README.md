# SpringBoot SQL监控项目

通过字节码增强技术实现对SpringBoot中所有SQL语句的监控打印，包括SQL语句和执行时间。

## 项目特性

- ✅ **字节码增强监控**: 使用Java Agent和ASM技术实现无侵入式SQL监控
- ✅ **SQL语句打印**: 实时打印所有执行的SQL语句
- ✅ **执行时间统计**: 精确记录每个SQL的执行时间
- ✅ **异常监控**: 监控SQL执行异常并记录错误信息
- ✅ **统计信息**: 提供SQL执行统计信息（总数、成功率、平均时间等）
- ✅ **REST API演示**: 提供完整的REST API来演示各种SQL操作

## 技术栈

- **Spring Boot 2.7.11**: 主框架
- **ByteBuddy**: 字节码操作库
- **ASM**: 字节码分析和修改
- **H2 Database**: 内存数据库用于演示
- **JDBC Template**: 数据库操作
- **Lombok**: 简化代码

## 项目结构

```
src/main/java/com/lzx/learn/
├── agent/                    # Java Agent相关
│   ├── SqlMonitorAgent.java      # 主Agent类
│   ├── AdvancedSqlMonitor.java   # 高级SQL监控器
│   └── SqlMonitorManager.java    # SQL监控管理器
└── systemdesign/            # SpringBoot应用
    ├── SqlMonitorApplication.java # 主应用类
    ├── config/
    │   └── DatabaseConfig.java    # 数据库配置
    ├── controller/
    │   └── UserController.java    # REST API控制器
    ├── entity/
    │   └── User.java             # 用户实体
    └── service/
        └── UserService.java      # 用户服务
```

## 快速开始

### 1. 编译项目

```bash
mvn clean package
```

### 2. 启动应用（带SQL监控）

```bash
# 使用启动脚本
chmod +x start-with-agent.sh
./start-with-agent.sh

# 或手动启动
java -javaagent:target/system-design-0.0.1-SNAPSHOT.jar \
     -jar target/system-design-0.0.1-SNAPSHOT.jar
```

### 3. 测试API

应用启动后，可以访问以下地址：

- **应用主页**: http://localhost:8080
- **H2数据库控制台**: http://localhost:8080/h2-console
- **API文档**: http://localhost:8080/api/users

#### 测试命令示例：

```bash
# 获取所有用户
curl http://localhost:8080/api/users

# 根据ID获取用户
curl http://localhost:8080/api/users/1

# 搜索用户
curl http://localhost:8080/api/users/search?name=张

# 创建用户
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"测试用户","email":"test@example.com","age":25}'

# 演示各种SQL操作
curl -X POST http://localhost:8080/api/users/demo

# 批量创建用户
curl -X POST http://localhost:8080/api/users/batch
```

## 监控输出示例

启动应用后，你会在控制台看到类似以下的SQL监控输出：

```
SQL监控Agent已启动...
初始化SQL监控管理器...
SQL监控管理器初始化完成

=== SQL监控开始 [ID: 1] ===
类: PreparedStatement
方法: executeQuery
SQL: SELECT * FROM users
执行时间: 15ms
=== SQL监控结束 ===

=== SQL监控开始 [ID: 2] ===
类: PreparedStatement
方法: executeQuery
SQL: SELECT * FROM users WHERE id = ?
执行时间: 8ms
=== SQL监控结束 ===
```

## 核心实现原理

### 1. Java Agent

`SqlMonitorAgent` 是主要的Java Agent类，在JVM启动时通过 `premain` 方法注册字节码转换器。

### 2. 字节码增强

使用ByteBuddy和ASM技术对JDBC相关的类进行字节码增强：

- 拦截 `Statement.execute()`, `executeQuery()`, `executeUpdate()` 等方法
- 在方法执行前后插入监控代码
- 记录执行时间和SQL语句

### 3. 监控管理器

`SqlMonitorManager` 提供统一的监控管理功能：

- 记录SQL执行信息
- 统计执行时间
- 提供监控统计信息
- 支持启用/禁用监控

## 配置说明

### Java Agent配置

在 `pom.xml` 中配置了Maven Shade插件来创建包含Agent的fat JAR：

```xml
<transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
    <manifestEntries>
        <Premain-Class>com.lzx.learn.agent.SqlMonitorAgent</Premain-Class>
        <Can-Redefine-Classes>true</Can-Redefine-Classes>
        <Can-Retransform-Classes>true</Can-Retransform-Classes>
    </manifestEntries>
</transformer>
```

### 监控范围

当前监控的类和方法：

- **类**: `java.sql.Statement`, `java.sql.PreparedStatement`, `java.sql.CallableStatement`
- **方法**: `execute()`, `executeQuery()`, `executeUpdate()`, `executeBatch()`

## 扩展功能

### 1. 添加更多监控点

可以在 `AdvancedSqlMonitor` 中添加更多需要监控的类和方法。

### 2. 自定义监控输出

可以修改 `SqlMonitorManager` 中的日志输出格式，或者将监控信息发送到外部系统。

### 3. 性能优化

- 使用异步日志记录
- 添加监控开关
- 实现监控数据持久化

## 注意事项

1. **性能影响**: 字节码增强会带来一定的性能开销，建议在生产环境中谨慎使用
2. **兼容性**: 确保与目标JDK版本兼容
3. **安全性**: 在生产环境中使用时需要考虑安全性问题

## 故障排除

### 常见问题

1. **Agent未启动**: 检查JVM参数是否正确设置
2. **监控不生效**: 确认目标类是否在监控范围内
3. **性能问题**: 考虑调整监控粒度或使用异步处理

### 调试技巧

1. 启用详细日志：`-Dlogging.level.com.lzx.learn=DEBUG`
2. 检查字节码转换：添加更多调试输出
3. 使用JVM工具分析性能影响

## 许可证

MIT License
