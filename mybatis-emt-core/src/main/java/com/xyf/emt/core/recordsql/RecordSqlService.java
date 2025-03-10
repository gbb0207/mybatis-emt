package com.xyf.emt.core.recordsql;

import com.xyf.emt.core.EmtGlobalConfig;
import com.xyf.emt.core.config.PropertyConfig;
import com.xyf.emt.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RecordSqlService {

    public static void record(List<EmtExecuteSqlLog> emtExecuteSqlLogs) {   // 这里传入的是一条具体需要插入到备份表/文件的记录，EmtExecuteSqlLog 的实现类

        PropertyConfig.RecordSqlProperties recordSql = EmtGlobalConfig.getEmtProperties().getRecordSql();

        if (!recordSql.isEnable()) {    // 默认是不开启的！！！！！！！！！！！！！！！
            return;
        }

        RecordSqlHandler recordSqlHandler;
        PropertyConfig.RecordSqlProperties.TypeEnum recordType = recordSql.getRecordType();
        switch (recordType) {
            case db:    // 默认
                recordSqlHandler = new RecordSqlDbHandler();
                break;
            case file:
                recordSqlHandler = new RecordSqlFileHandler();
                break;
            case custom:
            default:
                recordSqlHandler = EmtGlobalConfig.getCustomRecordSqlHandler();
                break;
        }

        String version = recordSql.getVersion();

        if (StringUtils.noText(version)) {
            log.warn("Emt的SQL记录功能没有配置版本号，默认为空，强烈建议关联即将上线的版本号，根据版本管理SQL日志，避免混乱");
        }

        for (EmtExecuteSqlLog emtExecuteSqlLog : emtExecuteSqlLogs) {
            // 设置手动指定的版本
            emtExecuteSqlLog.setVersion(version); // 可以为 null，非必须
            // 调用不同的记录器
            recordSqlHandler.record(emtExecuteSqlLog);    // 在这里创建备份表/文件，并将具体创建表时的信息存入其中
        }
    }
}
