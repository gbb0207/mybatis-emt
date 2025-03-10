package com.xyf.emt.common.index;

import java.lang.annotation.*;

/**
 * @Author: 熊韵飞
 * @Description: 指定主键
 */

@Target({ElementType.FIELD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PrimaryKey {

    /**
     * @return 自增
     */
    boolean value();
}
