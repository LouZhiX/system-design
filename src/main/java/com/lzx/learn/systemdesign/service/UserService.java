package com.lzx.learn.systemdesign.service;

import com.lzx.learn.systemdesign.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务
 * 演示各种SQL操作
 */
@Slf4j
@Service
public class UserService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setAge(rs.getInt("age"));
        return user;
    };
    
    /**
     * 查询所有用户
     */
    public List<User> findAllUsers() {
        log.info("查询所有用户");
        return jdbcTemplate.query("SELECT * FROM users", userRowMapper);
    }
    
    /**
     * 根据ID查询用户
     */
    public User findUserById(Long id) {
        log.info("根据ID查询用户: {}", id);
        return jdbcTemplate.queryForObject(
            "SELECT * FROM users WHERE id = ?", 
            userRowMapper, 
            id
        );
    }
    
    /**
     * 根据名称查询用户
     */
    public List<User> findUsersByName(String name) {
        log.info("根据名称查询用户: {}", name);
        return jdbcTemplate.query(
            "SELECT * FROM users WHERE name LIKE ?", 
            userRowMapper, 
            "%" + name + "%"
        );
    }
    
    /**
     * 创建用户
     */
    public void createUser(User user) {
        log.info("创建用户: {}", user.getName());
        jdbcTemplate.update(
            "INSERT INTO users (name, email, age) VALUES (?, ?, ?)",
            user.getName(),
            user.getEmail(),
            user.getAge()
        );
    }
    
    /**
     * 更新用户
     */
    public void updateUser(User user) {
        log.info("更新用户: {}", user.getId());
        jdbcTemplate.update(
            "UPDATE users SET name = ?, email = ?, age = ? WHERE id = ?",
            user.getName(),
            user.getEmail(),
            user.getAge(),
            user.getId()
        );
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(Long id) {
        log.info("删除用户: {}", id);
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }
    
    /**
     * 统计用户数量
     */
    public int countUsers() {
        log.info("统计用户数量");
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
    }
    
    /**
     * 批量插入用户
     */
    public void batchInsertUsers(List<User> users) {
        log.info("批量插入用户: {}", users.size());
        String sql = "INSERT INTO users (name, email, age) VALUES (?, ?, ?)";
        
        jdbcTemplate.batchUpdate(sql, users, users.size(), (ps, user) -> {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setInt(3, user.getAge());
        });
    }
    
    /**
     * 复杂查询 - 按年龄分组统计
     */
    public List<User> findUsersByAgeRange(int minAge, int maxAge) {
        log.info("按年龄范围查询用户: {} - {}", minAge, maxAge);
        return jdbcTemplate.query(
            "SELECT * FROM users WHERE age BETWEEN ? AND ? ORDER BY age",
            userRowMapper,
            minAge,
            maxAge
        );
    }
}