package com.lzx.learn.systemdesign.autoconfigure;

import com.lzx.learn.systemdesign.config.MybatisSqlLoggerProperties;
import com.lzx.learn.systemdesign.interceptor.SqlLogInterceptor;
import com.lzx.learn.systemdesign.processor.MybatisSqlLoggerBeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis SQL日志自动配置类
 * 
 * @author lzx
 */
@Configuration
@ConditionalOnClass(name = "org.apache.ibatis.session.SqlSessionFactory")
@EnableConfigurationProperties(MybatisSqlLoggerProperties.class)
public class MybatisSqlLoggerAutoConfiguration {
    
    /**
     * 创建配置属性Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisSqlLoggerProperties mybatisSqlLoggerProperties() {
        return new MybatisSqlLoggerProperties();
    }
    
    /**
     * 创建SQL日志拦截器Bean
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "mybatis.sql.logger", name = "enabled", havingValue = "true")
    public SqlLogInterceptor sqlLogInterceptor(MybatisSqlLoggerProperties properties) {
        return new SqlLogInterceptor(properties);
    }
    
    /**
     * 创建BeanPostProcessor
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "mybatis.sql.logger", name = "enabled", havingValue = "true")
    public MybatisSqlLoggerBeanPostProcessor mybatisSqlLoggerBeanPostProcessor() {
        return new MybatisSqlLoggerBeanPostProcessor();
    }
}