package com.lzx.learn.systemdesign.entity;

import lombok.Data;

/**
 * 用户实体
 */
@Data
public class User {
    private Long id;
    private String name;
    private String email;
    private Integer age;
}