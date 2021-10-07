package com.cxy7.data.fense.server.service;

import com.cxy7.data.fense.server.model.QueryLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/20 19:25
 */
@Component
public class QueryLogAppender implements CommandLineRunner {
    private static Logger LOG = LoggerFactory.getLogger(QueryLogAppender.class);
    private volatile static Set<String> blackList = new HashSet<>();
    @Autowired
    private QueryLogService queryLogService;

    private void runOnce(QueryLog log) {
        queryLogService.log(log);
    }

    public void close() {
        QueryLogQueue.running = false;
    }

    @Override
    public void run(String... args) {
        LOG.info("Start QueueLogAppender...");
        while (QueryLogQueue.running) {
            QueryLog log = QueryLogQueue.poll();
            if (log == null) {
                continue;
            }
            try {
                runOnce(log);
            } catch (Exception e) {
                LOG.error("消费线程有未处理的异常！", e);
            }
        }
    }

    @PreDestroy
    public void stop() {
        LOG.info("Stop QueueLogAppender...");
        QueryLogQueue.running = false;
    }

}
