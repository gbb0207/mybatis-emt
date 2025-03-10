package com.xyf.emt.core.recordsql;

import com.xyf.emt.core.EmtGlobalConfig;
import com.xyf.emt.core.config.PropertyConfig;
import com.xyf.emt.core.dynamicds.DatasourceNameManager;
import com.xyf.emt.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class RecordSqlFileHandler implements RecordSqlHandler {
    @Override
    public void record(EmtExecuteSqlLog emtExecuteSqlLog) {

        Path path = getFilePath(emtExecuteSqlLog);
        if (path != null && !Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            } catch (IOException e) {
                log.error("创建日志文件{}出错", path, e);
                path = null;
            }
        }

        if (path != null) {
            try {
                String sqlStatement = emtExecuteSqlLog.getSqlStatement();
                // 末尾添加换行符
                if (!sqlStatement.endsWith(System.lineSeparator())) {
                    sqlStatement = sqlStatement + System.lineSeparator();
                }
                Files.write(path, sqlStatement.getBytes(StandardCharsets.UTF_8), java.nio.file.StandardOpenOption.APPEND);
            } catch (IOException e) {
                log.error("向{}写入SQL日志出错", path, e);
            }
        }
    }

    /**
     * 希望自定义文件全路径的话，可以重写此方法
     */
    protected Path getFilePath(EmtExecuteSqlLog emtExecuteSqlLog) {

        PropertyConfig.RecordSqlProperties recordSql = EmtGlobalConfig.getEmtProperties().getRecordSql();

        String folderPath = recordSql.getFolderPath();

        if (StringUtils.noText(folderPath)) {
            log.error("没有指定SQL日志文件目录，无法记录SQL执行记录");
            return null;
        }

        String fileName = getFileName(emtExecuteSqlLog);

        return Paths.get(folderPath, fileName);
    }

    /**
     * 希望自定义文件名称的话，可以重写此方法
     */
    private String getFileName(EmtExecuteSqlLog emtExecuteSqlLog) {

        StringBuilder fileName = new StringBuilder();
        // 添加版本号
        if (StringUtils.hasText(emtExecuteSqlLog.getVersion())) {
            fileName.append(emtExecuteSqlLog.getVersion()).append("_");
        }
        // 添加数据源名称
        String dataSourceName = DatasourceNameManager.getDatasourceName();
        if (StringUtils.hasText(dataSourceName)) {
            fileName.append(dataSourceName).append("_");
        }
        // 添加tableSchema
        String tableSchema = emtExecuteSqlLog.getTableSchema();
        if (StringUtils.hasText(tableSchema)) {
            fileName.append(tableSchema).append("_");
        }
        // 添加表名
        fileName.append(emtExecuteSqlLog.getTableName());

        return fileName.append(".sql").toString();
    }

}
