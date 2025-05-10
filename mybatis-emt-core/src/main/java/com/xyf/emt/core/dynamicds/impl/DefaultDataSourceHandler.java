package com.xyf.emt.core.dynamicds.impl;

import com.xyf.emt.core.dynamicds.IDataSourceHandler;
import lombok.NonNull;

public class DefaultDataSourceHandler implements IDataSourceHandler {

    @Override
    public void useDataSource(String dataSourceName) {  // 一个数据源时，不需要选择
        // nothing
    }

    @Override
    public void clearDataSource(String dataSourceName) {
        // nothing
    }

    @Override
    public @NonNull String getDataSourceName(Class<?> clazz) {
        return "";
    }
}
