package com.xyf.mybatis.emt.test;

import com.xyf.emt.starter.EnableEmt;

import com.xyf.emt.starter.EnableEmtTest;
import com.xyf.emt.starter.config.EmtAutoConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableEmt
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
