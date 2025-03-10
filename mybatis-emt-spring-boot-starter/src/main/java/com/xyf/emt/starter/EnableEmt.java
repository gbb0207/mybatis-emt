package com.xyf.emt.starter;

import com.xyf.emt.starter.config.EmtAutoConfig;
import com.xyf.emt.starter.config.EmtImportRegister;
import com.xyf.emt.starter.properties.EmtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Author: 熊韵飞
 * @Description: 启动框架。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableConfigurationProperties(EmtProperties.class)
@Import({EmtAutoConfig.class, EmtImportRegister.class, EmtRunner.class})
public @interface EnableEmt {
    String[] basePackages() default {};
}
