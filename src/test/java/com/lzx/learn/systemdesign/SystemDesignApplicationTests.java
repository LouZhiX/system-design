package com.lzx.learn.systemdesign;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;

@SpringBootTest
class SystemDesignApplicationTests {

    @Resource
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {

        int count = applicationContext.getBeanDefinitionCount();
        System.out.println("所有bean的数量为：" + count);
    }

}
