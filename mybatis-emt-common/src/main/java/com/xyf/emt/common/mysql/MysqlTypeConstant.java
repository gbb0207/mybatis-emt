package com.xyf.emt.common.mysql;

/**
 * @Author: 熊韵飞
 * @Description: mysql字段类型
 */

public interface MysqlTypeConstant {

    /**
     * 整数
     */
    String INT = "int";
    String TINYINT = "tinyint";
    String SMALLINT = "smallint";
    String MEDIUMINT = "mediumint";
    String BIGINT = "bigint";
    /**
     * 小数
     */
    String FLOAT = "float";
    String DOUBLE = "double";
    String DECIMAL = "decimal";
    /**
     * 字符串
     */
    String CHAR = "char";
    String VARCHAR = "varchar";
    String TEXT = "text";
    String TINYTEXT = "tinytext";
    String MEDIUMTEXT = "mediumtext";
    String LONGTEXT = "longtext";
    /**
     * 枚举
     */
    String ENUM = "enum";
    String SET = "set";
    /**
     * 日期
     */
    String YEAR = "year";
    String TIME = "time";
    String DATE = "date";
    String DATETIME = "datetime";
    String TIMESTAMP = "timestamp";
    /**
     * 二进制
     */
    String BIT = "bit";
    String BINARY = "binary";
    String VARBINARY = "varbinary";
    String BLOB = "blob";
    String TINYBLOB = "tinyblob";
    String MEDIUMBLOB = "mediumblob";
    String LONGBLOB = "longblob";
    /**
     * json
     */
    String JSON = "json";
}
