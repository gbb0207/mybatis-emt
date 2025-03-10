package com.xyf.emt.common.mysql;


import java.lang.annotation.*;

/**
 * @Author: 熊韵飞
 * @Description: 指定MySQL字符编码和排序规则
 */

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MysqlCharset {

    /**
     * @return 字符集
     */
    String charset();

    /**
     * @return 字符排序
     */
    String collate();
}
