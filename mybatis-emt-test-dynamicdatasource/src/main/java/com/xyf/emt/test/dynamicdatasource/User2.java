package com.xyf.emt.test.dynamicdatasource;

import com.xyf.emt.test.dynamicdatasource.dynamicdatasourceframe.DataSourceConstants;
import com.xyf.emt.test.dynamicdatasource.dynamicdatasourceframe.Ds;
import com.xyf.emt.common.field.ColumnComment;
import com.xyf.emt.common.field.ColumnType;
import com.xyf.emt.common.index.PrimaryKey;
import com.xyf.emt.common.mysql.MysqlTypeConstant;
import com.xyf.emt.common.table.EntityTable;
import lombok.Data;

@Data
@Ds(DataSourceConstants.DS_KEY_SLAVE)
@EntityTable("slave_user")
public class User2 {

    @PrimaryKey(true)
    @ColumnComment("用户id")
    private Long id;

    @ColumnComment("电话")
    private String phone;

    @ColumnComment("备注")
    @ColumnType(MysqlTypeConstant.TEXT)
    private String mark;
}
