package com.xyf.emt.test.dynamicdatasource;

import com.xyf.emt.common.field.ColumnComment;
import com.xyf.emt.common.field.ColumnType;
import com.xyf.emt.common.index.PrimaryKey;
import com.xyf.emt.common.mysql.MysqlTypeConstant;
import com.xyf.emt.common.table.EntityTable;
import com.xyf.emt.test.dynamicdatasource.dynamicdatasourceframe.Ds;
import lombok.Data;

@Data
@EntityTable("master_user2")
@Ds("a")    // 数据源标识如果没有通过 DynamicDataSourceConfig 配置在 dynamicDataSource.setTargetDataSources(dataSourceMap); 里。那就走默认 master 数据源
public class User3 {

    @PrimaryKey(true)
    @ColumnComment("用户id")
    private Long id;

    @ColumnComment("电话")
    private String phone;

    @ColumnComment("备注")
    @ColumnType(MysqlTypeConstant.TEXT)
    private String mark;
}
