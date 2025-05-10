package com.xyf.emt.test.dynamicdatasource.dynamicdatasourceframe;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 读取配置文件中的主从数据源，注册到容器中（集成的 Spring 数据源管理）
 */
@Configuration
public class DynamicDataSourceConfig {  // 1.先将多数据源注册

    /**
     * 最后执行，先执行非 @Primary 的方法，最后返回一个 Map<数据源标识, 数据源Bean> 的动态数据源
     * @return 返回的自定义的动态数据源 DynamicDataSource，在执行语句时自动根据数据源标识来切换容器中配置好的数据源实例
     */
    @Bean
    @Primary
    public DataSource dynamicDataSource() {
        Map<Object, Object> dataSourceMap = new HashMap<>(2);   // 测试写死，一主一从
        dataSourceMap.put(DataSourceConstants.DS_KEY_MASTER, masterDataSource());   // 等下面两个先注册完后，调用一次
        dataSourceMap.put(DataSourceConstants.DS_KEY_SLAVE, slaveDataSource());
        // 设置动态数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        // 设置所有可用的数据源
        // 这是 DynamicDataSource.determineCurrentLookupKey() 可以在执行 sql 时自动切换数据源的凭据，按 map 中给的值来切换指定数据源连接对象
        dynamicDataSource.setTargetDataSources(dataSourceMap);
        // 设置默认数据源（当无法获取上下文键时使用），默认即 master。例如 @Ds("a") 中标注了错误的数据源类型，那么就直接用 master
        dynamicDataSource.setDefaultTargetDataSource(masterDataSource());

        return dynamicDataSource;
    }

    @Bean("master")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean("slave")
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }
}
