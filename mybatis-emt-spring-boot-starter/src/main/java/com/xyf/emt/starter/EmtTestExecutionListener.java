package com.xyf.emt.starter;

import com.xyf.emt.core.EmtBootstrap;
import com.xyf.emt.core.config.PropertyConfig;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import com.xyf.emt.core.EmtGlobalConfig;

import javax.swing.*;

public class EmtTestExecutionListener implements TestExecutionListener {    // 在测试类执行前，初始化所需的所有组件，这个和 EmtRunner 一样的其实

    // 所有测试类执行之前执行
    @Override
    public void beforeTestClass(TestContext testContext) {
        initBasePackages(testContext);

        // 单元测试模式下，启动
        EmtBootstrap.start();
    }

    private void initBasePackages(TestContext testContext) {    // EmtImportRegister 做的事
        // 初始化应用上下文，会阻塞等待上下文加载完。
        // 由于 mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS，所以会加载 @AutoConfigureAfter 的类
        ApplicationContext applicationContext = testContext.getApplicationContext();

        PropertyConfig emtProperties = EmtGlobalConfig.getEmtProperties();  // PropertyConfig 的属性是有默认值的，这是个类静态成员，在 JVM 加载类时就创建了

        // 用于扫描加了 @EntityTable 的实体类
        String[] modelPackage = emtProperties.getModelPackage();
        if (modelPackage == null || modelPackage.length == 0) { // 如果配置类没写，就找启动类或配置类
            // 获取启动类（带有@SpringBootApplication注解的类）
            Object mainClass = applicationContext.getBeansWithAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class)   // 所有标注了指定注解的 Bean
                    .values()
                    .stream()
                    .findFirst()
                    // 尝试查找标注 @SpringBootConfiguration 的 Bean，@SpringBootApplication 包含其
                    .orElseGet(() -> applicationContext.getBeansWithAnnotation(org.springframework.boot.SpringBootConfiguration.class)
                            .values()
                            .stream()
                            .findFirst()    // 和前面一样
                            .orElseThrow(() -> new IllegalStateException("启动类未找到"))
                    );

            // 获取启动类的包名
            String packageName = mainClass.getClass().getPackage().getName();
            emtProperties.setModelPackage(new String[]{packageName});
        }
    }
}
