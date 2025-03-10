package com.xyf.emt.core.recordsql;

public interface RecordSqlHandler {

    /**
     * 记录sql
     * @param emtExecuteSqlLog sql对象
     */
    void record(EmtExecuteSqlLog emtExecuteSqlLog);
}
