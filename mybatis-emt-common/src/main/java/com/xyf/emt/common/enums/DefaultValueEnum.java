package com.xyf.emt.common.enums;

/**
 * @Author: 熊韵飞
 * @Description: 默认值类型。当指定默认值类型(UNDEFINED除外)的时候，会忽略自定义默认值，并按照默认值的类型匹配值。
 */

public enum DefaultValueEnum {

    /**
     * 未定义：在注解中必须填写一个值，同时表示无意义。例如：
     * CREATE TABLE test_table (
     *     name VARCHAR(50)
     * );
     */
    UNDEFINED,
    /**
     * 空字符串：仅限于字符串类型。例如：
     * CREATE TABLE test_table (
     *     description VARCHAR(200) DEFAULT ''
     * );
     */
    EMPTY_STRING,
    /**
     * null值。例如：
     * CREATE TABLE test_table (
     *     address VARCHAR(200) DEFAULT NULL
     * );
     */
    NULL;

    public static boolean isValid(DefaultValueEnum defaultValueEnum) {
        return defaultValueEnum != null && defaultValueEnum != UNDEFINED;
    }

    public static boolean isCustom(DefaultValueEnum defaultValueEnum) {
        return !isValid(defaultValueEnum);
    }
}