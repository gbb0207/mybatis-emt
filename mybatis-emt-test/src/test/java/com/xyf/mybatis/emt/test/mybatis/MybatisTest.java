package com.xyf.mybatis.emt.test.mybatis;

import com.xyf.emt.starter.EnableEmtTest;
import com.xyf.mybatis.emt.test.mapper.TestMapper;
import com.xyf.mybatis.emt.test.pojo.TestTable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @Author: 熊韵飞
 * @Description:
 */

@SpringBootTest
//@EnableEmtTest
public class MybatisTest {

    @Autowired
    TestMapper mapper;

    @Test
    void a() {
        System.out.println("开启 @EnableEmtTest，测试环境下允许建表");
    }

    @Test
    void insert() {
        mapper.insertTestTable("xyf", 21, "17786347815", 1, 2);
    }
}
