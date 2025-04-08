package com.xyf.emt.core.dynamicds;

import lombok.NonNull;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public interface IDataSourceHandler {

    Logger log = LoggerFactory.getLogger(IDataSourceHandler.class);

    /**
     * 开始分析处理模型
     * 处理ignore and repeat表
     *
     * @param classList 待处理的类
     * @param consumer  实体消费回调
     */
    default void handleAnalysis(Set<Class<?>> classList, BiConsumer<String, Set<Class<?>>> consumer) {  // Consumer 不会返回值哦~

        // <数据源，Set<实体类>>，其中表就是扫描出的打了 @EntityTable 注解的实体类！
//          实际流程就是：
//              1.取出打了 @EntityTable 的实体类集合（打了就是需要自动建表的），如果没有主动实现此接口，将由此接口的默认实现 DefaultDataSourceHandler 来完成对
//                getDataSourceName() 的实现。实际默认就返回了一个 ""。具体实现可以见 DynamicDataSourceHandler。如果是动态数据源情况，这里的需要搭配一个自定义
//                注解 @Ds（实际框架不提供，此项目有但在测试包里），来用 getDataSourceName() 读取每个实体类上的 @Ds 中的值来返回。
//              2.遍历集合。
//              3.使用数据源，默认空实现。实际实现可以是一个动态数据源处理器来实现，作用就是传入一个 dataSource 来读取写好的 mybatis-config-xxx.xml，来修改
//                SqlSessionFactoryManager 中默认用框架默认读取的 SqlSession（默认读取 yml 配置文件来创建，动态数据源的话就不需要将数据源信息配置在 yml 文件中，
//                而是返璞归真写多个数据源的 mybatis 配置文件，用流读取 mybatis 配置文件来动态修改 SqlSessionFactory）
//                修改 SqlSessionFactory 是因为后面紧接着的就会取方言，然后根据执行具体 sql。
//              4.自动取方言，传的实参都没用。。
//              5.consumer 回调传入方言和实体Set。
        Map<String, Set<Class<?>>> needHandleTableMap = classList.stream()  // 注意下方 getDataSourceName()，需要配合 @Ds 注解标在实体类上（自定义注解！）
                .collect(Collectors.groupingBy(this::getDataSourceName, Collectors.toSet()));   // groupingBy() 会返回一个 Map，好好学好好用

        needHandleTableMap.forEach((dataSource, entityClasses) -> { // 没有主动指定数据源，所有实体类的数据源都默认为空字符串，走默认的 mysql 策略
            log.info("使用数据源：{}", dataSource);
            // 使用数据源。
            this.useDataSource(dataSource);
            DatasourceNameManager.setDatasourceName(dataSource);
            try {
                // 根据数据库名取方言，没用动态数据源处理器，那么这里用的是 EmtAutoConfig 给予的默认 SqlSessionFactory，返回 yml 里的数据源
                String databaseDialect = this.getDatabaseDialect(dataSource);   // 例如返回 MySQL
                log.info("数据库方言（{}）", databaseDialect);
                // 注意，就这里执行了 consumer 一次，回到 BootStrap 的地方看 lambda
                consumer.accept(databaseDialect, entityClasses);    // 数据源名字符串(如"MySQL")和Set<实体类>
            } finally {
                log.info("清理数据源：{}", dataSource);
                this.clearDataSource(dataSource);
                DatasourceNameManager.cleanDatasourceName();
            }
        });
    }


    /**
     * 自动获取当前数据源的方言
     *
     * @param dataSource 数据源名称
     * @return 返回数据方言
     */
    default String getDatabaseDialect(String dataSource) {

        // 获取 Configuration 对象，如果有动态数据源处理器（DynamicDataSourceHandler）类似的修改了 useDataSource()，那么这里的 SqlSessionFactory 就可能会被修改
        Configuration configuration = SqlSessionFactoryManager.getSqlSessionFactory().getConfiguration();

        try (Connection connection = configuration.getEnvironment().getDataSource().getConnection()) {
            // 通过连接获取 DatabaseMetaData 对象
            DatabaseMetaData metaData = connection.getMetaData();
            log.info("数据库链接 => {}", metaData.getURL());
            // 获取数据库方言
            return metaData.getDatabaseProductName();   // 如果是 yml 形式配的静态数据源，那么这里就会按 yml 中的数据源返回
        } catch (SQLException e) {
            throw new RuntimeException("获取数据方言失败", e);
        }
    }

    /**
     * 切换指定的数据源
     *
     * @param dataSourceName 数据源名称
     */
    void useDataSource(String dataSourceName);

    /**
     * 清除当前数据源
     *
     * @param dataSourceName 数据源名称
     */
    void clearDataSource(String dataSourceName);

    /**
     * 获取指定类的数据库数据源
     *
     * @param clazz 指定类
     * @return 数据源名称，表分组的依据，届时，根据该值分组所有的表，同一数据源下的统一处理
     */
    @NonNull String getDataSourceName(Class<?> clazz);
}
