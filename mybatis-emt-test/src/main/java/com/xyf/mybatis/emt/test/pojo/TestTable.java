package com.xyf.mybatis.emt.test.pojo;

import com.xyf.emt.common.Ignore;
import com.xyf.emt.common.enums.DefaultValueEnum;
import com.xyf.emt.common.enums.IndexSortTypeEnum;
import com.xyf.emt.common.enums.IndexTypeEnum;
import com.xyf.emt.common.field.Column;
import com.xyf.emt.common.index.*;
import com.xyf.emt.common.mysql.MysqlCharset;
import com.xyf.emt.common.mysql.MysqlEngine;
import com.xyf.emt.common.table.EntityTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@EntityTable(value = "test_table")
@MysqlEngine("InnoDB")
@MysqlCharset(charset = "utf8mb4", collate = "utf8mb4_general_ci")
@JointIndex(name = "united1_united2",
        type = IndexTypeEnum.UNIQUE,    // 唯一索引，NON_UNIQUE=0
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

    @Column(value = "age", type = "int", length = 11, notNull = true, comment = "年龄")
    private Integer age;

    @Index  // 普通索引
    @Column(value = "phone", type = "varchar", length = 11, notNull = true, comment = "电话", defaultValueType = DefaultValueEnum.EMPTY_STRING)
    private String phone;

    @Column(value = "present_time", type = "timestamp", defaultValue = "CURRENT_TIMESTAMP")
    private Timestamp presentTime;

    @Column(value = "test_int", type = "int", length = 2)
    @Ignore
    private Integer testInt;    // @Ignore，忽略此字段，不会包含在列中。只有主动忽略才不会对其创建。

    private Integer united1;    // 测试联合索引1

    private Integer united2;    // 测试联合索引1

    private Integer newColumn;  // update 模式下，测试
}
