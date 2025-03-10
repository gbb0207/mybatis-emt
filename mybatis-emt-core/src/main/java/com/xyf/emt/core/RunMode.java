package com.xyf.emt.core;

import lombok.Getter;

/**
 * 执行模式
 */
@Getter
public enum RunMode {

    /**
     * 系统不做任何处理
     */
    none,
    /**
     * 系统启动时，会检查数据库中的表与java实体类是否匹配。如果不匹配，则启动失败
     */
    validate,
    /**
     * <p>系统启动时，会先将所有实体标注的表删除掉，然后重新建表
     * <p>注意，该模式会破坏原有数据
     */
    create,
    /**
     * <p>系统启动时，会自动判断哪些表是新建的，哪些字段要新增修改，哪些索引/约束要新增删除等
     * <p>该操作不会删除字段(更改字段名称的情况下，会认为是新增字段)
     * <p>如果需要从数据库强制删除实体上不存在的字段，请参考配置 auto-drop-column 配置项
     */
    update
}
