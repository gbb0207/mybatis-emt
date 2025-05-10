package com.xyf.emt.test.dynamicdatasource.dynamicdatasourceframe;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * AbstractRoutingDataSource 用于实现运行时动态选择数据源的能力，核心是 determineCurrentLookupKey() 方法，这个方法返回一个数据源标识，Spring 会根据这个标识从预配置的数据源列表中选择当前要使用的数据源。
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
    /**
     * 进行数据库操作时，例如执行查询、插入、更新或删除等操作，Spring 会在内部调用 determineCurrentLookupKey() 方法来确定当前应该使用哪个数据源。
     * 这个过程通常发生在获取 SqlSession，事务管理等地方。
     * @return
     */
    @Override
    protected Object determineCurrentLookupKey() {  // 执行 sql 时自动调用此方法，切换数据源（DynamicDataSourceConfig 中配置的数据源 bean）
        // 通过数据源标识取出 DynamicDataSourceConfig 对应的 DataSource 实例，具体映射关系通过 dynamicDataSource.setTargetDataSources(dataSourceMap);
//         中的 Map 来变更。
        return DynamicDataSourceContextHolder.getContextKey();
    }
}
