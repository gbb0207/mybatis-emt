package com.xyf.mybatis.emt.test.mapper;

import org.apache.ibatis.annotations.Insert;

public interface TestMapper {

    @Insert("INSERT INTO test_table (username, age, phone, united1, united2) " +
            "VALUES (#{username}, #{age}, #{phone}, #{united1}, #{united2})")
    int insertTestTable(String username, Integer age, String phone, Integer united1, Integer united2);

}
