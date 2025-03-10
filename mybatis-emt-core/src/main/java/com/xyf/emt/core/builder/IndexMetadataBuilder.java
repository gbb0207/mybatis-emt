package com.xyf.emt.core.builder;

import com.xyf.emt.common.index.IndexField;
import com.xyf.emt.common.index.JointIndex;
import com.xyf.emt.core.EmtGlobalConfig;
import com.xyf.emt.core.strategy.IndexMetadata;
import com.xyf.emt.core.utils.IndexRepeatChecker;
import com.xyf.emt.core.utils.StringUtils;
import com.xyf.emt.core.utils.TableBeanUtils;
import com.xyf.emt.common.index.Index;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IndexMetadataBuilder {

    public <T extends IndexMetadata> List<T> buildList(Class<?> clazz, List<Field> fields) {

        IndexRepeatChecker indexRepeatChecker = IndexRepeatChecker.of();

        List<IndexMetadata> indexMetadataList = new ArrayList<>(16);

        // 类上的索引注解
        List<IndexMetadata> onClassIndexMetadata = buildFromClass(clazz, indexRepeatChecker);
        indexMetadataList.addAll(onClassIndexMetadata);

        // 字段上的索引注解
        List<IndexMetadata> onFieldIndexMetadata = buildFromField(clazz, fields, indexRepeatChecker);
        indexMetadataList.addAll(onFieldIndexMetadata);

        return (List<T>) indexMetadataList;
    }

    protected List<IndexMetadata> buildFromField(Class<?> clazz, List<Field> fields, IndexRepeatChecker indexRepeatChecker) {
        return fields.stream()
                .filter(field -> TableBeanUtils.isIncludeField(field, clazz))
                .map(field -> buildIndexMetadata(clazz, field))
                .filter(Objects::nonNull)
                .peek(indexMetadata -> indexRepeatChecker.filter(indexMetadata.getName()))
                .collect(Collectors.toList());
    }

    protected List<IndexMetadata> buildFromClass(Class<?> clazz, IndexRepeatChecker indexRepeatChecker) {
        List<JointIndex> jointIndexes = TableBeanUtils.getTableIndexes(clazz);
        return jointIndexes.stream()
                .map(jointIndex -> buildIndexMetadata(clazz, jointIndex))
                .filter(Objects::nonNull)
                .peek(indexMetadata -> indexRepeatChecker.filter(indexMetadata.getName()))
                .collect(Collectors.toList());
    }

    protected IndexMetadata buildIndexMetadata(Class<?> clazz, Field field) {
        // 获取当前字段的@Index注解
        Index index = TableBeanUtils.getIndex(field);
        if (null != index) {
            String realColumnName = TableBeanUtils.getRealColumnName(clazz, field);
            IndexMetadata indexMetadata = newIndexMetadata();
            String indexName = getIndexName(clazz, field, index);
            indexMetadata.setName(indexName);
            indexMetadata.setType(index.type());
            indexMetadata.setComment(index.comment());
            indexMetadata.getColumns().add(IndexMetadata.IndexColumnParam.newInstance(realColumnName, null));
            return indexMetadata;
        }
        return null;
    }

    protected String getIndexName(Class<?> clazz, JointIndex jointIndex) {
        String indexPrefix = EmtGlobalConfig.getEmtProperties().getIndexPrefix();

        // 手动指定了索引名
        String indexName = jointIndex.name();
        if (StringUtils.hasText(indexName)) {
            return indexPrefix + indexName;
        }

        String filedNames = Stream.concat(Arrays.stream(jointIndex.indexFields()).map(IndexField::field), Arrays.stream(jointIndex.fields()))
                .map(fieldName -> TableBeanUtils.getRealColumnName(clazz, fieldName))
                .collect(Collectors.joining("_"));
        String tableName = TableBeanUtils.getTableName(clazz);
        return encryptIndexName(indexPrefix, tableName, filedNames);
    }

    protected String getIndexName(Class<?> clazz, Field field, Index index) {
        String indexPrefix = EmtGlobalConfig.getEmtProperties().getIndexPrefix();

        // 手动指定了索引名
        String indexName = index.name();
        if (StringUtils.hasText(indexName)) {
            return indexPrefix + indexName;
        }

        String realColumnName = getDefaultIndexName(clazz, field);
        String tableName = TableBeanUtils.getTableName(clazz);
        return encryptIndexName(indexPrefix, tableName, realColumnName);
    }

    protected String getDefaultIndexName(Class<?> clazz, Field field) {
        return TableBeanUtils.getRealColumnName(clazz, field);
    }

    protected String encryptIndexName(String prefix, String tableNamePart, String filedNamePart) {
        String indexName = prefix + tableNamePart + "_" + filedNamePart;
        int maxLength = 63;
        if (indexName.length() > maxLength) {
            String md5 = generateMD5(indexName);
            if (prefix.length() + md5.length() > maxLength) {
                throw new RuntimeException("索引名前缀[" + prefix + "]超长，无法生成有效索引名称，请手动指定索引名称");
            }
            // 截取前半部分长度的字符，空余足够的位置，给“_”和MD5值
            String onePart = indexName.substring(0, maxLength - md5.length());
            return onePart + md5;
        }
        return indexName;
    }

    @SneakyThrows
    private String generateMD5(String text) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(text.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    protected IndexMetadata buildIndexMetadata(Class<?> clazz, JointIndex jointIndex) {

        // 获取当前字段的@Index注解
        if (null != jointIndex && (jointIndex.fields().length > 0 || jointIndex.indexFields().length > 0)) {

            List<IndexMetadata.IndexColumnParam> columnParams = getColumnParams(clazz, jointIndex);

            IndexMetadata indexMetadata = newIndexMetadata();
            indexMetadata.setName(getIndexName(clazz, jointIndex));
            indexMetadata.setType(jointIndex.type());
            indexMetadata.setComment(jointIndex.comment());
            indexMetadata.setColumns(columnParams);
            return indexMetadata;
        }
        return null;
    }

    protected IndexMetadata newIndexMetadata() {
        return new IndexMetadata();
    }

    protected List<IndexMetadata.IndexColumnParam> getColumnParams(Class<?> clazz, final JointIndex jointIndex) {
        List<IndexMetadata.IndexColumnParam> columnParams = new ArrayList<>();
        // 防止 两种模式设置的字段有冲突
        Set<String> exitsColumns = new HashSet<>();
        // 优先获取 带排序方式的字段
        IndexField[] sortFields = jointIndex.indexFields();
        if (sortFields.length > 0) {
            columnParams.addAll(
                    Arrays.stream(sortFields)
                            .map(sortField -> {
                                String realColumnName = TableBeanUtils.getRealColumnName(clazz, sortField.field());
                                // 重复字段，自动排除忽略掉
                                if (exitsColumns.contains(realColumnName)) {
                                    return null;
                                }
                                exitsColumns.add(realColumnName);
                                return IndexMetadata.IndexColumnParam.newInstance(realColumnName, sortField.sort());
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            );
        }
        // 其次获取 简单模式的字段，如果重复了，跳过，以带排序方式的为准
        String[] fields = jointIndex.fields();
        if (fields.length > 0) {
            columnParams.addAll(
                    Arrays.stream(fields)
                            .map(field -> {
                                String realColumnName = TableBeanUtils.getRealColumnName(clazz, field);
                                // 重复字段，自动排除忽略掉
                                if (exitsColumns.contains(realColumnName)) {
                                    return null;
                                }
                                exitsColumns.add(realColumnName);
                                return IndexMetadata.IndexColumnParam.newInstance(realColumnName, null);
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            );
        }

        return columnParams;
    }
}
