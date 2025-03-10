package com.xyf.emt.common.mysql;


import java.lang.annotation.*;

/**
 * @Author: 熊韵飞
 * @Description: 指定MySQL字段的字符编码和排序规则
 */

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MysqlColumnCharset {

    /**
     * @return 字符集
     */
    String value();

    /**
     * @return 字符排序
     */
    String collate();
}
