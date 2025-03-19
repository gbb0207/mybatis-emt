package com.xyf.emt.core;

import com.xyf.emt.common.Ignore;
import com.xyf.emt.common.table.EntityTable;
import com.xyf.emt.core.config.PropertyConfig;
import com.xyf.emt.core.dynamicds.IDataSourceHandler;
import com.xyf.emt.core.strategy.IStrategy;
import com.xyf.emt.core.strategy.mysql.MysqlStrategy;
import com.xyf.emt.core.utils.ClassScanner;
import com.xyf.emt.core.utils.TableBeanUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 启动时进行处理的实现类
 */
@Slf4j
public class EmtBootstrap {

    public static void start() {

        PropertyConfig emtProperties = EmtGlobalConfig.getEmtProperties();

        // 判断模式，none或者禁用，不启动
        if (emtProperties.getMode() == RunMode.none || !emtProperties.getEnable()) {
            return;
        }

        if (emtProperties.getShowBanner()) {
            Banner.print();
        }

        final long start = System.currentTimeMillis();

        // 注册内置的不同数据源策略，后面 IDataSourceHandler 实现类的 handleAnalysis() 会按 databaseDialect 来取数据库策略执行 sql。
        EmtGlobalConfig.addStrategy(new MysqlStrategy()); // 这里每个实现 IStrategy 的都会实现 databaseDialect，返回数据库方言字符串，此方法的 key 就是这个方言

        // 获取扫描包路径，如果 @EnableXxx 中有值（即开发者填入的实体包名）则用其中的属性值，如果只用了 Enable 注解没有填写值，那么就返回启动类的包（等会从启动类的包开始递归寻
        // 找包含指定注解的实体类！）
        String[] packs = getModelPackage(emtProperties);

        // 准备从包 package 中获取所有包含指定注解的类
        Set<Class<? extends Annotation>> includeAnnotations = new HashSet<>(
                Collections.singletonList(EntityTable.class)  // 注意这里哦~只包含一个 @EntityTable 类型。等会会从启动类包一层一层递归寻找打了这个注解的实体类
        );
        // 添加开发者自定义的注解，这里默认是空的。开发者实现需要自己创建一个 list 作为注解集合并返回。
        includeAnnotations.addAll(EmtGlobalConfig.getEmtOrmFrameAdapter().scannerAnnotations());

        // 排除的注解，优先级大于所有注解！实体类上、字段上都能用。@Ignore 是大于 @EntityTable 的。
        Set<Class<? extends Annotation>> ignoreAnnotations = new HashSet<>(Collections.singleton(Ignore.class));

        // 通过开发者自定义的注解拦截器，修改最终影响自动建表的注解。这里给此方法传入【包含的注解，排出的注解】，可以对传入的这两个集合添加删除其中的注解。
        EmtGlobalConfig.getEmtAnnotationInterceptor().intercept(includeAnnotations, ignoreAnnotations);

        // 扫描指定包下所有的类，过滤出指定注解的实体。其中 ? 就是扫描出的打了 @EntityTable 注解的实体类！
        Set<Class<?>> classes = ClassScanner.scan(packs, includeAnnotations, ignoreAnnotations);

        // 获取对应的数据源，根据不同数据库方言，执行不同的处理
        IDataSourceHandler datasourceHandler = EmtGlobalConfig.getDatasourceHandler();
        datasourceHandler.handleAnalysis(classes, (databaseDialect, entityClasses) -> { // (方言字符串"MySQL", Set<实体类>)

            // 同一个数据源下，先检查重名的表~很严谨这里，有重名的表直接抛异常，不管就是了。
            Map<String, List<Class<?>>> repeatCheckMap = entityClasses.stream() // 下面两个工具类里的方法返回值取决于实体类有没有对应注解
                    .collect(Collectors.groupingBy(entity -> TableBeanUtils.getTableSchema(entity) + "." + TableBeanUtils.getTableName(entity)));

            for (Map.Entry<String, List<Class<?>>> repeatCheckItem : repeatCheckMap.entrySet()) {
                int sameTableNameCount = repeatCheckItem.getValue().size();
                if (sameTableNameCount > 1) {
                    String tableName = repeatCheckItem.getKey();
                    throw new RuntimeException(String.format("存在重名的表：%s(%s)，请检查！", tableName,
                            String.join(",", repeatCheckItem.getValue().stream().map(Class::getName).collect(Collectors.toSet()))));
                }
            }   // repeatCheckMap 只用于检查重名表，下面也用不到这个变量了

            // 查找对应方言的数据源策略，对实体类执行建表操作
            IStrategy<?, ?, ?> databaseStrategy = EmtGlobalConfig.getStrategy(databaseDialect);
            if (databaseStrategy != null) {
                for (Class<?> entityClass : entityClasses) {
                    databaseStrategy.start(entityClass);    // 最重要！东西超级多。这是个 default 方法，所以和方言无关。
                }
            } else {
                log.warn("没有找到对应的数据库（{}）方言策略，无法自动维护表结构", databaseDialect);
            }
        });
        log.info("Mybatis-Emt执行结束。耗时：{}ms", System.currentTimeMillis() - start);
    }

    private static String[] getModelPackage(PropertyConfig emtProperties) {
        String[] packs = emtProperties.getModelPackage(); // 通过 EmtImportRegister 获取的 Enable 注解的属性值，即
        if (packs == null || packs.length == 0) {   // 只有 @EnableXxx 而不是 @EnableXxx(basePackage = "xxx")
            packs = new String[]{getBootPackage()}; // 那就自动扫描，找到离此方法最近的第一个 main 方法的包名返回，例如：com.xyf.XxxApplication 的 com.xyf
        }
        return packs;
    }

    private static String getBootPackage() {
        StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if ("main".equals(stackTraceElement.getMethodName())) {
                String mainClassName = stackTraceElement.getClassName();
                int lastDotIndex = mainClassName.lastIndexOf(".");
                return (lastDotIndex != -1 ? mainClassName.substring(0, lastDotIndex) : "");    // 全限定名中只取包名，不包含类名
            }
//            if ("main".equals(stackTraceElement.getMethodName())) {
//                return stackTraceElement.getClassName();  // 这就是取全限定名
//            }
        }
        throw new RuntimeException("未找到主默认包");
    }
}
