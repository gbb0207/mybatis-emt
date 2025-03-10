package com.xyf.emt.common.enums;

/**
 * @Author: 熊韵飞
 * @Description: 索引类型枚举。
 */

public enum IndexTypeEnum {
    /**
     * 普通索引。最基本的索引类型，它没有任何限制，唯一任务就是加快系统对数据的访问速度。普通索引允许在定义索引的列中插入重复值和空值。
     */
    NORMAL,
    /**
     * 唯一索引。列的值必须唯一，允许有空值。如果是组合索引，则列值的组合必须唯一。
     */
    UNIQUE
}
