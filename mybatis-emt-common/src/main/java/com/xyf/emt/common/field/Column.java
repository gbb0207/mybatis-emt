package com.xyf.emt.common.field;

import com.xyf.emt.common.enums.DefaultValueEnum;

import java.lang.annotation.*;

/**
 * @Author: 熊韵飞
 * @Description: 字段属性合集
 */

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Column {

    /**
     *
     * @return 列名。为空字符串默认为属性名转蛇形命名。
     */
    String value() default "";

    /**
     * 建议提供枚举类供使用。
     * @return 字段类型。为空字符串会使用属性的数据类型进行转换。
     */
    String type() default "";

    /**
     * 字段长度，默认是-1，小于0相当于null。在浮点数中充当总长度 precision。
     * @return 字段长度。为-1则会在解析器中转化为合适的长度。
     */
    int length() default -1;

    /**
     * 小数点长度，默认是-1，小于0相当于null
     * @return 小数点长度。为-1则会在解析器中转化为合适的长度。
     */
    int scale() default -1;

    /**
     * @return 是否可为空，true是不可以，false是可以，默认为false
     */
    boolean notNull() default false;

    /**
     * @return 默认值，如果为空字符串则会默认为空。当插入数据时，如果未显式指定该字段的值，MySQL 会自动使用默认值填充。
     */
    String defaultValue() default "";

    /**
     * 默认值，默认为null。这个字段作用为：如果列值为空，是用未定义、空字符串还是Null来填入。
     * {@link ColumnDefault#type()}
     *
     * @return 默认值
     */
    DefaultValueEnum defaultValueType() default DefaultValueEnum.UNDEFINED;

    /**
     * @return 字段注释。默认为空。
     */
    String comment() default "";
}
