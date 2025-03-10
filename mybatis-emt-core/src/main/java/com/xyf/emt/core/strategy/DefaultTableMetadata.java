package com.xyf.emt.core.strategy;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class DefaultTableMetadata extends TableMetadata {

    /**
     * 所有列信息
     */
    private List<ColumnMetadata> columnMetadataList;

    /**
     * 所有索引信息
     */
    private List<IndexMetadata> indexMetadataList;

    public DefaultTableMetadata(Class<?> entityClass, String tableName, String schema, String comment) {
        super(entityClass, tableName, schema, comment);
    }
}
