package com.xyf.mybatis.emt.test;

import com.xyf.emt.starter.EnableEmt;

import com.xyf.emt.starter.EnableEmtTest;
import com.xyf.emt.starter.config.EmtAutoConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableEmt
@MapperScan("com.xyf.mybatis.emt.test.mapper")
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
