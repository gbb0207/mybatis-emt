package com.xyf.emt.core.recordsql;

import com.xyf.emt.core.EmtGlobalConfig;
import com.xyf.emt.core.config.PropertyConfig;
import com.xyf.emt.core.dynamicds.DatasourceNameManager;
import com.xyf.emt.core.dynamicds.IDataSourceHandler;
import com.xyf.emt.core.dynamicds.SqlSessionFactoryManager;
import com.xyf.emt.core.strategy.IStrategy;
import com.xyf.emt.core.utils.StringUtils;
import com.xyf.emt.core.utils.TableBeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class RecordSqlDbHandler implements RecordSqlHandler {   // 实际就是做一个表备份，备份形式是在同一个数据库实例中
    @Override
    public void record(EmtExecuteSqlLog emtExecuteSqlLog) {

        PropertyConfig.RecordSqlProperties recordSqlConfig = EmtGlobalConfig.getEmtProperties().getRecordSql();

        // 优先使用自定义的表名，没有则根据统一的风格定义表名。只会更改表名，但不会变更 EmtExecuteSqlLog 的任何字段。
        String tableName = recordSqlConfig.getTableName();
        if (StringUtils.noText(tableName)) {
            tableName = TableBeanUtils.getTableName(EmtExecuteSqlLog.class);  // 默认按 EmtExecuteSqlLog 驼峰转下划线命名
        }

        // 判断表是否存在，不存在则创建
        SqlSessionFactory sqlSessionFactory = SqlSessionFactoryManager.getSqlSessionFactory();
        try (SqlSession sqlSession = sqlSessionFactory.openSession();
             Connection connection = sqlSession.getConnection()) {
            // Mysql中，对应就是数据库名
            String catalog = connection.getCatalog();
            // Mysql中，不做要求，一般就是空字符
            String schema = StringUtils.hasText(emtExecuteSqlLog.getTableSchema()) ? emtExecuteSqlLog.getTableSchema() : connection.getSchema();
            boolean tableNotExit = !connection.getMetaData().getTables(catalog, schema, tableName, new String[]{"TABLE"}).next();
            connection.setAutoCommit(false);
            if (tableNotExit) { // 表不存在
                // 初始化表，就是按 EmtExecuteSqlLog 的字段来创建一张表
                initTable(connection);
                log.info("初始化sql记录表：{}", tableName);
            }
            // 插入数据
            insertLog(tableName, emtExecuteSqlLog, connection);
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void insertLog(String tableName, EmtExecuteSqlLog emtExecuteSqlLog, Connection connection) throws SQLException {
        /* 插入数据 */
        Class<EmtExecuteSqlLog> sqlLogClass = EmtExecuteSqlLog.class;
        // 筛选列，排除掉 @Ignore 标注的属性
        List<Field> columnFields = Arrays.stream(sqlLogClass.getDeclaredFields())
                .filter(field -> TableBeanUtils.isIncludeField(field, sqlLogClass))
                .collect(Collectors.toList());
        // 根据统一的风格定义列名
        List<String> columns = columnFields.stream()
                .map(field -> TableBeanUtils.getRealColumnName(sqlLogClass, field)) // 这里是属性名大驼峰转蛇形，这个方法原本是按框架内注解里的值命名，没有就驼峰转蛇形
                .collect(Collectors.toList());
        // 获取每一列的值
        // 思考，为什么要用反射取值？为什么不直接 emtExecuteSqlLog.getExecutionTime() 这样主动对每个没有 @Ignore 的属性提取数据？
        // 因为，假如后面对 EmtExecuteSqlLog 要扩展，又新增了一些备份需要的属性，或对某些字段加上了 @Ignore，那么这里主动提取，就会显得耦合度过高。。。。
        // 所以这里通过反射对每个字段提取值，传入的 emtExecuteSqlLog 按照 columnFields 筛选后的 Field 对象来取值，并存在一个值列表中。
        List<Object> values = columnFields.stream().map(field -> {  // 这里 field 是指 columnFields 的每一个 Field 对象，这里好像是废话。。。
            try {
                field.setAccessible(true);  // 通过反射来允许访问受访问控制限制的成员，相当于变更访问修饰符。
                return field.get(emtExecuteSqlLog);   // 用于获取指定对象中此字段的值，注意 emtExecuteSqlLog 传入的这个对象已经赋值了，在 IStrategy.executeSql() 中
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        // 执行数据插入
        // 1.这里使用原始的 jdbc 方式，先整一个含占位符的字符串
        // INSERT INTO auto_table_execute_sql_log (table_schema, table_name, sql_statement, version, execution_time, execution_end_time) VALUES (?, ?, ?, ?, ?, ?)
        String insertSql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, String.join(", ", columns), IntStream.range(0, values.size()).mapToObj(i -> "?").collect(Collectors.joining(", ")));
        log.info("插入SQL记录：{}", insertSql);

        // 2.通过含占位符的预备插入语句，生成一个预处理对象。
        PreparedStatement preparedStatement = connection.prepareStatement(insertSql);
        for (int i = 0; i < values.size(); i++) {
            // 将插入值列表中的每个字符串设置到第 i+1 个占位符位置
            preparedStatement.setObject(i + 1, values.get(i));
        }
        preparedStatement.executeUpdate();
    }

    private static void initTable(Connection connection) throws SQLException {

        IDataSourceHandler datasourceHandler = EmtGlobalConfig.getDatasourceHandler();
        String datasourceName = DatasourceNameManager.getDatasourceName();
        String databaseDialect = datasourceHandler.getDatabaseDialect(datasourceName);  // 这个方法，典。。传进去的跟返回值完全无关。直接按配置文件的数据源返回

        IStrategy<?, ?, ?> createTableStrategy = EmtGlobalConfig.getStrategy(databaseDialect);
        // sql 备份表就按 EmtExecuteSqlLog 来创建
        List<String> initTableSql = createTableStrategy.createTable(EmtExecuteSqlLog.class);

        try (Statement statement = connection.createStatement()) {
            for (String sql : initTableSql) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            throw new RuntimeException("初始化sql记录表失败", e);
        }
    }
}
