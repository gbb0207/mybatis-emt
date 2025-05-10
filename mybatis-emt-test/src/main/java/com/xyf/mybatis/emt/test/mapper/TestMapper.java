package com.xyf.mybatis.emt.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyf.mybatis.emt.test.pojo.TestTable;
import org.apache.ibatis.annotations.Insert;

public interface TestMapper extends BaseMapper<TestTable> {

    @Insert("INSERT INTO test_table (username, age, phone, united1, united2) " +
            "VALUES (#{username}, #{age}, #{phone}, #{united1}, #{united2})")
    int insertTestTable(String username, Integer age, String phone, Integer united1, Integer united2);

}
