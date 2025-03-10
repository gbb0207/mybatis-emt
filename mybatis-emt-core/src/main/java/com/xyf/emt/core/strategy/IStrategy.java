package com.xyf.emt.core.strategy;

import com.xyf.emt.core.EmtGlobalConfig;
import com.xyf.emt.core.RunMode;
import com.xyf.emt.core.converter.DefaultTypeEnumInterface;
import com.xyf.emt.core.dynamicds.SqlSessionFactoryManager;
import com.xyf.emt.core.recordsql.EmtExecuteSqlLog;
import com.xyf.emt.core.recordsql.RecordSqlService;
import com.xyf.emt.core.utils.StringUtils;
import lombok.NonNull;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface IStrategy<TABLE_META extends TableMetadata, COMPARE_TABLE_INFO extends CompareTableInfo, MAPPER> {

    /*
        <TABLE_META extends TableMetadata, COMPARE_TABLE_INFO extends CompareTableInfo, MAPPER>
        此泛型的目的，就在于此接口实现类可以确定唯一类型为指定泛型。什么意思？举个例子，此接口中有个抽象方法：
        TABLE_META analyseClass(Class<?> beanClass);
        其实现类：
        class MysqlStrategy implements IStrategy<MysqlTableMetadata, MysqlCompareTableInfo, MysqlTablesMapper>
        确定了 MysqlTableMetadata 为 TABLE_META！同时指定了 TABLE_META analyseClass(Class<?> beanClass); 的实际重写形式为：
        MysqlTableMetadata analyseClass(Class<?> beanClass);
        实际在 MysqlStrategy 重写接口方法时，就不能指定返回值为其他 TableMetadata 实现类，只能是 MysqlTableMetadata。
     */

    Logger log = LoggerFactory.getLogger(IStrategy.class);

    /**
     * 获取mapper执行mapper的方法
     *
     * @param execute 要执行的SQL方法
     * @return 数据库类型
     */
    default <R> R executeReturn(Function<MAPPER, R> execute) {  // 传入的是 XxxTablesMapper 接口中的方法，即具体的 Mapper 方法

        // 从接口泛型上读取MapperClass
        Class<MAPPER> mapperClass = getMapperClass();   // 取出例如：MysqlTablesMapper

        // 具体执行
        try (SqlSession sqlSession = SqlSessionFactoryManager.getSqlSessionFactory().openSession()) {
            return execute.apply(sqlSession.getMapper(mapperClass));
        }
    }

    /**
     * 从接口泛型上读取MapperClass
     *
     * @return MapperClass
     */
    default Class<MAPPER> getMapperClass() {

        // 从接口泛型上读取MapperClass
        /*
            getGenericInterfaces()：这是 java.lang.Class 类的一个方法。它返回一个数组，其中包含了该类所实现的所有接口的 Type 对象。
            Type：是一个 Java 中的接口，它有多种子类型，如：Class（当接口没有泛型参数时）、ParameterizedType（当接口有泛型参数时）等。
            -------------------------------------------------------------------------------------------------------------------------------------
            这个数组的顺序通常与类声明中实现接口的顺序一致。例如，如果一个类 MyClass 声明为 class MyClass implements Interface1, Interface2，那么
            getGenericInterfaces() 返回的数组中，第一个元素对应 Interface1 的类型信息，第二个元素对应 Interface2 的类型信息。
            所以：[0] 表示获取 getGenericInterfaces() 返回的数组中的第一个元素，也就是这个类所实现的第一个接口的类型信息。
            -------------------------------------------------------------------------------------------------------------------------------------
            getActualTypeArguments()：这个数组包含了 ParameterizedType（也就是第一行获取的那个接口类型）里的所有泛型参数。
            例如：interface MyInterface<T, U, V>，如果某个类实现了它，getActualTypeArguments() 返回的数组就依次对应 T、U、V 的具体类型信息。
            所以：[2] 这里就是取出 IStrategy 实现类的第三个泛型参数，例如：MySQL Strategy 的 MysqlTablesMapper。
         */
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericInterfaces()[0];
        Class<MAPPER> mapperClass = (Class<MAPPER>) genericSuperclass.getActualTypeArguments()[2];

        // 如果没有注册 Mapper，则注册
        SqlSessionFactory sqlSessionFactory = SqlSessionFactoryManager.getSqlSessionFactory();
        Configuration configuration = sqlSessionFactory.getConfiguration();
        if (!configuration.hasMapper(mapperClass)) {
            configuration.addMapper(mapperClass);   // 添加成 MyBatis 的 SqlSession 中的一个 Mapper，可以直接通过接口方法执行 sql
        }

        return mapperClass;
    }

    /**
     * 开始分析实体集合
     *
     * @param entityClass 待处理的实体
     */
    default void start(Class<?> entityClass) {  // 从这里开始看！

        EmtGlobalConfig.getRunStateCallback().before(entityClass);    // 回调钩子，前置通知

        // 实际这个方法里都是 XxxTableMetadataBuilder.build(); Xxx 是方言名
        TABLE_META tableMetadata = this.analyseClass(entityClass);

        // 实际
        this.start(tableMetadata);

        EmtGlobalConfig.getRunStateCallback().after(entityClass); // 回调钩子，后置通知
    }

    /**
     * 开始分析实体
     *
     * @param tableMetadata 表元数据
     */
    default void start(TABLE_META tableMetadata) {
        // 拦截表信息，供用户自定义修改
        EmtGlobalConfig.getBuildTableMetadataInterceptor().intercept(this.databaseDialect(), tableMetadata);

        // 根据启动模式确定具体行为，默认是 update
        RunMode runMode = EmtGlobalConfig.getEmtProperties().getMode();

        // 目前只有 检查（表不一致不能启动），创建（删了重新建），追加（只加新有的表、字段、索引等）
        switch (runMode) {
            case validate:  // 系统启动时，会检查数据库中的表与 java 实体类是否匹配。如果不匹配，则启动失败。只做匹配，不创建表。应该叫 check
                validateMode(tableMetadata);    // 3
                break;
            case create:    // 系统启动时，会先将所有的表删除掉，然后根据 model 中配置的结构重新建表，该模式会清空原有数据。应该叫 reset。
                createMode(tableMetadata);  // 2
                break;
            case update:    // 系统启动时，会自动判断哪些表是新建的，哪些字段要新增修改，哪些索引/约束要新增删除等。应该叫 append，但不只有追加，没有的也会删除，这个名字没问题。
                updateMode(tableMetadata);  // 1
                break;
            default:    // none 这个枚举已经在 Bootstrap 中处理了，只要为 none 在 Bootstrap 开头判断后都不会往后进行。
                throw new RuntimeException(String.format("不支持的运行模式：%s", runMode));
        }
    }

    /**
     * 检查数据库数据模型与实体是否一致
     * 1. 检查数据库数据模型是否存在
     * 2. 检查数据库数据模型与实体是否一致
     *
     * @param tableMetadata 表元数据
     */
    default void validateMode(TABLE_META tableMetadata) {   // 3

        String schema = tableMetadata.getSchema();
        String tableName = tableMetadata.getTableName();

        // 检查数据库数据模型与实体是否一致
        boolean tableNotExist = this.checkTableNotExist(schema, tableName);
        if (tableNotExist) {
            EmtGlobalConfig.getValidateFinishCallback().validateFinish(false, this.databaseDialect(), null);
            throw new RuntimeException(String.format("启动失败，%s中不存在表%s", this.databaseDialect(), tableMetadata.getTableName()));
        }

        // 对比数据库表结构与新的表元数据的差异
        COMPARE_TABLE_INFO compareTableInfo = this.compareTable(tableMetadata);
        if (compareTableInfo.needModify()) {
            log.warn("{}表结构不一致：\n{}", tableMetadata.getTableName(), compareTableInfo.validateFailedMessage());
            EmtGlobalConfig.getValidateFinishCallback().validateFinish(false, this.databaseDialect(), compareTableInfo);
            throw new RuntimeException(String.format("启动失败，%s数据表%s与实体不匹配", this.databaseDialect(), tableMetadata.getTableName()));
        }
        EmtGlobalConfig.getValidateFinishCallback().validateFinish(true, this.databaseDialect(), compareTableInfo);
    }

    /**
     * 创建模式
     * <p>1. 删除表
     * <p>2. 新建表
     *
     * @param tableMetadata 表元数据
     */
    default void createMode(TABLE_META tableMetadata) { // 2

        String schema = tableMetadata.getSchema();
        String tableName = tableMetadata.getTableName();

        // 表是否存在的标记
        log.info("create模式，删除表：{}", tableName);
        // 直接尝试删除表
        String sql = this.dropTable(schema, tableName);
        this.executeSql(tableMetadata, Collections.singletonList(sql));

        // 新建表
        executeCreateTable(tableMetadata);
    }

    /**
     * 更新模式
     * 1. 检查表是否存在
     * 2. 不存在创建
     * 3. 检查表是否需要修改
     * 4. 需要修改就修改表
     *
     * @param tableMetadata 表元数据
     */
    default void updateMode(TABLE_META tableMetadata) { // 1

        String schema = tableMetadata.getSchema();
        String tableName = tableMetadata.getTableName();

        // 1.检查表存在与否，新创建表名不存在的表！
        // 检查表不存在，表不存在返回 false，通过 Connection 对象来读取连接的数据库信息（连接池对象创建时就已创建好，读取完 yml 配置文件时就已经创建好了）
        boolean tableNotExist = this.checkTableNotExist(schema, tableName);
        // 重要！当表不存在的时候，直接创建表
        if (tableNotExist) {    // 表不存在，此处为 true，因为上面方法叫 NotExist
            // 开始创建表！
            executeCreateTable(tableMetadata);  // 最重要！进去看。
            return;
        }

        // 2.当表存在，比对数据库表结构与表元数据的差异，即看实体类上的注解信息有没有变更，但要从
        COMPARE_TABLE_INFO compareTableInfo = this.compareTable(tableMetadata);
        if (compareTableInfo.needModify()) {    // COMPARE_TABLE_INFO 的值全是布尔值，这是一个断言方法，只要其中的属性有一个不为空那么就需要修改
            // 修改表信息
            log.info("修改表：{}", (StringUtils.hasText(schema) ? schema + "." : "") + tableName);

            // 修改表前拦截
            EmtGlobalConfig.getModifyTableInterceptor().beforeModifyTable(this.databaseDialect(), tableMetadata, compareTableInfo);

            // ********************** 重要：开始创建所有 sql，新增、修改、删除
            List<String> sqlList = this.modifyTable(compareTableInfo);
            this.executeSql(tableMetadata, sqlList);

            // 修改表后回调
            EmtGlobalConfig.getModifyTableFinishCallback().afterModifyTable(this.databaseDialect(), tableMetadata, compareTableInfo);
        }
    }

    /**
     * 执行创建表
     *
     * @param tableMetadata 表元数据
     */
    default void executeCreateTable(TABLE_META tableMetadata) { // 重要1：根据实体类给出的信息，生成建表sql，并执行sql

        String schema = tableMetadata.getSchema();
        String tableName = tableMetadata.getTableName();
        log.info("创建表：{}", (StringUtils.hasText(schema) ? schema + "." : "") + tableName);

        // 建表拦截
        EmtGlobalConfig.getCreateTableInterceptor().beforeCreateTable(this.databaseDialect(), tableMetadata);

        // 生成建表 sql，下面方法不同的数据源策略不同，即生成建表 sql 的字符串不同。
        // 重要，里面内容挺多的！！！！！！！！！！！！！！！！！
        List<String> sqlList = this.createTable(tableMetadata); // 返回的是一个不可变列表！而且这个列表只装一个元素。即只有一个建表 sql。

        // =》重要2：执行建表 sql
        this.executeSql(tableMetadata, sqlList);

        // 建表结束回调
        EmtGlobalConfig.getCreateTableFinishCallback().afterCreateTable(this.databaseDialect(), tableMetadata);
    }

    /**
     * 执行SQL
     *
     * @param tableMetadata 表元数据
     * @param sqlList       SQL集合
     */
    default void executeSql(TABLE_META tableMetadata, List<String> sqlList) {   // 重要2：执行建表sql
        SqlSessionFactory sqlSessionFactory = SqlSessionFactoryManager.getSqlSessionFactory();

        try (SqlSession sqlSession = sqlSessionFactory.openSession();
             Connection connection = sqlSession.getConnection()) {

            log.debug("开启事务");

            // 批量的 SQL 改为手动提交模式，后续执行的 SQL 语句不会立即提交，而是累积在一个事务当中。
            // 将多个相关的 SQL 操作视为一个整体，要么全部成功提交（通过调用connection.commit()），要么全部失败回滚（通过调用connection.rollback()）。
            connection.setAutoCommit(false);

            // sql 执行日志
            List<EmtExecuteSqlLog> emtExecuteSqlLogs = new ArrayList<>();

            try (Statement statement = connection.createStatement()) {  // Statement 对象主要用于执行静态 SQL 语句
                for (String sql : sqlList) {
                    // sql末尾添加;
                    if (!sql.endsWith(";")) {
                        sql += ";";
                    }

                    // 记录 sql 执行开始时间
                    long executionTime = System.currentTimeMillis();
                    // 执行任意 sql，此处为完成后的建表语句
                    statement.execute(sql);
                    // 记录 sql 执行结束时间
                    long executionEndTime = System.currentTimeMillis();

                    // 这里也就是一个静态工厂方法，对比全参构造而言，这里没有 version 字段，说明这个字段不是必须的。
                    // 注意，传入了具体执行的 sql。这里是具体存入 sql 备份表/文件的一条记录。
                    EmtExecuteSqlLog emtExecuteSqlLog = EmtExecuteSqlLog.of(tableMetadata.getEntityClass(), tableMetadata.getSchema(), tableMetadata.getTableName(), sql, executionTime, executionEndTime);
                    emtExecuteSqlLogs.add(emtExecuteSqlLog);

                    log.info("执行sql({}ms)：{}", executionEndTime - executionTime, sql);
                }
                // 提交
                log.debug("提交事务");
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException(String.format("执行SQL: \n%s\n期间出错", String.join("\n", sqlList)), e);
            }

            // 记录SQL，看完啦！！
            RecordSqlService.record(emtExecuteSqlLogs);   // 这里传入的是一条具体需要插入到备份表/文件的记录，EmtExecuteSqlLog 的实现类

        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接出错", e);
        }
    }

    /**
     * 检查表是否存在
     *
     * @param schema    schema
     * @param tableName 表名
     * @return 表详情
     */
    default boolean checkTableNotExist(String schema, String tableName) {   // 检查表是否不存在【不是存在】，如果表存在返回 false，表不存在返回 true
        // 获取 Configuration 对象
        Configuration configuration = SqlSessionFactoryManager.getSqlSessionFactory().getConfiguration();
        try (Connection connection = configuration.getEnvironment().getDataSource().getConnection()) {
            // 通过连接获取 DatabaseMetaData 对象，此连接是通过 springboot 配置文件（默认 hikari 连接池）创建的，即连接文件对应中 url 的数据库后给予的
            DatabaseMetaData metaData = connection.getMetaData();
            String connectionCatalog = connection.getCatalog(); // 对于 MySQL，就是对应相应的数据库名；对于Oracle来说，则是对应相应的数据库实例，可以不填，也可以直接使用 Connection 的实例对象中的 getCatalog() 方法返回的值填充；
            String connectionSchema = connection.getSchema();   // 对于 Oracle 也可以理解成对该数据库操作的所有者的登录名；MySQL 不做强制要求。
            /*
                getTables(数据库名称, 模式, 表名称, 类型标准)：获取表信息
                1.数据库名称，对于MySQL，则对应相应的数据库，对于Oracle来说，则是对应相应的数据库实例，可以不填，也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
                2.模式，可以理解为数据库的登录名，而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意，其登陆名必须是大写，不然的话是无法获取到相应的数据，而MySQL则不做强制要求。
                3.表名称，一般情况下如果要获取所有的表的话，可以直接设置为null，如果设置为特定的表名称，则返回该表的具体信息。
                4.类型标准,以数组形式传值，有"TABLE"、“VIEW”、“SYSTEM TABLE”、"GLOBAL TEMPORARY"、“LOCAL TEMPORARY”、“ALIAS” 和"SYNONYM"这几个经典的类型，一般使用”TABLE”，即获取所有类型为TABLE的表
                返回一个 ResultSet 对象，有 10 列：
                TABLE_CAT String => 表类别(可为 null)
                TABLE_SCHEM String => 表模式(可为 null)
                TABLE_NAME String => 表名称
                TABLE_TYPE String => 表类型。
                REMARKS String => 表的解释性注释
                TYPE_CAT String => 类型的类别(可为 null)
                TYPE_SCHEM String => 类型模式(可为null)
                TYPE_NAME String => 类型名称(可为 null)
                SELF_REFERENCING_COL_NAME String => 有类型表的指定 “identifier” 列的名称(可为 null)
                REF_GENERATION String
             */
            boolean exist = metaData.getTables(connectionCatalog, StringUtils.hasText(schema) ? schema : connectionSchema, tableName, new String[]{"TABLE"}).next();
            return !exist;
        } catch (SQLException e) {
            throw new RuntimeException("判断数据库是否存在出错", e);
        }
    }

    /**
     * 获取创建表的SQL
     *
     * @param clazz 实体
     * @return sql
     */
    default List<String> createTable(Class<?> clazz) {  // 只用于sql记录的数据库备份形式创建sql语句备份表
        TABLE_META tableMeta = this.analyseClass(clazz);
        return this.createTable(tableMeta);
    }

    /**
     * 策略对应的数据库方言，与数据库驱动中的接口{@link DatabaseMetaData#getDatabaseProductName()}实现返回值一致
     *
     * @return 方言
     */
    String databaseDialect();

    /**
     * java字段类型与数据库类型映射关系
     *
     * @return 映射
     */
    Map<Class<?>, DefaultTypeEnumInterface> typeMapping();

    /**
     * 根据表名删除表，生成删除表的SQL
     *
     * @param schema    schema
     * @param tableName 表名
     * @return SQL
     */
    String dropTable(String schema, String tableName);

    /**
     * 分析Bean，得到元数据信息
     *
     * @param beanClass 待分析的class
     * @return 表元信息
     */
    @NonNull
    TABLE_META analyseClass(Class<?> beanClass);

    /**
     * 生成创建表SQL
     *
     * @param tableMetadata 表元数据
     * @return SQL
     */
    List<String> createTable(TABLE_META tableMetadata);

    /**
     * 对比表与bean的差异
     *
     * @param tableMetadata 表元数据
     * @return 待修改的表信息描述
     */
    @NonNull
    COMPARE_TABLE_INFO compareTable(TABLE_META tableMetadata);

    /**
     * 生成修改表SQL
     *
     * @param compareTableInfo 修改表的描述信息
     * @return SQL
     */
    List<String> modifyTable(COMPARE_TABLE_INFO compareTableInfo);
}
