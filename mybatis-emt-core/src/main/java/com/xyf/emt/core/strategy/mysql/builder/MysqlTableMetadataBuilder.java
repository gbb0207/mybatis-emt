package com.xyf.emt.core.strategy.mysql.builder;

import com.xyf.emt.common.mysql.MysqlCharset;
import com.xyf.emt.common.mysql.MysqlEngine;
import com.xyf.emt.core.EmtGlobalConfig;
import com.xyf.emt.core.builder.IndexMetadataBuilder;
import com.xyf.emt.core.config.PropertyConfig;
import com.xyf.emt.core.strategy.IndexMetadata;
import com.xyf.emt.core.strategy.mysql.data.MysqlColumnMetadata;
import com.xyf.emt.core.strategy.mysql.data.MysqlTableMetadata;
import com.xyf.emt.core.utils.BeanClassUtil;
import com.xyf.emt.core.utils.StringUtils;
import com.xyf.emt.core.utils.TableBeanUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;

@Slf4j
public class MysqlTableMetadataBuilder {    // 单个方法，返回实体类注解解析出的元数据

    public static MysqlTableMetadata build(Class<?> clazz) {

        String tableName = TableBeanUtils.getTableName(clazz);
        String tableComment = TableBeanUtils.getTableComment(clazz);

        // 这里的构造器只有 MysqlTableMetadata 其父类的构造器，还有其本身的所有属性没有设置！
        // 默认字符集、默认排序规则、引擎、所有列、所有索引。
        MysqlTableMetadata mysqlTableMetadata = new MysqlTableMetadata(clazz, tableName, tableComment);

        // 设置表字符集和排序规则
        String charset;
        String collate;

        // 从注解中提取字符集和排序规则
        MysqlCharset mysqlCharsetAnno = EmtGlobalConfig.getEmtAnnotationFinder().find(clazz, MysqlCharset.class);
        if (mysqlCharsetAnno != null) {
            charset = mysqlCharsetAnno.charset();
            collate = mysqlCharsetAnno.collate();
        } else {    // 如果没用注解主动声明字符集，就用默认字符集创建
            PropertyConfig emtProperties = EmtGlobalConfig.getEmtProperties();
            // yml 中如果也没有 emt: mysql : xxx-default-xxx，那么下面两个也是 null~
            charset = emtProperties.getMysql().getTableDefaultCharset();
            collate = emtProperties.getMysql().getTableDefaultCollation();
        }
        if (StringUtils.hasText(charset) && StringUtils.hasText(collate)) {
            // 获取表字符集
            mysqlTableMetadata.setCharacterSet(charset);
            // 字符排序
            mysqlTableMetadata.setCollate(collate);
        }

        // 从注解中提取表引擎
        MysqlEngine mysqlEngine = EmtGlobalConfig.getEmtAnnotationFinder().find(clazz, MysqlEngine.class);
        if (mysqlEngine != null) {
            mysqlTableMetadata.setEngine(mysqlEngine.value());
        }

        // 获取一个类下的所有 Field 字段，实际里面洗了一下避免父类继承重名问题
        List<Field> fields = BeanClassUtil.listAllFieldForColumn(clazz);

        // 从注解中提取所有列，会排除打了 @Ignore 的属性。
        // 相当重要的部分
        List<MysqlColumnMetadata> columnMetadataList = new MysqlColumnMetadataBuilder().buildList(clazz, fields);
        mysqlTableMetadata.setColumnMetadataList(columnMetadataList);

        // 从注解中提取所有索引，会排除打了 @Ignore 的属性。
        List<IndexMetadata> indexMetadataList = new IndexMetadataBuilder().buildList(clazz, fields);
        mysqlTableMetadata.setIndexMetadataList(indexMetadataList);

        return mysqlTableMetadata;
    }
}
