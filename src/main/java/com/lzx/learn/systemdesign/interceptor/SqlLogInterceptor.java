package com.lzx.learn.systemdesign.interceptor;

import com.lzx.learn.systemdesign.config.MybatisSqlLoggerProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.text.DateFormat;
import java.util.*;
import java.util.regex.Matcher;

/**
 * MyBatis SQL日志拦截器
 * 
 * @author lzx
 */
@Slf4j
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class SqlLogInterceptor implements Interceptor {
    
    private final MybatisSqlLoggerProperties properties;
    
    public SqlLogInterceptor(MybatisSqlLoggerProperties properties) {
        this.properties = properties;
    }
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (!properties.isEnabled()) {
            return invocation.proceed();
        }
        
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        
        String sqlId = mappedStatement.getId();
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String sql = boundSql.getSql();
        
        // 获取SQL类型
        String sqlType = getSqlType(mappedStatement);
        
        // 格式化SQL
        if (properties.isFormatSql()) {
            sql = formatSql(sql);
        }
        
        // 获取参数
        String parameters = "";
        if (properties.isShowParameters()) {
            parameters = getParameters(mappedStatement, boundSql, parameter);
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = invocation.proceed();
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // 记录日志
            logSql(sqlId, sqlType, sql, parameters, executionTime, result);
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // 记录错误日志
            logError(sqlId, sqlType, sql, parameters, executionTime, e);
            throw e;
        }
    }
    
    /**
     * 获取SQL类型
     */
    private String getSqlType(MappedStatement mappedStatement) {
        if (!properties.isShowSqlType()) {
            return "";
        }
        
        String sqlCommandType = mappedStatement.getSqlCommandType().name();
        return "[" + sqlCommandType + "]";
    }
    
    /**
     * 格式化SQL
     */
    private String formatSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }
    
    /**
     * 获取参数信息
     */
    private String getParameters(MappedStatement mappedStatement, BoundSql boundSql, Object parameter) {
        if (parameter == null) {
            return "";
        }
        
        Configuration configuration = mappedStatement.getConfiguration();
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        
        if (parameterMappings == null || parameterMappings.isEmpty()) {
            return "";
        }
        
        StringBuilder parameters = new StringBuilder();
        parameters.append("Parameters: ");
        
        try {
            MetaObject metaObject = configuration.newMetaObject(parameter);
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                // 检查参数映射模式，OUT模式不需要处理
                if (parameterMapping.getMode() == null || parameterMapping.getMode().name().equals("IN")) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (parameter == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(parameter.getClass())) {
                        value = parameter;
                    } else {
                        value = metaObject.getValue(propertyName);
                    }
                    
                    parameters.append(propertyName).append("=").append(value);
                    if (i < parameterMappings.size() - 1) {
                        parameters.append(", ");
                    }
                }
            }
        } catch (Exception e) {
            parameters.append("Error getting parameters: ").append(e.getMessage());
        }
        
        return parameters.toString();
    }
    
    /**
     * 记录SQL日志
     */
    private void logSql(String sqlId, String sqlType, String sql, String parameters, long executionTime, Object result) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("SQL执行日志 ").append(sqlType).append(" - ");
        logMessage.append("ID: ").append(sqlId).append(", ");
        logMessage.append("SQL: ").append(sql);
        
        if (properties.isShowParameters() && !parameters.isEmpty()) {
            logMessage.append(", ").append(parameters);
        }
        
        if (properties.isShowExecutionTime()) {
            logMessage.append(", 执行时间: ").append(executionTime).append("ms");
            
            if (executionTime > properties.getSlowQueryThreshold()) {
                logMessage.append(" (慢查询警告)");
            }
        }
        
        if (properties.isShowResultCount() && result instanceof List) {
            logMessage.append(", 结果行数: ").append(((List<?>) result).size());
        }
        
        String level = properties.getLogLevel().toUpperCase();
        switch (level) {
            case "DEBUG":
                log.debug(logMessage.toString());
                break;
            case "WARN":
                log.warn(logMessage.toString());
                break;
            case "ERROR":
                log.error(logMessage.toString());
                break;
            default:
                log.info(logMessage.toString());
                break;
        }
    }
    
    /**
     * 记录错误日志
     */
    private void logError(String sqlId, String sqlType, String sql, String parameters, long executionTime, Exception e) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("SQL执行错误 ").append(sqlType).append(" - ");
        logMessage.append("ID: ").append(sqlId).append(", ");
        logMessage.append("SQL: ").append(sql);
        
        if (properties.isShowParameters() && !parameters.isEmpty()) {
            logMessage.append(", ").append(parameters);
        }
        
        if (properties.isShowExecutionTime()) {
            logMessage.append(", 执行时间: ").append(executionTime).append("ms");
        }
        
        log.error(logMessage.toString(), e);
    }
    
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    
    @Override
    public void setProperties(Properties properties) {
        // 不需要实现，因为我们使用配置类
    }
}