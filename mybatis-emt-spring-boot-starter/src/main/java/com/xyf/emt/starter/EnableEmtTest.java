package com.xyf.emt.starter;

import org.springframework.test.context.TestExecutionListeners;

import java.lang.annotation.*;

/**
 * 在 spring boot 单元测试环境下仍然希望自动建表，那么就需要使用到这个注解。如果引入 springboot 测试包后，需要更改其 scope 为 test
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@TestExecutionListeners(
        listeners = {EmtTestExecutionListener.class},
        // 将自定义监听器与 Spring 默认的监听器合并，保证依赖注入等功能不受影响。默认监听器会按顺序自动加载加了 @AutoConfigureAfter 的类，所以这里也会。
        // 效果就和主动 @Import 一样了
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public @interface EnableEmtTest {
    String[] basePackages() default {};
}
