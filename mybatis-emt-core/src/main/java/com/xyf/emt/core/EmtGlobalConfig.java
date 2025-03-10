package com.xyf.emt.core;

import com.xyf.emt.core.callback.CreateTableFinishCallback;
import com.xyf.emt.core.callback.ModifyTableFinishCallback;
import com.xyf.emt.core.callback.RunStateCallback;
import com.xyf.emt.core.callback.ValidateFinishCallback;
import com.xyf.emt.core.config.PropertyConfig;
import com.xyf.emt.core.converter.JavaTypeToDatabaseTypeConverter;
import com.xyf.emt.core.dynamicds.IDataSourceHandler;
import com.xyf.emt.core.dynamicds.impl.DefaultDataSourceHandler;
import com.xyf.emt.core.interceptor.EmtAnnotationInterceptor;
import com.xyf.emt.core.interceptor.BuildTableMetadataInterceptor;
import com.xyf.emt.core.interceptor.CreateTableInterceptor;
import com.xyf.emt.core.interceptor.ModifyTableInterceptor;
import com.xyf.emt.core.recordsql.RecordSqlHandler;
import com.xyf.emt.core.strategy.CompareTableInfo;
import com.xyf.emt.core.strategy.IStrategy;
import com.xyf.emt.core.strategy.TableMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局配置
 */
public class EmtGlobalConfig {

    /**
     * 全局配置
     */
    @Setter
    @Getter
    private static PropertyConfig emtProperties = new PropertyConfig();

    /**
     * 数据源处理器
     */
    @Setter
    @Getter
    private static IDataSourceHandler datasourceHandler = new DefaultDataSourceHandler();

    /**
     * 自定义注解查找器
     */
    @Setter
    @Getter
    private static EmtAnnotationFinder emtAnnotationFinder = new EmtAnnotationFinder() {
    };

    /**
     * ORM框架适配器
     */
    @Setter
    @Getter
    private static EmtOrmFrameAdapter emtOrmFrameAdapter = new EmtOrmFrameAdapter() {
    };

    /**
     * 数据库类型转换
     */
    @Setter
    @Getter
    private static JavaTypeToDatabaseTypeConverter javaTypeToDatabaseTypeConverter = new JavaTypeToDatabaseTypeConverter() {
    };

    /**
     * 自定义记录sql的方式
     */
    @Setter
    @Getter
    private static RecordSqlHandler customRecordSqlHandler = sqlLog -> {
    };

    /**
     * 自动表注解拦截器
     */
    @Setter
    @Getter
    private static EmtAnnotationInterceptor emtAnnotationInterceptor = (includeAnnotations, excludeAnnotations) -> {
    };

    /**
     * 创建表拦截
     */
    @Setter
    @Getter
    private static BuildTableMetadataInterceptor buildTableMetadataInterceptor = (databaseDialect, tableMetadata) -> {
    };

    /**
     * 创建表拦截
     */
    @Setter
    @Getter
    private static CreateTableInterceptor createTableInterceptor = (databaseDialect, tableMetadata) -> {
    };

    /**
     * 修改表拦截
     */
    @Setter
    @Getter
    private static ModifyTableInterceptor modifyTableInterceptor = (databaseDialect, tableMetadata, compareTableInfo) -> {
    };

    /**
     * 验证完成回调
     */
    @Setter
    @Getter
    private static ValidateFinishCallback validateFinishCallback = (status, databaseDialect, compareTableInfo) -> {
    };

    /**
     * 创建表回调
     */
    @Setter
    @Getter
    private static CreateTableFinishCallback createTableFinishCallback = (databaseDialect, tableMetadata) -> {
    };

    /**
     * 修改表回调
     */
    @Setter
    @Getter
    private static ModifyTableFinishCallback modifyTableFinishCallback = (databaseDialect, tableMetadata, compareTableInfo) -> {

    };
    /**
     * 单个表执行前后回调
     */
    @Setter
    @Getter
    private static RunStateCallback runStateCallback = new RunStateCallback() {
        @Override
        public void before(Class<?> tableClass) {
        }

        @Override
        public void after(Class<?> tableClass) {
        }
    };

    private final static Map<String, IStrategy<? extends TableMetadata, ? extends CompareTableInfo, ?>> STRATEGY_MAP = new HashMap<>();

    public static void addStrategy(IStrategy<? extends TableMetadata, ? extends CompareTableInfo, ?> strategy) {
        STRATEGY_MAP.put(strategy.databaseDialect(), strategy);
        JavaTypeToDatabaseTypeConverter.addTypeMapping(strategy.databaseDialect(), strategy.typeMapping());
    }

    public static IStrategy<?, ?, ?> getStrategy(String databaseDialect) {
        return STRATEGY_MAP.get(databaseDialect);
    }

    public static Collection<IStrategy<?, ?, ?>> getAllStrategy() {
        return STRATEGY_MAP.values();
    }

}
