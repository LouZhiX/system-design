package com.lzx.learn.systemdesign.processor;

import com.lzx.learn.systemdesign.config.MybatisSqlLoggerProperties;
import com.lzx.learn.systemdesign.interceptor.SqlLogInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * MyBatis SQL日志BeanPostProcessor
 * 用于在Spring容器启动时自动配置MyBatis拦截器
 * 
 * @author lzx
 */
@Slf4j
@Component
@Configuration
@EnableConfigurationProperties(MybatisSqlLoggerProperties.class)
@ConditionalOnProperty(prefix = "mybatis.sql.logger", name = "enabled", havingValue = "true")
public class MybatisSqlLoggerBeanPostProcessor {
    
    @Autowired
    private MybatisSqlLoggerProperties properties;
    
    @Autowired(required = false)
    private List<SqlSessionFactory> sqlSessionFactories;
    
    @PostConstruct
    public void init() {
        if (sqlSessionFactories == null || sqlSessionFactories.isEmpty()) {
            log.warn("未找到SqlSessionFactory，MyBatis SQL日志功能将不会生效");
            return;
        }
        
        log.info("开始配置MyBatis SQL日志拦截器，共找到{}个SqlSessionFactory", sqlSessionFactories.size());
        
        SqlLogInterceptor interceptor = new SqlLogInterceptor(properties);
        
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories) {
            try {
                sqlSessionFactory.getConfiguration().addInterceptor(interceptor);
                log.info("成功为SqlSessionFactory添加SQL日志拦截器");
            } catch (Exception e) {
                log.error("为SqlSessionFactory添加SQL日志拦截器失败", e);
            }
        }
        
        log.info("MyBatis SQL日志拦截器配置完成");
    }
    
    /**
     * 创建SQL日志拦截器Bean
     */
    @Bean
    public SqlLogInterceptor sqlLogInterceptor() {
        return new SqlLogInterceptor(properties);
    }
}