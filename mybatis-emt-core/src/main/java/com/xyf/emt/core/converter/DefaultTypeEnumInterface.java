package com.xyf.emt.core.converter;

/**
 * 不同数据库的字段类型不同，在 common 包下不同数据库的专有包里会定义 XxxTypeConstant，来存储自己的字段类型。
 * 此处用于 Java 字段类型到数据库字段类型的映射（IStrategy 的 typeMapping()），也用于对未在注解中显式声明默认长度（此接口实现类）
 */
public interface DefaultTypeEnumInterface {
    /**
     * 默认类型长度
     * @return 默认长度
     */
    Integer getDefaultLength();
    /**
     * 默认小数点后长度
     * @return 默认小数点后长度
     */
    Integer getDefaultDecimalLength();
    /**
     * 类型名称
     * @return 类型名称
     */
    String getTypeName();
}
