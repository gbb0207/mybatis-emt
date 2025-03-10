package com.xyf.emt.core.strategy.mysql.builder;

import com.xyf.emt.common.enums.DefaultValueEnum;
import com.xyf.emt.core.strategy.mysql.data.MysqlColumnMetadata;
import com.xyf.emt.core.strategy.mysql.data.MysqlTypeHelper;
import com.xyf.emt.core.utils.StringConnectHelper;
import com.xyf.emt.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ColumnSqlBuilder {

    /**
     * 生成字段相关的SQL片段
     */
    public static String buildSql(MysqlColumnMetadata columnMetadata) { // 宏观作用是依据 MysqlColumnMetadata 生成 sql 字符串
        // 例子：`name` varchar(100) NULL DEFAULT '张三' COMMENT '名称'
        // 例子：`id` int(32) NOT NULL AUTO_INCREMENT COMMENT '主键'

        // newInstance() 就是构造器，静态工厂方法，相比于直接使用 new 关键字，构造器总是返回当前类的实例，没有变通的余地；静态工厂方法可以灵活决定返回值。
        // 不用构造器而用静态工厂方法，是因为这样实例化后可以返回任意值，而不是一定非要是其本身的类型对象！这里就返回的不是 StringConnectHelper 类型对象，而是 String 类型。
        return StringConnectHelper.newInstance("`{columnName}` {typeAndLength} {qualifier} {character} {collate} {null} {default} {autoIncrement} {columnComment} {position}")
                // 必备的，即使没值也会在 TableBeanUtils.getTableName() 中自动属性名驼峰转蛇形，不可能为空
                .replace("{columnName}", columnMetadata.getName())
                // 必备的，即使没值也会在 JavaTypeToDatabaseTypeConverter.convert() 中自动 java 类型转数据库类型，不可能为空
                .replace("{typeAndLength}", MysqlTypeHelper.getFullType(columnMetadata.getType()))
                // 这下面都是非必备的建表列字段，
                // 添加二进制、无符号、补零 等修饰符，UNSIGNED 和 ZEROFILL，不知道去翻文档
                .replace("{qualifier}", () -> {
                    Set<String> qualifiers = new HashSet<>();
                    if (columnMetadata.isUnsigned()) {
                        qualifiers.add("UNSIGNED");
                    }
                    if (columnMetadata.isZerofill()) {
                        qualifiers.add("UNSIGNED");
                        qualifiers.add("ZEROFILL");
                    }
                    return String.join(" ", qualifiers);
                })
                .replace("{character}", () -> {
                    String characterSet = columnMetadata.getCharacterSet();
                    if (StringUtils.hasText(characterSet)) {
                        return "CHARACTER SET " + characterSet;
                    }
                    return "";
                })
                .replace("{collate}", () -> {
                    String collate = columnMetadata.getCollate();
                    if (StringUtils.hasText(collate)) {
                        return "COLLATE " + collate;
                    }
                    return "";
                })
                .replace("{null}", columnMetadata.isNotNull() ? "NOT NULL" : "NULL")
                .replace("{default}", () -> {
                    // 指定NULL
                    DefaultValueEnum defaultValueType = columnMetadata.getDefaultValueType();
                    if (defaultValueType == DefaultValueEnum.NULL) {
                        return "DEFAULT NULL";
                    }
                    // 指定空字符串
                    if (defaultValueType == DefaultValueEnum.EMPTY_STRING) {
                        return "DEFAULT ''";
                    }
                    // 自定义
                    String defaultValue = columnMetadata.getDefaultValue();
                    if (DefaultValueEnum.isCustom(defaultValueType) && StringUtils.hasText(defaultValue)) {
                        return "DEFAULT " + defaultValue;
                    }
                    return "";
                })
                .replace("{autoIncrement}", columnMetadata.isAutoIncrement() ? "AUTO_INCREMENT" : "")
                .replace("{columnComment}", StringUtils.hasText(columnMetadata.getComment()) ? "COMMENT '" + columnMetadata.getComment() + "'" : "")
                .replace("{position}", () -> {
                    if (StringUtils.hasText(columnMetadata.getNewPreColumn())) {
                        return "AFTER `" + columnMetadata.getNewPreColumn() + "`";
                    }
                    if ("".equals(columnMetadata.getNewPreColumn())) {
                        return "FIRST";
                    }
                    return "";
                })
                .toString();
    }
}
