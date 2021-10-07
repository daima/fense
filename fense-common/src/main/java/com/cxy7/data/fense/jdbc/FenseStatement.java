package com.cxy7.data.fense.jdbc;

import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.AvaticaStatement;
import org.apache.calcite.avatica.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: XiaoYu
 * @Date: 2021/9/27 23:21
 */
public class FenseStatement extends AvaticaStatement {
    private static Logger LOG = LoggerFactory.getLogger(FenseStatement.class);

    private ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    private static final Pattern appIdPattern = Pattern.compile(".* Running with YARN Application = (.*)");
    public static final int FETCH_LOG_SIZE = DEFAULT_FETCH_SIZE;
    private String applicationId;

    protected FenseStatement(AvaticaConnection connection, Meta.StatementHandle h, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        super(connection, h, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public void addLogs(List<String> logs) {
        if (logs.isEmpty()) {
            return;
        }
        for (String line : logs) {
            LOG.info(line);
            Matcher matcher = appIdPattern.matcher(line);
            if (matcher.find()) {
                applicationId = matcher.group(1);
            }
        }
        queue.addAll(logs);
    }

    public String getApplicationId() {
        return applicationId;
    }

//    public List<String> getQueryLog() {
//        return getQueryLog(true, FETCH_LOG_SIZE);
//    }
//
//    public List<String> getQueryLog(boolean incremental, int fetchSize) {
//        return driver.createMeta(connection).getQueryLog(handle, true, fetchSize);
//    }
}
