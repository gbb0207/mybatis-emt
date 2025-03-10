package com.xyf.emt.core.dynamicds;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatasourceNameManager {

    /**
     * 当前数据源
     */
    private static final ThreadLocal<String> DATASOURCE_NAME = new ThreadLocal<>();

    public static void setDatasourceName(@NonNull String datasourceName) {
        DatasourceNameManager.DATASOURCE_NAME.set(datasourceName);
    }

    public static String getDatasourceName() {
        String datasourceName = DatasourceNameManager.DATASOURCE_NAME.get();
        if (datasourceName == null) {
            log.error("当前数据源下，未找到对应的DatasourceName");
        }
        return datasourceName;
    }

    public static void cleanDatasourceName() {
        DATASOURCE_NAME.remove();
    }
}
