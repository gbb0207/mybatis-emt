package com.xyf.emt.common.index;

import com.xyf.emt.common.enums.IndexTypeEnum;

import java.lang.annotation.*;

/**
 * @Author: 熊韵飞
 * @Description: 索引
 */

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Index {

    /**
     * 索引名。配置文件中应有默认前缀~
     */
    String name() default "";

    /**
     * @return 索引类型。默认为普通索引。
     */
    IndexTypeEnum type() default IndexTypeEnum.NORMAL;

    /**
     * @return 索引注释
     */
    String comment() default "";
}
