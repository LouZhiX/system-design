package com.lzx.learn.systemdesign.controller;

import com.lzx.learn.systemdesign.entity.User;
import com.lzx.learn.systemdesign.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 用户控制器
 * 提供REST API来演示SQL监控
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取所有用户
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("REST API: 获取所有用户");
        return ResponseEntity.ok(userService.findAllUsers());
    }
    
    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("REST API: 根据ID获取用户: {}", id);
        try {
            User user = userService.findUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("用户不存在: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 根据名称搜索用户
     */
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsersByName(@RequestParam String name) {
        log.info("REST API: 根据名称搜索用户: {}", name);
        return ResponseEntity.ok(userService.findUsersByName(name));
    }
    
    /**
     * 创建用户
     */
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody User user) {
        log.info("REST API: 创建用户: {}", user.getName());
        try {
            userService.createUser(user);
            return ResponseEntity.ok("用户创建成功");
        } catch (Exception e) {
            log.error("创建用户失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("创建用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("REST API: 更新用户: {}", id);
        try {
            user.setId(id);
            userService.updateUser(user);
            return ResponseEntity.ok("用户更新成功");
        } catch (Exception e) {
            log.error("更新用户失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("更新用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        log.info("REST API: 删除用户: {}", id);
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("用户删除成功");
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("删除用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户统计信息
     */
    @GetMapping("/count")
    public ResponseEntity<Integer> getUserCount() {
        log.info("REST API: 获取用户统计");
        return ResponseEntity.ok(userService.countUsers());
    }
    
    /**
     * 按年龄范围查询用户
     */
    @GetMapping("/age-range")
    public ResponseEntity<List<User>> getUsersByAgeRange(
            @RequestParam int minAge, 
            @RequestParam int maxAge) {
        log.info("REST API: 按年龄范围查询用户: {} - {}", minAge, maxAge);
        return ResponseEntity.ok(userService.findUsersByAgeRange(minAge, maxAge));
    }
    
    /**
     * 批量创建用户
     */
    @PostMapping("/batch")
    public ResponseEntity<String> batchCreateUsers() {
        log.info("REST API: 批量创建用户");
        try {
            List<User> users = Arrays.asList(
                new User(null, "张三", "zhangsan@example.com", 25),
                new User(null, "李四", "lisi@example.com", 30),
                new User(null, "王五", "wangwu@example.com", 35),
                new User(null, "赵六", "zhaoliu@example.com", 28),
                new User(null, "钱七", "qianqi@example.com", 32)
            );
            
            userService.batchInsertUsers(users);
            return ResponseEntity.ok("批量创建用户成功");
        } catch (Exception e) {
            log.error("批量创建用户失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("批量创建用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示各种SQL操作
     */
    @PostMapping("/demo")
    public ResponseEntity<String> demo() {
        log.info("REST API: 演示各种SQL操作");
        try {
            // 1. 查询所有用户
            userService.findAllUsers();
            
            // 2. 根据ID查询用户
            userService.findUserById(1L);
            
            // 3. 根据名称查询用户
            userService.findUsersByName("张");
            
            // 4. 统计用户数量
            userService.countUsers();
            
            // 5. 按年龄范围查询
            userService.findUsersByAgeRange(20, 40);
            
            return ResponseEntity.ok("SQL操作演示完成");
        } catch (Exception e) {
            log.error("SQL操作演示失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("SQL操作演示失败: " + e.getMessage());
        }
    }
}