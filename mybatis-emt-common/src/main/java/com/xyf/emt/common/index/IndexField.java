package com.xyf.emt.common.index;

import com.xyf.emt.common.enums.IndexSortTypeEnum;

import java.lang.annotation.*;

/**
 * @Author: 熊韵飞
 * @Description: 索引字段的详细描述
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.ANNOTATION_TYPE})
public @interface IndexField {

    /**
     * @return 字段名
     */
    String field();

    /**
     * @return 字段排序方式
     */
    IndexSortTypeEnum sort();
}
