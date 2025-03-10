package com.xyf.emt.core.utils;

import com.xyf.emt.common.Ignore;
import com.xyf.emt.common.enums.DefaultValueEnum;
import com.xyf.emt.common.field.*;
import com.xyf.emt.common.index.Index;
import com.xyf.emt.common.index.JointIndex;
import com.xyf.emt.common.index.JointIndexes;
import com.xyf.emt.common.index.PrimaryKey;
import com.xyf.emt.common.table.EntityTable;
import com.xyf.emt.core.EmtAnnotationFinder;
import com.xyf.emt.core.EmtGlobalConfig;
import com.xyf.emt.core.EmtOrmFrameAdapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableBeanUtils {

    public static boolean isIncludeField(Field field, Class<?> clazz) {

        Ignore ignore = EmtGlobalConfig.getEmtAnnotationFinder().find(field, Ignore.class);
        if (ignore != null) {
            return false;
        }

        // 调用第三方ORM实现
        boolean isIgnoreField = EmtGlobalConfig.getEmtOrmFrameAdapter().isIgnoreField(field, clazz);
        return !isIgnoreField;
    }

    public static List<JointIndex> getTableIndexes(Class<?> clazz) {
        List<JointIndex> jointIndices = new ArrayList<>();
        JointIndexes jointIndexes = EmtGlobalConfig.getEmtAnnotationFinder().find(clazz, JointIndexes.class);
        if (jointIndexes != null) {
            Collections.addAll(jointIndices, jointIndexes.value());
        }
        JointIndex jointIndex = EmtGlobalConfig.getEmtAnnotationFinder().find(clazz, JointIndex.class);
        if (jointIndex != null) {
            jointIndices.add(jointIndex);
        }
        return jointIndices;
    }

    /**
     * 获取bean上的表名
     *
     * @param clazz bean
     * @return 表名
     */
    public static String getTableName(Class<?> clazz) { // 看过

        String tableName = null;

        EmtAnnotationFinder entityTableAnnotationFinder = EmtGlobalConfig.getEmtAnnotationFinder();

        EntityTable entityTable = entityTableAnnotationFinder.find(clazz, EntityTable.class);
        if (entityTable != null && StringUtils.hasText(entityTable.value())) {
            tableName = entityTable.value();  // @EntityTable 中的 value() 就是表名
        }

        // 如果没用 @EntityTable("表名") 或用了注解但没填值 @EntityTable
        // 调用第三方 ORM 实现
        if (tableName == null) {
            tableName = EmtGlobalConfig.getEmtOrmFrameAdapter().getTableName(clazz);    // 默认空实现
        }

        // 最后的底线，类名驼峰转下划线，clazz 就是每个（实体）类，getSimpleName() 就是取类名。
        if (tableName == null) {
            tableName = StringUtils.camelToUnderline(clazz.getSimpleName());
        }

        return tableName;
    }

    /**
     * 获取bean上的schema
     *
     * @param clazz bean
     * @return schema
     */
    public static String getTableSchema(Class<?> clazz) {

        EmtAnnotationFinder entityTableAnnotationFinder = EmtGlobalConfig.getEmtAnnotationFinder();
        EntityTable entityTable = entityTableAnnotationFinder.find(clazz, EntityTable.class);
        if (entityTable != null) {
            return entityTable.schema();
        }

        // 调用第三方ORM实现
        return EmtGlobalConfig.getEmtOrmFrameAdapter().getTableSchema(clazz);
    }

    public static String getTableComment(Class<?> clazz) {
        EmtAnnotationFinder entityTableAnnotationFinder = EmtGlobalConfig.getEmtAnnotationFinder();

        EntityTable entityTable = entityTableAnnotationFinder.find(clazz, EntityTable.class);
        if (entityTable != null) {
            return entityTable.comment();
        }

        EmtOrmFrameAdapter entityTableOrmFrameAdapter = EmtGlobalConfig.getEmtOrmFrameAdapter();
        String adapterTableComment = entityTableOrmFrameAdapter.getTableComment(clazz);
        if(adapterTableComment != null) {
            return adapterTableComment;
        }

        return null;
    }

    public static boolean isPrimary(Field field, Class<?> clazz) {

        PrimaryKey isPrimary = EmtGlobalConfig.getEmtAnnotationFinder().find(field, PrimaryKey.class);
        if (isPrimary != null) {
            return true;
        }

        // 调用第三方ORM实现
        return EmtGlobalConfig.getEmtOrmFrameAdapter().isPrimary(field, clazz);
    }

    public static boolean isAutoIncrement(Field field, Class<?> clazz) {
        PrimaryKey isPrimary = EmtGlobalConfig.getEmtAnnotationFinder().find(field, PrimaryKey.class);
        if (isPrimary != null) {
            return isPrimary.value();
        }
        EmtOrmFrameAdapter entityTableOrmFrameAdapter = EmtGlobalConfig.getEmtOrmFrameAdapter();
        return entityTableOrmFrameAdapter.isAutoIncrement(field, clazz);
    }

    public static Boolean isNotNull(Field field, Class<?> clazz) {
        // 主键默认为非空
        if (isPrimary(field, clazz)) {
            return true;
        }

        ColumnNotNull column = EmtGlobalConfig.getEmtAnnotationFinder().find(field, ColumnNotNull.class);
        if (column != null) {
            return column.value();
        }
        Column autoColumn = EmtGlobalConfig.getEmtAnnotationFinder().find(field, Column.class);
        if (autoColumn != null) {
            return autoColumn.notNull();
        }
        return false;
    }

    public static String getComment(Field field, Class<?> clazz) {
        ColumnComment column = EmtGlobalConfig.getEmtAnnotationFinder().find(field, ColumnComment.class);
        if (column != null) {
            return column.value();
        }
        Column autoColumn = EmtGlobalConfig.getEmtAnnotationFinder().find(field, Column.class);
        if (autoColumn != null) {
            return autoColumn.comment();
        }

        EmtOrmFrameAdapter entityTableOrmFrameAdapter = EmtGlobalConfig.getEmtOrmFrameAdapter();
        String adapterColumnComment = entityTableOrmFrameAdapter.getColumnComment(field, clazz);
        if(adapterColumnComment != null) {
            return adapterColumnComment;
        }

        return "";
    }

    public static ColumnDefault getDefaultValue(Field field) {
        ColumnDefault columnDefault = EmtGlobalConfig.getEmtAnnotationFinder().find(field, ColumnDefault.class);
        if (columnDefault != null) {
            return columnDefault;
        }
        Column autoColumn = EmtGlobalConfig.getEmtAnnotationFinder().find(field, Column.class);
        if (autoColumn != null) {
            return new ColumnDefault() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return ColumnDefault.class;
                }

                @Override
                public DefaultValueEnum type() {
                    return autoColumn.defaultValueType();
                }

                @Override
                public String value() {
                    return autoColumn.defaultValue();
                }
            };
        }
        return null;
    }

    public static ColumnType getColumnType(Field field) {
        ColumnType columnType = EmtGlobalConfig.getEmtAnnotationFinder().find(field, ColumnType.class);
        if (columnType != null) {
            return columnType;
        }

        Column autoColumn = EmtGlobalConfig.getEmtAnnotationFinder().find(field, Column.class);
        if (autoColumn != null) {
            return new ColumnType() {
                @Override
                public String value() {
                    return autoColumn.type();
                }

                @Override
                public int length() {
                    return autoColumn.length();
                }

                @Override
                public int decimalLength() {
                    return autoColumn.scale();
                }

                @Override
                public String[] values() {
                    return new String[0];
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return ColumnType.class;
                }
            };
        }
        return null;
    }

    public static Index getIndex(Field field) {
        return EmtGlobalConfig.getEmtAnnotationFinder().find(field, Index.class);
    }

    public static Class<?> getFieldType(Class<?> clazz, Field field) {

        // 自定义获取字段的类型
        Class<?> fieldType = EmtGlobalConfig.getEmtOrmFrameAdapter().customFieldTypeHandler(clazz, field);

        if (fieldType == null) {
            fieldType = field.getType();
        }

        return fieldType;
    }

    /**
     * 根据注解顺序和配置，获取字段对应的数据库字段名
     *
     * @param clazz bean
     * @param field 字段
     * @return 字段名
     */
    public static String getRealColumnName(Class<?> clazz, Field field) {   // 如果标了框架内提供的列注解，就按注解里的值来为列命名，否则就属性名转蛇形

        ColumnName columnNameAnno = EmtGlobalConfig.getEmtAnnotationFinder().find(field, ColumnName.class);
        if (columnNameAnno != null) {
            String columnName = columnNameAnno.value();
            if (StringUtils.hasText(columnName)) {
                return columnName;
            }
        }
        Column autoColumn = EmtGlobalConfig.getEmtAnnotationFinder().find(field, Column.class);
        if (autoColumn != null) {
            String columnName = autoColumn.value();
            if (StringUtils.hasText(columnName)) {
                return columnName;
            }
        }

        String realColumnName = EmtGlobalConfig.getEmtOrmFrameAdapter().getRealColumnName(clazz, field);
        if (StringUtils.hasText(realColumnName)) {
            return realColumnName;
        }

        return StringUtils.camelToUnderline(field.getName());
    }

    /**
     * 根据注解顺序和配置，获取字段对应的数据库字段名
     *
     * @param beanClazz bean class
     * @param fieldName 字段名
     * @return 字段名
     */
    public static String getRealColumnName(Class<?> beanClazz, String fieldName) {

        Field field = BeanClassUtil.getField(beanClazz, fieldName);
        return getRealColumnName(beanClazz, field);
    }
}
