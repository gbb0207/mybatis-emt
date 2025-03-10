package com.xyf.emt.core.strategy.mysql.builder;

import com.xyf.emt.common.enums.IndexTypeEnum;
import com.xyf.emt.core.strategy.IndexMetadata;
import com.xyf.emt.core.strategy.mysql.data.MysqlColumnMetadata;
import com.xyf.emt.core.strategy.mysql.data.MysqlTableMetadata;
import com.xyf.emt.core.utils.StringConnectHelper;
import com.xyf.emt.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CreateTableSqlBuilder {    // 生成建表 sql 字符串，即：CREATE TABLE xxx

    /**
     * 构建创建新表的SQL
     *
     * @param mysqlTableMetadata 参数
     * @return sql
     */
    public static String buildSql(MysqlTableMetadata mysqlTableMetadata) {  // 形参为实体类上标的注解对应的建表所需属性值

        // 从实体类注解值中提取内容
        String name = mysqlTableMetadata.getTableName();    // 表名
        List<MysqlColumnMetadata> mysqlColumnMetadataList = mysqlTableMetadata.getColumnMetadataList(); // 所有列
        List<IndexMetadata> indexMetadataList = mysqlTableMetadata.getIndexMetadataList();  // 所有索引
        String characterSet = mysqlTableMetadata.getCharacterSet(); // 字符集
        String collate = mysqlTableMetadata.getCollate();   // 排序规则
        String engine = mysqlTableMetadata.getEngine(); // 引擎
        String comment = mysqlTableMetadata.getComment();   // 注释

        // 1.字段、索引（主键在这里处理，不作为表级选项）todo 没有外键，等着加
        // 记录所有处理过的字段/修改项，（利用数组结构，便于添加,分割），这里就是 MysqlTableMetadata 需要处理的字段
        List<String> addItems = new ArrayList<>();

        // 获取所有主键（至于表字段处理之前，为了主键修改notnull）
        List<String> primaries = new ArrayList<>();

        // 判断是主键，自动设置为 NOT NULL，并记录
        mysqlColumnMetadataList.forEach(columnData -> {
            if (columnData.isPrimary()) {
                columnData.setNotNull(true);
                primaries.add(columnData.getName());
            }
        });

        // 表字段处理，举个例子：
        // `id` int    NULL    ,`username` varchar(255)    NULL    ,`age` int    NULL    ,`phone` varchar(255)    NOT NULL    ,`present_time` timestamp    NULL DEFAULT CURRENT_TIMESTAMP   ,`united1` int    NULL    ,`united2` int    NULL
        addItems.add(
                mysqlColumnMetadataList.stream()
                        .sorted(Comparator.comparingInt(MysqlColumnMetadata::getPosition))  // 此处怎么来的，见 todo3
                        // 拼接每个字段的sql片段
                        .map(ColumnSqlBuilder::buildSql)    // 将从实体类属性的元数据转变为 sql，为 CREATE 语句中的列语句
                        .collect(Collectors.joining(","))
        );


        // 主键
        if (!primaries.isEmpty()) {
            String primaryKeySql = getPrimaryKeySql(primaries);
            addItems.add(primaryKeySql);
        }

        // 索引
        addItems.add(
                indexMetadataList.stream()
                        // 例子： UNIQUE INDEX `unique_name_age`(`name` ASC, `age` DESC) COMMENT '姓名、年龄索引' USING BTREE
                        .map(CreateTableSqlBuilder::getIndexSql)
                        // 同类型的索引，排在一起，SQL美化（按字典顺序排）
                        .sorted()
                        .collect(Collectors.joining(","))
        );

        // 2.引擎、字符集、排序规则、备注
        List<String> tableProperties = getTableProperties(engine, characterSet, collate, comment);

        // 3.开始组合
        // 组合字段、索引的 sql，过滤空字符项，逗号拼接
        String addSql = addItems.stream()   // 这是字段和索引的
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(","));

        // 组合引擎、字符集、排序规则、备注的 sql
        String propertiesSql = tableProperties.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(","));

        return "CREATE TABLE `{tableName}` ({addItems}) {tableProperties};"
                .replace("{tableName}", name)
                .replace("{addItems}", addSql)
                .replace("{tableProperties}", propertiesSql);
    }

    public static List<String> getTableProperties(String engine, String characterSet, String collate, String comment) {
        List<String> tableProperties = new ArrayList<>();

        // 引擎
        if (StringUtils.hasText(engine)) {
            tableProperties.add("ENGINE = " + engine);
        }
        // 字符集
        if (StringUtils.hasText(characterSet)) {
            tableProperties.add("CHARACTER SET = " + characterSet);
        }
        // 排序
        if (StringUtils.hasText(collate)) {
            tableProperties.add("COLLATE = " + collate);
        }
        // 备注
        if (StringUtils.hasText(comment)) {
            tableProperties.add(
                    "COMMENT = '{comment}'"
                            .replace("{comment}", comment)
            );
        }
        return tableProperties;
    }

    public static String getIndexSql(IndexMetadata indexMetadata) {
        // 例子： UNIQUE INDEX `unique_name_age`(`name` ASC, `age` DESC) COMMENT '姓名、年龄索引',
        return StringConnectHelper.newInstance("{indexType} INDEX `{indexName}`({columns}) {indexComment}")
                .replace("{indexType}", indexMetadata.getType() == IndexTypeEnum.UNIQUE ? "UNIQUE" : "")
                .replace("{indexName}", indexMetadata.getName())
                .replace("{columns}", () -> {
                    List<IndexMetadata.IndexColumnParam> columnParams = indexMetadata.getColumns();
                    return columnParams.stream().map(column ->
                            // 例：`name` ASC
                            "`{column}` {sortMode}"
                                    .replace("{column}", column.getColumn())
                                    .replace("{sortMode}", column.getSort() != null ? column.getSort().name() : "")
                    ).collect(Collectors.joining(","));
                })
                .replace("{indexComment}", StringUtils.hasText(indexMetadata.getComment()) ? "COMMENT '" + indexMetadata.getComment() + "'" : "")
                .toString();
    }

    public static String getPrimaryKeySql(List<String> primaries) {
        return "PRIMARY KEY ({primaries})"
                .replace(
                        "{primaries}",
                        primaries.stream()
                                .map(fieldName -> "`" + fieldName + "`")
                                .collect(Collectors.joining(","))
                );
    }
}
