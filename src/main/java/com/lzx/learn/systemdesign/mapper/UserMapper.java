package com.lzx.learn.systemdesign.mapper;

import com.lzx.learn.systemdesign.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 * 
 * @author lzx
 */
@Mapper
public interface UserMapper {
    
    /**
     * 根据ID查询用户
     */
    User selectById(@Param("id") Long id);
    
    /**
     * 查询所有用户
     */
    List<User> selectAll();
    
    /**
     * 根据用户名查询用户
     */
    List<User> selectByUsername(@Param("username") String username);
    
    /**
     * 插入用户
     */
    int insert(User user);
    
    /**
     * 更新用户
     */
    int update(User user);
    
    /**
     * 删除用户
     */
    int deleteById(@Param("id") Long id);
}