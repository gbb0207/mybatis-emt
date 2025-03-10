package com.xyf.emt.common.field;


import java.lang.annotation.*;


/**
 * @Author: 熊韵飞
 * @Description: 列名
 */

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ColumnName {

    /**
     * @return 列名
     */
    String value();
}
