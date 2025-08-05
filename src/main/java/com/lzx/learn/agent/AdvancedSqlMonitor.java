package com.lzx.learn.agent;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 高级SQL监控器
 * 使用ASM字节码技术进行更精确的SQL监控
 */
public class AdvancedSqlMonitor implements ClassFileTransformer {
    
    private static final AtomicLong sqlCounter = new AtomicLong(0);
    
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                           ProtectionDomain protectionDomain, byte[] classfileBuffer) 
                           throws IllegalClassFormatException {
        
        if (className == null) {
            return classfileBuffer;
        }
        
        // 只处理JDBC相关的类
        if (shouldTransform(className)) {
            try {
                ClassReader cr = new ClassReader(classfileBuffer);
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                ClassVisitor cv = new SqlMonitorClassVisitor(cw, className);
                cr.accept(cv, ClassReader.EXPAND_FRAMES);
                return cw.toByteArray();
            } catch (Exception e) {
                System.err.println("字节码转换失败: " + className + ", 错误: " + e.getMessage());
                return classfileBuffer;
            }
        }
        
        return classfileBuffer;
    }
    
    private boolean shouldTransform(String className) {
        return className.contains("java/sql/Statement") ||
               className.contains("java/sql/PreparedStatement") ||
               className.contains("java/sql/CallableStatement") ||
               className.contains("java/sql/Connection");
    }
    
    /**
     * SQL监控类访问器
     */
    private static class SqlMonitorClassVisitor extends ClassVisitor {
        private final String className;
        
        public SqlMonitorClassVisitor(ClassVisitor cv, String className) {
            super(Opcodes.ASM9, cv);
            this.className = className;
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, 
                                      String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            
            if (isSqlMethod(name)) {
                return new SqlMonitorMethodVisitor(mv, access, name, descriptor, className);
            }
            
            return mv;
        }
        
        private boolean isSqlMethod(String methodName) {
            return "execute".equals(methodName) ||
                   "executeQuery".equals(methodName) ||
                   "executeUpdate".equals(methodName) ||
                   "executeBatch".equals(methodName);
        }
    }
    
    /**
     * SQL监控方法访问器
     */
    private static class SqlMonitorMethodVisitor extends AdviceAdapter {
        private final String methodName;
        private final String className;
        private final Label startLabel = new Label();
        private final Label endLabel = new Label();
        private final Label exceptionLabel = new Label();
        
        public SqlMonitorMethodVisitor(MethodVisitor mv, int access, String name, 
                                    String descriptor, String className) {
            super(Opcodes.ASM9, mv, access, name, descriptor);
            this.methodName = name;
            this.className = className;
        }
        
        @Override
        protected void onMethodEnter() {
            // 记录开始时间
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", 
                              "()J", false);
            mv.visitVarInsn(LSTORE, getLocalVariableIndex("startTime"));
            
            // 记录SQL计数器
            mv.visitMethodInsn(INVOKESTATIC, "java/util/concurrent/atomic/AtomicLong", 
                              "incrementAndGet", "()J", false);
            mv.visitVarInsn(LSTORE, getLocalVariableIndex("sqlId"));
            
            // 打印监控开始信息
            mv.visitLdcInsn("=== SQL监控开始 [ID: " + sqlCounter.incrementAndGet() + "] ===");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "out", 
                              "Ljava/io/PrintStream;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", 
                              "(Ljava/lang/String;)V", false);
            
            mv.visitLdcInsn("类: " + className);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "out", 
                              "Ljava/io/PrintStream;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", 
                              "(Ljava/lang/String;)V", false);
            
            mv.visitLdcInsn("方法: " + methodName);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "out", 
                              "Ljava/io/PrintStream;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", 
                              "(Ljava/lang/String;)V", false);
        }
        
        @Override
        protected void onMethodExit(int opcode) {
            if (opcode != ATHROW) {
                // 正常退出
                mv.visitLabel(endLabel);
                printExecutionTime();
                printSuccessMessage();
            }
        }
        
        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            // 异常处理
            mv.visitLabel(exceptionLabel);
            printExecutionTime();
            printErrorMessage();
            mv.visitInsn(ATHROW);
            
            super.visitMaxs(maxStack, maxLocals);
        }
        
        private void printExecutionTime() {
            // 计算执行时间
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", 
                              "()J", false);
            mv.visitVarInsn(LLOAD, getLocalVariableIndex("startTime"));
            mv.visitInsn(LSUB);
            mv.visitVarInsn(LSTORE, getLocalVariableIndex("duration"));
            
            // 打印执行时间
            mv.visitLdcInsn("执行时间: ");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "out", 
                              "Ljava/io/PrintStream;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", 
                              "(Ljava/lang/String;)V", false);
            
            mv.visitVarInsn(LLOAD, getLocalVariableIndex("duration"));
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "toString", 
                              "(J)Ljava/lang/String;", false);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "out", 
                              "Ljava/io/PrintStream;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", 
                              "(Ljava/lang/String;)V", false);
            
            mv.visitLdcInsn("ms");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "out", 
                              "Ljava/io/PrintStream;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", 
                              "(Ljava/lang/String;)V", false);
        }
        
        private void printSuccessMessage() {
            mv.visitLdcInsn("=== SQL监控结束 ===");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "out", 
                              "Ljava/io/PrintStream;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", 
                              "(Ljava/lang/String;)V", false);
        }
        
        private void printErrorMessage() {
            mv.visitLdcInsn("执行异常");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "out", 
                              "Ljava/io/PrintStream;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", 
                              "(Ljava/lang/String;)V", false);
            
            mv.visitLdcInsn("=== SQL监控结束 ===");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "out", 
                              "Ljava/io/PrintStream;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", 
                              "(Ljava/lang/String;)V", false);
        }
        
        private int getLocalVariableIndex(String name) {
            // 简单的本地变量索引计算
            return 100; // 使用一个较大的索引避免冲突
        }
    }
}