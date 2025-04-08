package com.xyf.mybatis.emt.test.interceptor;

import com.xyf.emt.core.interceptor.CreateTableInterceptor;
import com.xyf.emt.core.strategy.TableMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author: 熊韵飞
 * @Description:
 */

@Component
@Slf4j
public class TestCreateTableInterceptor implements CreateTableInterceptor {
    @Override
    public void beforeCreateTable(String databaseDialect, TableMetadata tableMetadata) {
        log.info("建表前拦截，获取实体元数据：" + tableMetadata);
    }
}
