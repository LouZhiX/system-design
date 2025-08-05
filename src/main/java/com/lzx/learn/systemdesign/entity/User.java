package com.lzx.learn.systemdesign.entity;

import lombok.Data;

/**
 * 用户实体类
 * 
 * @author lzx
 */
@Data
public class User {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 年龄
     */
    private Integer age;
    
    /**
     * 创建时间
     */
    private String createTime;
}