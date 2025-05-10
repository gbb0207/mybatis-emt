package com.xyf.emt.test.dynamicdatasource.dynamicdatasourceframe;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ds {

    /**
     * 数据源名称
     * @return
     */
    String value();
}
