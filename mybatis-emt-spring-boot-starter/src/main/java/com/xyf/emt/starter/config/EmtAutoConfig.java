package com.xyf.emt.starter.config;

import com.xyf.emt.core.EmtAnnotationFinder;
import com.xyf.emt.core.EmtGlobalConfig;
import com.xyf.emt.core.EmtOrmFrameAdapter;
import com.xyf.emt.core.callback.CreateTableFinishCallback;
import com.xyf.emt.core.callback.ModifyTableFinishCallback;
import com.xyf.emt.core.callback.RunStateCallback;
import com.xyf.emt.core.callback.ValidateFinishCallback;
import com.xyf.emt.core.config.PropertyConfig;
import com.xyf.emt.core.converter.JavaTypeToDatabaseTypeConverter;
import com.xyf.emt.core.dynamicds.IDataSourceHandler;
import com.xyf.emt.core.dynamicds.SqlSessionFactoryManager;
import com.xyf.emt.core.interceptor.EmtAnnotationInterceptor;
import com.xyf.emt.core.interceptor.BuildTableMetadataInterceptor;
import com.xyf.emt.core.interceptor.CreateTableInterceptor;
import com.xyf.emt.core.interceptor.ModifyTableInterceptor;
import com.xyf.emt.core.recordsql.RecordSqlHandler;
import com.xyf.emt.core.strategy.CompareTableInfo;
import com.xyf.emt.core.strategy.IStrategy;
import com.xyf.emt.core.strategy.TableMetadata;
import com.xyf.emt.starter.properties.EmtProperties;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import com.xyf.emt.starter.util.CustomAnnotationFinder;

/**
 * @Author: 熊韵飞
 * @Description:
 */

@AutoConfigureAfter({DataSourceAutoConfiguration.class})    // 需要等待 mybatis/mp 执行完才执行，所以慢于 EmtImportRegister，可以在 spring 启动时就找到默认包
public class EmtAutoConfig {

    public EmtAutoConfig(   // spring 会默认执行这个构造器
            SqlSessionTemplate sqlSessionTemplate,
            EmtProperties emtProperties,
            ObjectProvider<IStrategy<? extends TableMetadata, ? extends CompareTableInfo, ?>> strategies,
            ObjectProvider<EmtAnnotationFinder> emtAnnotationFinder,
            ObjectProvider<EmtOrmFrameAdapter> emtOrmFrameAdapter,
            ObjectProvider<IDataSourceHandler> dynamicDataSourceHandler,
            ObjectProvider<RecordSqlHandler> recordSqlHandler,
            /* 拦截器 */
            ObjectProvider<EmtAnnotationInterceptor> emtAnnotationInterceptor,
            ObjectProvider<BuildTableMetadataInterceptor> buildTableMetadataInterceptor,
            ObjectProvider<CreateTableInterceptor> createTableInterceptor,
            ObjectProvider<ModifyTableInterceptor> modifyTableInterceptor,
            /* 回调事件 */
            ObjectProvider<CreateTableFinishCallback> createTableFinishCallback,
            ObjectProvider<ModifyTableFinishCallback> modifyTableFinishCallback,
            ObjectProvider<RunStateCallback> runStateCallback,
            ObjectProvider<ValidateFinishCallback> validateFinishCallback,

            ObjectProvider<JavaTypeToDatabaseTypeConverter> javaTypeToDatabaseTypeConverter) {

        // 默认设置全局的SqlSessionFactory
        SqlSessionFactoryManager.setSqlSessionFactory(sqlSessionTemplate.getSqlSessionFactory());

        // 设置全局的配置
        PropertyConfig propertiesConfig = emtProperties.toConfig();
        // 假如有注解扫描的包，就覆盖设置
        if (EmtImportRegister.basePackagesFromAnno != null) {
            propertiesConfig.setModelPackage(EmtImportRegister.basePackagesFromAnno);
        }
        EmtGlobalConfig.setEmtProperties(propertiesConfig); // 只有这个参数是必备的，下方都是非必备的

        // 假如有自定的注解扫描器，就使用自定义的注解扫描器。没有，则设置内置的注解扫描器
        // 虽然这里是一个类，但是和下方全局配置类中的接口匿名内部类没有区别。只是由于这个接口默认方法不足以让实现的匿名内部类有作用，需要一个类来完成重写逻辑再让他默认
        EmtGlobalConfig.setEmtAnnotationFinder(emtAnnotationFinder.getIfAvailable(CustomAnnotationFinder::new));

        // 如果有自定义的数据库策略，则加载
        strategies.stream().forEach(EmtGlobalConfig::addStrategy);

        // 假如有自定义的orm框架适配器，就使用自定义的orm框架适配器，没有就不用，
        emtOrmFrameAdapter.ifAvailable(EmtGlobalConfig::setEmtOrmFrameAdapter);

        // 假如有自定义的动态数据源处理器，就使用自定义的动态数据源处理器
        dynamicDataSourceHandler.ifAvailable(EmtGlobalConfig::setDatasourceHandler);

        // 假如有自定义的SQL记录处理器，就使用自定义的SQL记录处理器
        recordSqlHandler.ifAvailable(EmtGlobalConfig::setCustomRecordSqlHandler);

        /* 拦截器 */
        // 假如有自定义的注解拦截器，就使用自定义的注解拦截器
        emtAnnotationInterceptor.ifAvailable(EmtGlobalConfig::setEmtAnnotationInterceptor);
        // 假如有自定义的创建表拦截器，就使用自定义的创建表拦截器
        buildTableMetadataInterceptor.ifAvailable(EmtGlobalConfig::setBuildTableMetadataInterceptor);
        // 假如有自定义的创建表拦截器，就使用自定义的创建表拦截器
        createTableInterceptor.ifAvailable(EmtGlobalConfig::setCreateTableInterceptor);
        // 假如有自定义的修改表拦截器，就使用自定义的修改表拦截器
        modifyTableInterceptor.ifAvailable(EmtGlobalConfig::setModifyTableInterceptor);

        /* 回调事件 */
        // 假如有自定义的创建表回调，就使用自定义的创建表回调
        createTableFinishCallback.ifAvailable(EmtGlobalConfig::setCreateTableFinishCallback);
        // 假如有自定义的修改表回调，就使用自定义的修改表回调
        modifyTableFinishCallback.ifAvailable(EmtGlobalConfig::setModifyTableFinishCallback);
        // 假如有自定义的单个表执行前后回调，就使用自定义的单个表执行前后回调
        runStateCallback.ifAvailable(EmtGlobalConfig::setRunStateCallback);
        // 假如有自定义的验证表回调，就使用自定义的验证表回调
        validateFinishCallback.ifAvailable(EmtGlobalConfig::setValidateFinishCallback);

        // 假如有自定义的java到数据库的转换器，就使用自定义的java到数据库的转换器
        javaTypeToDatabaseTypeConverter.ifAvailable(EmtGlobalConfig::setJavaTypeToDatabaseTypeConverter);
    }

}
