package com.cxy7.data.fense.server.jdbc;

import com.cxy7.data.fense.jdbc.Engine;
import com.cxy7.data.fense.jdbc.FenseConnection;
import com.cxy7.data.fense.server.model.QueryTask;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hive.jdbc.HiveConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/5 19:22
 */
@Component
@Slf4j
public class JdbcExecutor implements Closeable {
    @Autowired
    private ThreadPoolHolder threadPoolHolder;

    @Value("${jdbc.execute.timeout}")
    private long executeTimeout;

    @SneakyThrows
    public long execute(QueryTask task) {
        ExecutorService executorService = threadPoolHolder.getThreadPool(task.getEngine());
        Future<Long> future = executorService.submit(() -> execute0(task));
        return future.get(executeTimeout, TimeUnit.MINUTES);
    }

    @SneakyThrows
    private long execute0(QueryTask task) {
        Statement statement = task.getStatement();
        long begin = System.currentTimeMillis();
        boolean ret = statement.execute(task.getSql());
        return System.currentTimeMillis() - begin;
    }

    public Statement createStatement(FenseConnection fenseConnection, String schema, int fetchSize) throws SQLException {
        Engine engine = fenseConnection.getEngine();
        switch (engine) {
            case HIVE:
            case CLICKHOUSE:
                fenseConnection.setSchema(schema);
                break;
        }
        fenseConnection.setCatalog(String.valueOf(fenseConnection.getDsId()));

        Connection connection = fenseConnection.getDelegate();
        if (engine == Engine.HIVE) {
            HiveConnection hiveConnection = connection.unwrap(HiveConnection.class);
            return hiveConnection.createStatement();
        }

        Statement statement = connection.createStatement();
        statement.setFetchSize(fetchSize);
        return statement;
    }

    @Override
    public void close() {
        threadPoolHolder.close();
    }
}
