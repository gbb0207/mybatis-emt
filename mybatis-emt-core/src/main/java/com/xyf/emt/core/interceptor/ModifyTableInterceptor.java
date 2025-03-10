package com.xyf.emt.core.interceptor;

import com.xyf.emt.core.strategy.CompareTableInfo;
import com.xyf.emt.core.strategy.TableMetadata;

/**
 * 修改表前拦截器
 */
@FunctionalInterface
public interface ModifyTableInterceptor {

    /**
     * 修改表前拦截
     *
     * @param databaseDialect  数据库方言
     * @param tableMetadata    表元数据
     * @param compareTableInfo 对比表信息
     */
    void beforeModifyTable(String databaseDialect, final TableMetadata tableMetadata, final CompareTableInfo compareTableInfo);
}
