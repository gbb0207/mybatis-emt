package com.xyf.emt.starter.config;

import com.xyf.emt.starter.EnableEmt;
import com.xyf.emt.starter.EnableEmtTest;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * 获取注解的basePackages，在spring执行时就会执行这里
 */
public class EmtImportRegister implements ImportBeanDefinitionRegistrar {

    /**
     * 提取注解的basePackages
     */
    public static volatile String[] basePackagesFromAnno;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 当取到basePackages，则不再继续执行，发生的场景是单元测试和启动类都指定了basePackages，优先以单元测试的为准
        if (basePackagesFromAnno != null) {
            return;
        }

        // 获取
        Map<String, Object> emtAttributes = getEmtAttributes(importingClassMetadata);
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(emtAttributes);
        if (annotationAttributes != null) {
            String[] basePackages = Arrays.stream(annotationAttributes.getStringArray("basePackages"))
                    .filter(StringUtils::hasText)
                    .distinct()
                    .toArray(String[]::new);
            if (basePackages.length > 0) {
                basePackagesFromAnno = basePackages;
            }
        }
    }

    /**
     * 分别尝试获取两个注解的值
     */
    private Map<String, Object> getEmtAttributes(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> emtAttributes = importingClassMetadata.getAnnotationAttributes(EnableEmtTest.class.getName());
        if (emtAttributes == null) {
            importingClassMetadata.getAnnotationAttributes(EnableEmt.class.getName());
        }
        return emtAttributes;
    }
}
