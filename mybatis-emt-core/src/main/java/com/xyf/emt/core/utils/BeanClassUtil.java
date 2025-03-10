package com.xyf.emt.core.utils;

import com.xyf.emt.core.EmtGlobalConfig;
import com.xyf.emt.core.config.PropertyConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BeanClassUtil {

    /**
     * 查找类下指定的字段，如果当前类没有，那就去它的父类寻找
     *
     * @param clazz     类
     * @param fieldName 字段名
     * @return 字段
     */
    public static Field getField(Class<?> clazz, String fieldName) {

        Field field;
        while (true) {
            field = Arrays.stream(clazz.getDeclaredFields())
                    .filter(f -> f.getName().equals(fieldName))
                    .findFirst().orElse(null);
            // 如果没有找到
            if (field == null) {
                Class<?> superclass = clazz.getSuperclass();
                // 如果存在父类，且不是Object，则去上一级的父类继续寻找
                if (superclass != null && superclass != Object.class) {
                    clazz = superclass;
                    continue;
                }
            }
            break;
        }

        if (field == null) {
            throw new RuntimeException(String.format("%s上没有找到字段：%s（友情提示：请配置java字段名，而不是数据库列名）", clazz.getName(), fieldName));
        }

        return field;
    }

    /**
     * 查询某个类下所有的列的字段
     *
     * @param beanClass 类class
     * @return 所有列的字段
     */
    public static List<Field> listAllFieldForColumn(Class<?> beanClass) {

        // 获取父类追加到子类位置的配置
        PropertyConfig emtProperties = EmtGlobalConfig.getEmtProperties();
        PropertyConfig.SuperInsertPosition superInsertPosition = emtProperties.getSuperInsertPosition();

        List<Field> fieldList = new ArrayList<>();

        getColumnFieldList(fieldList, beanClass, false, superInsertPosition == PropertyConfig.SuperInsertPosition.after, emtProperties.getStrictExtends());
        return fieldList;
    }

    /**
     * 获取某个类下所有的字段
     *
     * @param fields           预先声明的集合
     * @param beanClass        指定类
     * @param parentInsertBack 是否追加到集合后面
     */
    private static void getColumnFieldList(List<Field> fields, Class<?> beanClass, boolean isParent, boolean parentInsertBack, boolean strictExtends) {

        // 这些字段包含了类自身定义的成员变量，不包括从父类继承来的字段。
        Field[] declaredFields = beanClass.getDeclaredFields();

        // 获取当前实体类的所有 fields 的 name 列表，注意这里的 fields 是形参！！！！！！！！本来传入的就是空集合。这里第一遍什么也没有。
        // 但是最后会递归~~~
        Set<String> fieldNames = fields.stream().map(Field::getName).collect(Collectors.toSet());

        List<Field> newFields = Arrays.stream(declaredFields)
                // 避免重载属性
                .filter(field -> !fieldNames.contains(field.getName()))
                // 忽略静态变量，因为静态字段不属于对象实例的属性。
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                // 忽略final字段，出于某些业务需求，不想处理不可变的字段。
                .filter(field -> !Modifier.isFinal(field.getModifiers()))
                // 父类字段，必须声明字段为protected或者public
                .filter(field -> !isParent || !strictExtends || Modifier.isProtected(field.getModifiers()) || Modifier.isPublic(field.getModifiers()))
                .collect(Collectors.toList());

        if (parentInsertBack) {
            fields.addAll(newFields);
        } else {
            fields.addAll(0, newFields);
        }

        Class<?> superclass = beanClass.getSuperclass();
        if (superclass != null) {
            getColumnFieldList(fields, superclass, true, parentInsertBack, strictExtends);
        }
    }
}
