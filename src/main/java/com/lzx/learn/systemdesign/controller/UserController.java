package com.lzx.learn.systemdesign.controller;

import com.lzx.learn.systemdesign.entity.User;
import com.lzx.learn.systemdesign.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 用户控制器
 * 
 * @author lzx
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 查询所有用户
     */
    @GetMapping
    public List<User> getAllUsers() {
        log.info("查询所有用户");
        return userMapper.selectAll();
    }
    
    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        log.info("根据ID查询用户: {}", id);
        return userMapper.selectById(id);
    }
    
    /**
     * 根据用户名查询用户
     */
    @GetMapping("/search")
    public List<User> searchUsersByUsername(@RequestParam String username) {
        log.info("根据用户名查询用户: {}", username);
        return userMapper.selectByUsername(username);
    }
    
    /**
     * 创建用户
     */
    @PostMapping
    public String createUser(@RequestBody User user) {
        log.info("创建用户: {}", user);
        user.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        int result = userMapper.insert(user);
        return result > 0 ? "创建成功" : "创建失败";
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public String updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("更新用户: {}", user);
        user.setId(id);
        int result = userMapper.update(user);
        return result > 0 ? "更新成功" : "更新失败";
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        log.info("删除用户: {}", id);
        int result = userMapper.deleteById(id);
        return result > 0 ? "删除成功" : "删除失败";
    }
}