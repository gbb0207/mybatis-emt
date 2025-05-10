package com.xyf.emt.test.dynamicdatasource;

import com.xyf.emt.common.field.ColumnComment;
import com.xyf.emt.common.field.ColumnType;
import com.xyf.emt.common.index.PrimaryKey;
import com.xyf.emt.common.mysql.MysqlTypeConstant;
import com.xyf.emt.common.table.EntityTable;
import lombok.Data;

@Data
@EntityTable("master_user")
public class User {

    @PrimaryKey(true)
    @ColumnComment("用户id")
    private Long id;

    @ColumnComment("电话")
    private String phone;

    @ColumnComment("备注")
    @ColumnType(MysqlTypeConstant.TEXT)
    private String mark;
}
