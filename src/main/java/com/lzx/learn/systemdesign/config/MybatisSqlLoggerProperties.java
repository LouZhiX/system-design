package com.lzx.learn.systemdesign.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MyBatis SQL日志配置属性
 * 
 * @author lzx
 */
@Data
@ConfigurationProperties(prefix = "mybatis.sql.logger")
public class MybatisSqlLoggerProperties {
    
    /**
     * 是否启用SQL日志打印功能
     */
    private boolean enabled = false;
    
    /**
     * 是否打印SQL参数
     */
    private boolean showParameters = true;
    
    /**
     * 是否打印SQL执行时间
     */
    private boolean showExecutionTime = true;
    
    /**
     * 是否打印SQL结果行数
     */
    private boolean showResultCount = true;
    
    /**
     * 慢查询阈值（毫秒），超过此时间会打印警告日志
     */
    private long slowQueryThreshold = 1000;
    
    /**
     * 日志级别：DEBUG, INFO, WARN, ERROR
     */
    private String logLevel = "INFO";
    
    /**
     * 是否格式化SQL语句
     */
    private boolean formatSql = true;
    
    /**
     * 是否显示SQL类型（SELECT, INSERT, UPDATE, DELETE）
     */
    private boolean showSqlType = true;
}