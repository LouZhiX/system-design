package com.lzx.learn.agent;

import lombok.extern.slf4j.Slf4j;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SQL监控管理器
 * 统一管理SQL监控功能
 */
@Slf4j
public class SqlMonitorManager {
    
    private static final AtomicLong sqlCounter = new AtomicLong(0);
    private static final ConcurrentHashMap<String, SqlExecutionInfo> sqlCache = new ConcurrentHashMap<>();
    
    private static boolean isEnabled = true;
    
    /**
     * 初始化SQL监控
     */
    public static void init(Instrumentation inst) {
        log.info("初始化SQL监控管理器...");
        
        // 注册类文件转换器
        inst.addTransformer(new AdvancedSqlMonitor());
        
        // 启动监控统计线程
        startStatisticsThread();
        
        log.info("SQL监控管理器初始化完成");
    }
    
    /**
     * 记录SQL执行信息
     */
    public static void recordSqlExecution(String sql, String className, String methodName, long startTime) {
        if (!isEnabled) {
            return;
        }
        
        long sqlId = sqlCounter.incrementAndGet();
        SqlExecutionInfo info = new SqlExecutionInfo(sqlId, sql, className, methodName, startTime);
        sqlCache.put(String.valueOf(sqlId), info);
        
        log.info("=== SQL监控开始 [ID: {}] ===", sqlId);
        log.info("类: {}", className);
        log.info("方法: {}", methodName);
        log.info("SQL: {}", sql);
    }
    
    /**
     * 记录SQL执行完成
     */
    public static void recordSqlCompletion(String sqlId, long endTime, boolean success, String errorMessage) {
        if (!isEnabled) {
            return;
        }
        
        SqlExecutionInfo info = sqlCache.get(sqlId);
        if (info != null) {
            long duration = endTime - info.getStartTime();
            info.setEndTime(endTime);
            info.setDuration(duration);
            info.setSuccess(success);
            info.setErrorMessage(errorMessage);
            
            log.info("执行时间: {}ms", duration);
            if (!success) {
                log.error("执行异常: {}", errorMessage);
            }
            log.info("=== SQL监控结束 ===");
        }
    }
    
    /**
     * 获取SQL执行统计信息
     */
    public static SqlStatistics getStatistics() {
        SqlStatistics stats = new SqlStatistics();
        
        sqlCache.values().forEach(info -> {
            stats.setTotalCount(stats.getTotalCount() + 1);
            stats.setTotalTime(stats.getTotalTime() + info.getDuration());
            
            if (info.isSuccess()) {
                stats.setSuccessCount(stats.getSuccessCount() + 1);
            } else {
                stats.setErrorCount(stats.getErrorCount() + 1);
            }
            
            if (info.getDuration() > stats.getMaxTime()) {
                stats.setMaxTime(info.getDuration());
            }
            
            if (info.getDuration() < stats.getMinTime() || stats.getMinTime() == 0) {
                stats.setMinTime(info.getDuration());
            }
        });
        
        if (stats.getTotalCount() > 0) {
            stats.setAverageTime(stats.getTotalTime() / stats.getTotalCount());
        }
        
        return stats;
    }
    
    /**
     * 启用/禁用监控
     */
    public static void setEnabled(boolean enabled) {
        isEnabled = enabled;
        log.info("SQL监控已{}", enabled ? "启用" : "禁用");
    }
    
    /**
     * 清空监控数据
     */
    public static void clearData() {
        sqlCache.clear();
        sqlCounter.set(0);
        log.info("SQL监控数据已清空");
    }
    
    /**
     * 启动统计线程
     */
    private static void startStatisticsThread() {
        Thread statsThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000); // 每分钟输出一次统计信息
                    SqlStatistics stats = getStatistics();
                    if (stats.getTotalCount() > 0) {
                        log.info("=== SQL执行统计 ===");
                        log.info("总执行次数: {}", stats.getTotalCount());
                        log.info("成功次数: {}", stats.getSuccessCount());
                        log.info("失败次数: {}", stats.getErrorCount());
                        log.info("平均执行时间: {}ms", stats.getAverageTime());
                        log.info("最大执行时间: {}ms", stats.getMaxTime());
                        log.info("最小执行时间: {}ms", stats.getMinTime());
                        log.info("==================");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        statsThread.setDaemon(true);
        statsThread.setName("SQL-Monitor-Statistics");
        statsThread.start();
    }
    
    /**
     * SQL执行信息
     */
    public static class SqlExecutionInfo {
        private final long sqlId;
        private final String sql;
        private final String className;
        private final String methodName;
        private final long startTime;
        private long endTime;
        private long duration;
        private boolean success;
        private String errorMessage;
        
        public SqlExecutionInfo(long sqlId, String sql, String className, String methodName, long startTime) {
            this.sqlId = sqlId;
            this.sql = sql;
            this.className = className;
            this.methodName = methodName;
            this.startTime = startTime;
        }
        
        // Getters and Setters
        public long getSqlId() { return sqlId; }
        public String getSql() { return sql; }
        public String getClassName() { return className; }
        public String getMethodName() { return methodName; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
    
    /**
     * SQL统计信息
     */
    public static class SqlStatistics {
        private long totalCount;
        private long successCount;
        private long errorCount;
        private long totalTime;
        private long averageTime;
        private long maxTime;
        private long minTime;
        
        // Getters and Setters
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
        public long getSuccessCount() { return successCount; }
        public void setSuccessCount(long successCount) { this.successCount = successCount; }
        public long getErrorCount() { return errorCount; }
        public void setErrorCount(long errorCount) { this.errorCount = errorCount; }
        public long getTotalTime() { return totalTime; }
        public void setTotalTime(long totalTime) { this.totalTime = totalTime; }
        public long getAverageTime() { return averageTime; }
        public void setAverageTime(long averageTime) { this.averageTime = averageTime; }
        public long getMaxTime() { return maxTime; }
        public void setMaxTime(long maxTime) { this.maxTime = maxTime; }
        public long getMinTime() { return minTime; }
        public void setMinTime(long minTime) { this.minTime = minTime; }
    }
}