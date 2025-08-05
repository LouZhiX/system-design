package com.lzx.learn.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.concurrent.Callable;

/**
 * SQL监控Java Agent
 * 通过字节码增强技术拦截所有SQL执行
 */
public class SqlMonitorAgent {
    
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("SQL监控Agent已启动...");
        
        // 初始化SQL监控管理器
        SqlMonitorManager.init(inst);
        
        new AgentBuilder.Default()
                .type(ElementMatchers.nameStartsWith("java.sql"))
                .or(ElementMatchers.nameStartsWith("com.mysql"))
                .or(ElementMatchers.nameStartsWith("org.postgresql"))
                .or(ElementMatchers.nameStartsWith("org.h2"))
                .or(ElementMatchers.nameStartsWith("com.zaxxer.hikari"))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.method(ElementMatchers.any())
                                .intercept(MethodDelegation.to(SqlInterceptor.class)))
                .installOn(inst);
    }
    
    /**
     * SQL拦截器
     */
    public static class SqlInterceptor {
        
        @RuntimeType
        public static Object intercept(@Origin Method method,
                                    @AllArguments Object[] args,
                                    @SuperCall Callable<?> callable) throws Exception {
            
            String methodName = method.getName();
            String className = method.getDeclaringClass().getSimpleName();
            
            // 只监控关键的SQL执行方法
            if (isSqlExecutionMethod(methodName, className)) {
                long startTime = System.currentTimeMillis();
                String sql = extractSql(args, methodName);
                
                // 使用监控管理器记录SQL执行
                SqlMonitorManager.recordSqlExecution(sql, className, methodName, startTime);
                
                try {
                    Object result = callable.call();
                    long endTime = System.currentTimeMillis();
                    
                    // 记录成功完成
                    SqlMonitorManager.recordSqlCompletion(String.valueOf(System.currentTimeMillis()), 
                                                        endTime, true, null);
                    
                    return result;
                } catch (Exception e) {
                    long endTime = System.currentTimeMillis();
                    
                    // 记录异常完成
                    SqlMonitorManager.recordSqlCompletion(String.valueOf(System.currentTimeMillis()), 
                                                        endTime, false, e.getMessage());
                    
                    throw e;
                }
            }
            
            return callable.call();
        }
        
        private static boolean isSqlExecutionMethod(String methodName, String className) {
            return (methodName.equals("execute") || 
                    methodName.equals("executeQuery") || 
                    methodName.equals("executeUpdate") ||
                    methodName.equals("executeBatch")) &&
                   (className.contains("Statement") || 
                    className.contains("PreparedStatement") ||
                    className.contains("CallableStatement"));
        }
        
        private static String extractSql(Object[] args, String methodName) {
            if (args == null || args.length == 0) {
                return "无SQL参数";
            }
            
            StringBuilder sql = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    sql.append("参数").append(i).append(": ").append(args[i]).append(" ");
                }
            }
            
            return sql.toString().trim();
        }
    }
}