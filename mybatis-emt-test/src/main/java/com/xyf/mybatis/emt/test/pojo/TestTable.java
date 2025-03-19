package com.xyf.mybatis.emt.test.pojo;

import com.xyf.emt.common.Ignore;
import com.xyf.emt.common.enums.IndexSortTypeEnum;
import com.xyf.emt.common.enums.IndexTypeEnum;
import com.xyf.emt.common.field.Column;
import com.xyf.emt.common.index.*;
import com.xyf.emt.common.mysql.MysqlCharset;
import com.xyf.emt.common.mysql.MysqlEngine;
import com.xyf.emt.common.table.EntityTable;
import lombok.Data;

import java.sql.Timestamp;

@Data
@EntityTable(value = "test_table")
@MysqlEngine("InnoDB")
@MysqlCharset(charset = "utf8mb4", collate = "utf8mb4_general_ci")
@JointIndex(name = "united1_united2",
        type = IndexTypeEnum.UNIQUE,
        fields = {"united1", "united2"},
        comment = "测试联合索引",
        indexFields = {
                @IndexField(field = "united1", sort = IndexSortTypeEnum.ASC),
                @IndexField(field = "united2", sort = IndexSortTypeEnum.DESC)
        })
public class TestTable {

    @PrimaryKey(true)
    private Integer id;

    private String username;

    private Integer age;

    @Index
    @Column(value = "phone", type = "varchar", length = 255, notNull = true) // NOT NULL 默认建立唯一索引
    private String phone;

    @Column(value = "present_time", type = "timestamp", defaultValue = "CURRENT_TIMESTAMP")
    private Timestamp presentTime;

    @Column(value = "test_int", type = "int", length = 2)
    @Ignore
    private Integer testInt;

    private Integer united1;    // 测试联合索引1

    private Integer united2;    // 测试联合索引1
}
