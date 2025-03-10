package com.xyf.emt.common.mysql;


import java.lang.annotation.*;

/**
 * @Author: 熊韵飞
 * @Description: 指定MySQL引擎
 */

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MysqlEngine {

    /**
     * @return 引擎名称
     */
    String value();
}
