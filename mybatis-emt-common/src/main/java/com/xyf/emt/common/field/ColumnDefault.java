package com.xyf.emt.common.field;

import com.xyf.emt.common.enums.DefaultValueEnum;

import java.lang.annotation.*;

/**
 * @Author: 熊韵飞
 * @Description: 字段的默认值
 */

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ColumnDefault {

    /**
     * @return 列的默认值类型
     */
    DefaultValueEnum type() default DefaultValueEnum.UNDEFINED;

    /**
     * @return 列的默认值
     */
    String value() default "";
}
