package com.xyf.emt.common.field;

import java.lang.annotation.*;

/**
 * @Author: 熊韵飞
 * @Description: 字段的备注
 */

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ColumnComment {

    /**
     * @return 列注释
     */
    String value();
}