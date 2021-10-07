package com.cxy7.data.fense.server.jdbc;

import com.cxy7.data.fense.jdbc.Engine;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.sql.SQLException;
import java.util.concurrent.*;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/5 19:24
 */
@Component
@Slf4j
public class ThreadPoolHolder implements Closeable {
    @Autowired
    private MeterRegistry meterRegistry;

    @Value("${executor.shutdown.timeout.default}")
    private long defaultShutdownTimeout;

    @Value("${executor.shutdown.timeout.hive}")
    private long hiveShutdownTimeout;

    private final CountDownLatch latch = new CountDownLatch(5);

    @Value("${executor.corePoolSize.default}")
    private int defaultCorePoolSize;

    @Value("${executor.maxPoolSize.default}")
    private int defaultMaxPoolSize;

    @Value("${executor.queueCapacity.default}")
    private int defaultQueueCapacity;

    private ExecutorService tidbThreadPool;
    private ExecutorService clickhouseThreadPool;
    private ExecutorService dorisThreadPool;
    private ExecutorService mysqlThreadPool;
    private ExecutorService hiveThreadPool;

    @PostConstruct
    private void init() {
        tidbThreadPool = initThreadPool("TiDB-Task");
        clickhouseThreadPool = initThreadPool("CH-Task");
        dorisThreadPool = initThreadPool("Doris-Task");
        mysqlThreadPool = initThreadPool("MySQL-Task");
        hiveThreadPool = initThreadPool("Hive-Task");
    }

    private ExecutorService initThreadPool(String taskName) {
        ExecutorService executor = initThreadPool(taskName + '-', defaultCorePoolSize, defaultMaxPoolSize, defaultQueueCapacity);
        return ExecutorServiceMetrics.monitor(meterRegistry, executor, taskName);
    }

    private ExecutorService initThreadPool(String prefixName, int corePoolSize, int maxPoolSize, int queueCapacity) {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setQueueCapacity(queueCapacity);
        taskExecutor.setKeepAliveSeconds(60);
        taskExecutor.setThreadNamePrefix(prefixName);
        taskExecutor.setRejectedExecutionHandler(new FenseThreadPoolRejectedExecutionHandler());
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.initialize();
        return taskExecutor.getThreadPoolExecutor();
    }

    public ExecutorService getThreadPool(Engine engine) throws SQLException {
        ExecutorService service;
        switch (engine) {
            case TIDB:
                service = tidbThreadPool;
                break;
            case CLICKHOUSE:
                service = clickhouseThreadPool;
                break;
            case DORIS:
                service = dorisThreadPool;
                break;
            case MYSQL:
                service = mysqlThreadPool;
                break;
            case HIVE:
                service = hiveThreadPool;
                break;
            default:
                throw new SQLException("unknown engine: " + engine);
        }
        return service;
    }

    public static class FenseThreadPoolRejectedExecutionHandler implements RejectedExecutionHandler {

        public FenseThreadPoolRejectedExecutionHandler() { }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            log.error("Task " + r.toString() + " rejected from " + e.toString());
            throw new RejectedExecutionException("fense-server is busy, rejected!");
        }
    }

    @Override
    public void close() {
        closeThreadPool(tidbThreadPool, Engine.TIDB.name(), defaultShutdownTimeout);
        closeThreadPool(mysqlThreadPool, Engine.MYSQL.name(), defaultShutdownTimeout);
        closeThreadPool(dorisThreadPool, Engine.DORIS.name(), defaultShutdownTimeout);
        closeThreadPool(clickhouseThreadPool, Engine.CLICKHOUSE.name(), defaultShutdownTimeout);
        closeThreadPool(hiveThreadPool, Engine.HIVE.name(), hiveShutdownTimeout);
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("", e);
        }

    }

    public void closeThreadPool(ExecutorService executorService, String name, long timeout) {
        log.warn("close {} thread pool.", name);
        Thread thread = new Thread(() -> {
            executorService.shutdown();
            try {
                if(!executorService.awaitTermination(timeout, TimeUnit.SECONDS)){
                    executorService.shutdownNow();
                }
            } catch (InterruptedException ignore) {
                executorService.shutdownNow();
            } finally {
                latch.countDown();
            }
        });
        thread.start();
    }
}
