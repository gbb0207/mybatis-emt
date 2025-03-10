package com.xyf.emt.core.callback;


import com.xyf.emt.core.strategy.TableMetadata;

/**
 * 创建表回调
 */
@FunctionalInterface
public interface CreateTableFinishCallback {

    /**
     * 建表后回调
     *
     * @param databaseDialect 数据库方言
     * @param tableMetadata   表元数据
     */
    void afterCreateTable(String databaseDialect, final TableMetadata tableMetadata);
}
