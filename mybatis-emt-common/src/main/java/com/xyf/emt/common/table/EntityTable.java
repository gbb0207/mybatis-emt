package com.xyf.emt.common.table;

import java.lang.annotation.*;

/**
 * @Author: 熊韵飞
 * @Description: 实体映射表核心注解。以此注解作为解析被标注实体类元数据的依据。若某数据库有独特的表级属性，抽取到语言包下，由不同策略执行。例如：mysql的引擎、字符集排序规则；Oracle的catalog等。
 */

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EntityTable {

    /**
     *
     * @return 表名。如果为空字符串会在注解解析器中转化为类名的蛇形命名
     */
    String value() default "";

    /**
     * 这个属性为保留属性，在不同数据库可能有不同作用，sql通用。
     * @return 指定数据库。如果为空字符串会根据配置文件中数据库url的schema作为默认值。
     */
    String schema() default "";

    /**
     *
     * @return 表注释。为空默认无注释。
     */
    String comment() default "";

    /**
     * 目前先留白，因为自增在ddl中可以作为表级属性。不如直接实现列级的注解，表级还要多find一次列元素。优先级一定也是列级更高。
     * @return 自增。如果为空字符串默认去搜索实体属性上是否有自增注解，且必须满足数字类型。如果全部没有则为没有自增字段。
     */
    String increase() default "";

}


