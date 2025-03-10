package com.xyf.emt.core.callback;

import com.xyf.emt.core.strategy.CompareTableInfo;
import com.xyf.emt.core.strategy.TableMetadata;

/**
 * 修改表回调
 */
@FunctionalInterface
public interface ModifyTableFinishCallback {

    /**
     * 修改表后回调
     *
     * @param databaseDialect  数据库方言
     * @param tableMetadata    表元数据
     * @param compareTableInfo 对比表信息
     */
    void afterModifyTable(String databaseDialect, final TableMetadata tableMetadata, final CompareTableInfo compareTableInfo);
}
