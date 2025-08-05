# MyBatis SQL Logger 使用示例

## 1. 添加依赖

在你的Spring Boot项目的`pom.xml`中添加依赖：

```xml
<dependency>
    <groupId>com.lzx.learn</groupId>
    <artifactId>mybatis-sql-logger</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 2. 配置启用

在`application.yml`中启用SQL日志功能：

```yaml
# MyBatis SQL日志配置
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

## 3. 创建实体类

```java
package com.example.entity;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String username;
    private String email;
    private Integer age;
    private String createTime;
}
```

## 4. 创建Mapper接口

```java
package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    
    User selectById(@Param("id") Long id);
    
    List<User> selectAll();
    
    List<User> selectByUsername(@Param("username") String username);
    
    int insert(User user);
    
    int update(User user);
    
    int deleteById(@Param("id") Long id);
}
```

## 5. 创建Mapper XML文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.mapper.UserMapper">
    
    <resultMap id="BaseResultMap" type="com.example.entity.User">
        <id column="id" property="id" jdbcType="BIGINT"/>
        <result column="username" property="username" jdbcType="VARCHAR"/>
        <result column="email" property="email" jdbcType="VARCHAR"/>
        <result column="age" property="age" jdbcType="INTEGER"/>
        <result column="create_time" property="createTime" jdbcType="VARCHAR"/>
    </resultMap>
    
    <sql id="Base_Column_List">
        id, username, email, age, create_time
    </sql>
    
    <select id="selectById" parameterType="java.lang.Long" resultMap="BaseResultMap">
        SELECT 
        <include refid="Base_Column_List"/>
        FROM user
        WHERE id = #{id}
    </select>
    
    <select id="selectAll" resultMap="BaseResultMap">
        SELECT 
        <include refid="Base_Column_List"/>
        FROM user
        ORDER BY id DESC
    </select>
    
    <select id="selectByUsername" parameterType="java.lang.String" resultMap="BaseResultMap">
        SELECT 
        <include refid="Base_Column_List"/>
        FROM user
        WHERE username LIKE CONCAT('%', #{username}, '%')
    </select>
    
    <insert id="insert" parameterType="com.example.entity.User" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO user (username, email, age, create_time)
        VALUES (#{username}, #{email}, #{age}, #{createTime})
    </insert>
    
    <update id="update" parameterType="com.example.entity.User">
        UPDATE user
        SET username = #{username},
            email = #{email},
            age = #{age}
        WHERE id = #{id}
    </update>
    
    <delete id="deleteById" parameterType="java.lang.Long">
        DELETE FROM user WHERE id = #{id}
    </delete>
    
</mapper>
```

## 6. 创建控制器

```java
package com.example.controller;

import com.example.entity.User;
import com.example.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserMapper userMapper;
    
    @GetMapping
    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }
    
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userMapper.selectById(id);
    }
    
    @GetMapping("/search")
    public List<User> searchUsersByUsername(@RequestParam String username) {
        return userMapper.selectByUsername(username);
    }
    
    @PostMapping
    public String createUser(@RequestBody User user) {
        user.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        int result = userMapper.insert(user);
        return result > 0 ? "创建成功" : "创建失败";
    }
    
    @PutMapping("/{id}")
    public String updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        int result = userMapper.update(user);
        return result > 0 ? "更新成功" : "更新失败";
    }
    
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        int result = userMapper.deleteById(id);
        return result > 0 ? "删除成功" : "删除失败";
    }
}
```

## 7. 运行应用

启动应用后，当你调用任何数据库操作时，会在日志中看到类似以下的输出：

```
2024-01-15 10:30:45.123 INFO  - SQL执行日志 [SELECT] - ID: com.example.mapper.UserMapper.selectAll, SQL: SELECT id, username, email, age, create_time FROM user ORDER BY id DESC, 执行时间: 15ms, 结果行数: 5
```

对于慢查询：

```
2024-01-15 10:30:45.123 WARN  - SQL执行日志 [SELECT] - ID: com.example.mapper.UserMapper.selectByUsername, SQL: SELECT id, username, email, age, create_time FROM user WHERE username LIKE CONCAT('%', ?, '%'), Parameters: username=test, 执行时间: 1500ms (慢查询警告), 结果行数: 1
```

## 8. 测试API

使用curl或Postman测试API：

```bash
# 查询所有用户
curl http://localhost:8080/api/users

# 根据ID查询用户
curl http://localhost:8080/api/users/1

# 根据用户名搜索用户
curl http://localhost:8080/api/users/search?username=test

# 创建用户
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","age":25}'

# 更新用户
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"username":"updated","email":"updated@example.com","age":30}'

# 删除用户
curl -X DELETE http://localhost:8080/api/users/1
```

## 9. 配置说明

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

## 10. 注意事项

1. 该功能仅在`mybatis.sql.logger.enabled=true`时生效
2. 需要确保项目中已经配置了MyBatis
3. 建议在生产环境中谨慎使用，避免影响性能
4. 可以通过`slow-query-threshold`配置来监控慢查询
5. 支持Spring Boot 3.x和Java 17+