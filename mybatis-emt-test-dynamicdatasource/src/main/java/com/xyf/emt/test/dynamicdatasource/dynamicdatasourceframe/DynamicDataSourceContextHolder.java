package com.xyf.emt.test.dynamicdatasource.dynamicdatasourceframe;

import java.util.Stack;

/**
 * 数据源上下文管理
 */
public class DynamicDataSourceContextHolder {

    /**
     * 动态数据源名称上下文
     */
    private static final ThreadLocal<Stack<String>> DATASOURCE_CONTEXT_KEY_HOLDER = ThreadLocal.withInitial(Stack::new);

    public static void setContextKey(String dataSourceName) {
        DATASOURCE_CONTEXT_KEY_HOLDER.get().push(dataSourceName);
    }

    public static void removeContextKey() {
        Stack<String> stack = DATASOURCE_CONTEXT_KEY_HOLDER.get();
        if (!stack.empty()) {
            stack.pop();
        }
    }

    /**
     * 用于获取当前线程的顶部数据源名称。如果当前线程没有设置数据源，则返回默认的主数据源名称。
     * @return
     */
    public static String getContextKey() {  // ** 最重要
        // 1.如果实体类的 @Ds 中没有值，那么就直接走最下面的 return 逻辑，返回默认值，DynamicDataSourceHandler 会在插入之前调用此方法，然
        //   后为均没主动标识数据源的若干实体做默认分组，让一个数据源下的实体类使用同一个默认主数据源连接
        // *************************************************************************************************************
        // 2.DynamicDataSource 会自动调用这个方法，通过数据源标识更改 DynamicDataSourceConfig 提前注册好的指定名称的数据源 bean
        Stack<String> stack = DATASOURCE_CONTEXT_KEY_HOLDER.get();  //
        if (!stack.empty()) {
            // 取栈顶元素，即最后压栈的元素
            return stack.peek();
        }
        // 如果没有配置 @Ds("xxx") 选取指定前缀的数据源，那么直接返回指定的默认数据源标识（即 master）
        return DataSourceConstants.DS_KEY_MASTER;
    }
}
