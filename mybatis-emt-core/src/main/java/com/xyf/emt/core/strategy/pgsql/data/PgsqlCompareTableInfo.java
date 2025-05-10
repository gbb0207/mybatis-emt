package com.xyf.emt.core.strategy.pgsql.data;

import com.xyf.emt.core.strategy.ColumnMetadata;
import com.xyf.emt.core.strategy.CompareTableInfo;
import com.xyf.emt.core.strategy.IndexMetadata;
import com.xyf.emt.core.utils.StringUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class PgsqlCompareTableInfo extends CompareTableInfo {

    /**
     * 注释: 有值说明需要改
     */
    private String comment;

    /**
     * 新的主键
     */
    private List<ColumnMetadata> newPrimaries = new ArrayList<>();
    /**
     * 不为空删除主键
     */
    private String dropPrimaryKeyName;

    /**
     * 注释: 需要添加/修改的字段注释《列名，注释内容》
     */
    private Map<String, String> columnComment = new HashMap<>();

    /**
     * 注释: 需要添加/修改的索引注释《索引名，注释内容》
     */
    private Map<String, String> indexComment = new HashMap<>();

    /**
     * 需要删除的列
     */
    private List<String> dropColumnList = new ArrayList<>();

    /**
     * 需要修改的列
     */
    private List<ColumnMetadata> modifyColumnMetadataList = new ArrayList<>();

    /**
     * 需要新增的列
     */
    private List<ColumnMetadata> newColumnMetadataList = new ArrayList<>();

    /**
     * 需要删除的索引
     */
    private List<String> dropIndexList = new ArrayList<>();

    /**
     * 新添加的索引
     */
    private List<IndexMetadata> indexMetadataList = new ArrayList<>();

    public PgsqlCompareTableInfo(@NonNull String name, @NonNull String schema) {
        super(name, schema);
    }

    @Override
    public boolean needModify() {
        return StringUtils.hasText(comment) ||
                StringUtils.hasText(dropPrimaryKeyName) ||
                !newPrimaries.isEmpty() ||
                !columnComment.isEmpty() ||
                !indexComment.isEmpty() ||
                !dropColumnList.isEmpty() ||
                !modifyColumnMetadataList.isEmpty() ||
                !newColumnMetadataList.isEmpty() ||
                !dropIndexList.isEmpty() ||
                !indexMetadataList.isEmpty();
    }

    @Override
    public String validateFailedMessage() {
        StringBuilder errorMsg = new StringBuilder();
        if (StringUtils.hasText(comment)) {
            errorMsg.append("表注释变更: ").append(comment).append("\n");
        }
        if (StringUtils.hasText(dropPrimaryKeyName)) {
            errorMsg.append("删除主键: ").append(dropPrimaryKeyName).append("\n");
        }
        if (!newPrimaries.isEmpty()) {
            errorMsg.append("新增主键: ").append(newPrimaries).append("\n");
        }
        if (!columnComment.isEmpty()) {
            errorMsg.append("列注释变更: ").append(columnComment.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(", "))).append("\n");
        }
        if (!indexComment.isEmpty()) {
            errorMsg.append("索引注释变更: ").append(indexComment.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(", "))).append("\n");
        }
        if (!dropColumnList.isEmpty()) {
            errorMsg.append("删除列: ").append(String.join(",", dropColumnList)).append("\n");
        }
        if (!modifyColumnMetadataList.isEmpty()) {
            errorMsg.append("修改列: ").append(modifyColumnMetadataList.stream().map(ColumnMetadata::getName).collect(Collectors.joining(","))).append("\n");
        }
        if (!newColumnMetadataList.isEmpty()) {
            errorMsg.append("新增列: ").append(newColumnMetadataList.stream().map(ColumnMetadata::getName).collect(Collectors.joining(","))).append("\n");
        }
        if (!dropIndexList.isEmpty()) {
            errorMsg.append("删除索引: ").append(String.join(",", dropIndexList)).append("\n");
        }
        if (!indexMetadataList.isEmpty()) {
            errorMsg.append("新增索引: ").append(indexMetadataList.stream().map(IndexMetadata::getName).collect(Collectors.joining(","))).append("\n");
        }
        return errorMsg.toString();
    }

    public void addColumnComment(String columnName, String newComment) {
        this.columnComment.put(columnName, newComment);
    }

    public void addNewColumn(ColumnMetadata columnMetadata) {
        this.newColumnMetadataList.add(columnMetadata);
    }

    public void addModifyColumn(ColumnMetadata columnMetadata) {
        this.modifyColumnMetadataList.add(columnMetadata);
    }

    public void addDropColumns(Set<String> dropColumnList) {
        this.dropColumnList.addAll(dropColumnList);
    }

    public void addNewIndex(IndexMetadata indexMetadata) {
        this.indexMetadataList.add(indexMetadata);
    }

    public void addModifyIndex(IndexMetadata indexMetadata) {
        this.dropIndexList.add(indexMetadata.getName());
        this.indexMetadataList.add(indexMetadata);
    }

    public void addIndexComment(@NonNull String indexName, @NonNull String newComment) {
        this.indexComment.put(indexName, newComment);
    }

    public void addDropIndexes(Set<String> indexNameList) {
        this.dropIndexList.addAll(indexNameList);
    }

    public void addNewPrimary(List<ColumnMetadata> columnMetadata) {
        this.newPrimaries.addAll(columnMetadata);
    }
}
