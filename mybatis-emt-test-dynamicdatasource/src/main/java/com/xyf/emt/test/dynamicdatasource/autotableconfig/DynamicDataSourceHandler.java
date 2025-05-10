package com.xyf.emt.test.dynamicdatasource.autotableconfig;

import com.xyf.emt.test.dynamicdatasource.dynamicdatasourceframe.DynamicDataSourceContextHolder;
import com.xyf.emt.core.dynamicds.IDataSourceHandler;
import lombok.NonNull;
import com.xyf.emt.test.dynamicdatasource.dynamicdatasourceframe.Ds;
import org.springframework.stereotype.Component;

@Component
public class DynamicDataSourceHandler implements IDataSourceHandler {   // 2.对于同一个数据源下的实体类集合，设置一个数据源标识，用同一个数据源连接处理

    @Override
    public void useDataSource(String dataSourceName) {  // 2.将1中从@Ds中取出的数据源标识存入当前线程的ThreadLocal中，为了后续sql执行时更改指定名称的数据源
        // 设置当前实体类集的数据源
        DynamicDataSourceContextHolder.setContextKey(dataSourceName);
    }

    @Override
    public void clearDataSource(String dataSourceName) {    // 3
        // 清除数据源
        DynamicDataSourceContextHolder.removeContextKey();
    }

    /**
     * 取出数据源标识，例如 master、slave，即取出 @Ds("xxx") 注解中的值
     * @param clazz 指定类
     * @return
     */
    @Override
    public @NonNull String getDataSourceName(Class<?> clazz) {  // 1
        // 根据实体类获取对应的数据源名称，假定自定义的多数据源，有一个注解Ds
        Ds ds = clazz.getAnnotation(Ds.class);
        if (ds != null) {
            return ds.value();
        } else {
            return DynamicDataSourceContextHolder.getContextKey();  // 为空就返回默认的数据源，返回默认数据源逻辑在 getContextKey() 中
        }
    }
}
