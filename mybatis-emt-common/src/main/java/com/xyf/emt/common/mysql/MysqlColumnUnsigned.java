package com.xyf.emt.common.mysql;


import java.lang.annotation.*;

/**
 * @Author: 熊韵飞
 * @Description: 指定MySQL数字类型不允许负数，其范围从 0 开始
 */

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MysqlColumnUnsigned {

}
