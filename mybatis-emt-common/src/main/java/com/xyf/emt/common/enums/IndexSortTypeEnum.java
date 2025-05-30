package com.xyf.emt.common.enums;

/**
 * @Author: 熊韵飞
 * @Description: 索引排序方法
 */

public enum IndexSortTypeEnum {

    /**
     * 正序排序
     */
    ASC,
    /**
     * 倒序排序
     */
    DESC;

    public static IndexSortTypeEnum parseFromMysql(String val) {

        // IndexTypeEnum.FULLTEXT类型的索引，没有排序值
        if (val == null) {
            return null;
        }

        switch (val) {
            case "A":
                return ASC;
            case "D":
                return DESC;
            // 等同 case "NULL":
            default:
                return null;
        }
    }
}