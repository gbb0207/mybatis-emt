package com.xyf.mybatis.emt.test.mybatis;

import com.xyf.emt.starter.EnableEmtTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: 熊韵飞
 * @Description:
 */

@SpringBootTest
@EnableEmtTest
public class UnitTest {
    @Test
    void a() {
        System.out.println("开启 @EnableEmtTest，测试环境下允许建表");
    }
}
