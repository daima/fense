package com.cxy7.data.fense.server.service;

import com.cxy7.data.fense.server.model.QueryLog;
import com.cxy7.data.fense.utils.PropsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/20 19:23
 */
public class QueryLogQueue {
    private static Logger LOG = LoggerFactory.getLogger(QueryLogQueue.class);
    public static volatile boolean running = true;
    private static int capacity = PropsUtil.getInteger("server.queue.capacity", 500);
    private static int wait_s = PropsUtil.getInteger("server.queue.offer.wait_s", 30);
    private static ArrayBlockingQueue<QueryLog> queue = new ArrayBlockingQueue<QueryLog>(capacity);

    public static boolean append(QueryLog log) {
        boolean result = false;
        try {
            result = queue.offer(log, wait_s, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static QueryLog poll() {
        QueryLog item = null;
        try {
            item = queue.poll(wait_s, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted.", e);
        }
        return item;
    }

    public static boolean isEmpty() {
        return size() == 0;
    }

    public static int size() {
        return queue.size();
    }
}
