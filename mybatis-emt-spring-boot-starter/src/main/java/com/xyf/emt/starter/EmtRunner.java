package com.xyf.emt.starter;

import com.xyf.emt.core.EmtBootstrap;
import com.xyf.emt.starter.config.EmtAutoConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;

@AutoConfigureAfter({EmtAutoConfig.class})
public class EmtRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        // 启动Emt
        if (!isTestEnvironment()) {
            EmtBootstrap.start();
        }
    }

    public boolean isTestEnvironment() {
        try {
            // 尝试加载JUnit测试类
            Class.forName("org.junit.jupiter.api.Test");
            return true; // 如果找到则表示在测试环境中
        } catch (ClassNotFoundException e) {
            return false; // 否则是正常启动环境
        }
    }
}
