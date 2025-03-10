package com.xyf.emt.common;

import java.lang.annotation.*;

/**
 * @Author: 熊韵飞
 * @Description: 忽略某个可注解元素。该注解会直接影响元素是否能被找到。
 */

@Target({ElementType.FIELD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ignore {
}
