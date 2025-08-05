# MyBatis SQL Logger

基于Spring Boot BeanPostProcessor的MyBatis SQL日志增强jar包，支持通过配置开关来控制SQL日志的打印功能。

## 功能特性

- ✅ 基于BeanPostProcessor自动配置，无需手动注册
- ✅ 支持配置开关控制功能启用/禁用
- ✅ 支持SQL语句格式化
- ✅ 支持SQL参数打印
- ✅ 支持SQL执行时间统计
- ✅ 支持慢查询警告
- ✅ 支持SQL类型显示
- ✅ 支持结果行数统计
- ✅ 支持多种日志级别
- ✅ 支持Spring Boot自动配置

## 快速开始

### 1. 添加依赖

在你的Spring Boot项目中添加依赖：

```xml
<dependency>
    <groupId>com.lzx.learn</groupId>
    <artifactId>mybatis-sql-logger</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 配置启用

在`application.yml`中启用SQL日志功能：

```yaml
mybatis:
  sql:
    logger:
      # 启用SQL日志打印功能
      enabled: true
      # 是否打印SQL参数
      show-parameters: true
      # 是否打印SQL执行时间
      show-execution-time: true
      # 是否打印SQL结果行数
      show-result-count: true
      # 慢查询阈值（毫秒）
      slow-query-threshold: 1000
      # 日志级别：DEBUG, INFO, WARN, ERROR
      log-level: INFO
      # 是否格式化SQL语句
      format-sql: true
      # 是否显示SQL类型
      show-sql-type: true
```

### 3. 使用示例

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserMapper userMapper;
    
    @GetMapping
    public List<User> getAllUsers() {
        // 这个查询会自动打印SQL日志
        return userMapper.selectAll();
    }
}
```

## 配置说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `enabled` | boolean | false | 是否启用SQL日志打印功能 |
| `show-parameters` | boolean | true | 是否打印SQL参数 |
| `show-execution-time` | boolean | true | 是否打印SQL执行时间 |
| `show-result-count` | boolean | true | 是否打印SQL结果行数 |
| `slow-query-threshold` | long | 1000 | 慢查询阈值（毫秒） |
| `log-level` | String | "INFO" | 日志级别 |
| `format-sql` | boolean | true | 是否格式化SQL语句 |
| `show-sql-type` | boolean | true | 是否显示SQL类型 |

## 日志输出示例

启用功能后，你会看到类似以下的日志输出：

```
2024-01-15 10:30:45.123 INFO  - SQL执行日志 [SELECT] - ID: com.example.mapper.UserMapper.selectAll, SQL: SELECT id, username, email, age, create_time FROM user ORDER BY id DESC, 执行时间: 15ms, 结果行数: 5
```

对于慢查询：

```
2024-01-15 10:30:45.123 WARN  - SQL执行日志 [SELECT] - ID: com.example.mapper.UserMapper.selectByUsername, SQL: SELECT id, username, email, age, create_time FROM user WHERE username LIKE CONCAT('%', ?, '%'), Parameters: username=test, 执行时间: 1500ms (慢查询警告), 结果行数: 1
```

## 工作原理

1. **BeanPostProcessor**: 在Spring容器启动时，通过`MybatisSqlLoggerBeanPostProcessor`自动发现所有的`SqlSessionFactory`
2. **拦截器注册**: 将`SqlLogInterceptor`注册到每个`SqlSessionFactory`的配置中
3. **SQL拦截**: 拦截所有MyBatis的SQL执行操作（查询和更新）
4. **日志记录**: 根据配置记录SQL语句、参数、执行时间等信息

## 注意事项

1. 该功能仅在`mybatis.sql.logger.enabled=true`时生效
2. 需要确保项目中已经配置了MyBatis
3. 建议在生产环境中谨慎使用，避免影响性能
4. 可以通过`slow-query-threshold`配置来监控慢查询

## 构建和安装

```bash
# 编译项目
mvn clean compile

# 打包
mvn clean package

# 安装到本地仓库
mvn clean install
```

## 许可证

MIT License
