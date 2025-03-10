package com.xyf.emt.common.field;

import java.lang.annotation.*;


/**
 * @Author: 熊韵飞
 * @Description: 标记字段不为空
 */

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ColumnNotNull {
    boolean value() default true;
}
