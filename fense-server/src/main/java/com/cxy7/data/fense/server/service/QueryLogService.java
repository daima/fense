package com.cxy7.data.fense.server.service;

import com.cxy7.data.fense.server.dao.QueryLogDao;
import com.cxy7.data.fense.server.model.QueryLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

/**
 * @author: XiaoYu
 * @date: 2021/3/13 11:40 下午
 */
@Service
public class QueryLogService {
    private static Logger LOG = LoggerFactory.getLogger(QueryLogService.class);
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    @Autowired
    private QueryLogDao queryLogDao;

    public void log(QueryLog queryLog) {
        queryLogDao.save(queryLog);
    }
}
