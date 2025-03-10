package com.xyf.emt.common.index;

import java.lang.annotation.*;

/**
 * @Author: 熊韵飞
 * @Description: 设置多个字段的索引
 */

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JointIndexes {

    /**
     * @return 索引集合
     */
    JointIndex[] value();

}