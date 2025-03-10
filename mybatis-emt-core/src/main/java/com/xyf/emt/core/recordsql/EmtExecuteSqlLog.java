package com.xyf.emt.core.recordsql;

import com.xyf.emt.common.field.ColumnType;
import com.xyf.emt.common.Ignore;
import lombok.Getter;
import lombok.Setter;

/**
 * 记录自动建表执行的SQL
 */
@Getter
public class EmtExecuteSqlLog {

    @Ignore
    private Class<?> entityClass;

    private String tableSchema;

    private String tableName;

    @ColumnType(length = 5000)
    private String sqlStatement;    // 执行的 sql

    @Setter
    private String version; // 非必须

    private Long executionTime;

    private Long executionEndTime;

    private EmtExecuteSqlLog() {
    }

    public static EmtExecuteSqlLog of(Class<?> entityClass, String tableSchema, String tableName, String sql, long executionTime, long executionEndTime) {
        EmtExecuteSqlLog emtExecuteSqlLog = new EmtExecuteSqlLog();
        emtExecuteSqlLog.entityClass = entityClass;
        emtExecuteSqlLog.tableSchema = tableSchema;
        emtExecuteSqlLog.tableName = tableName;
        emtExecuteSqlLog.sqlStatement = sql;
        emtExecuteSqlLog.executionTime = executionTime;
        emtExecuteSqlLog.executionEndTime = executionEndTime;
        return emtExecuteSqlLog;
    }
}
