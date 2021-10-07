package com.cxy7.data.fense.server.service;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.cxy7.data.fense.jdbc.Engine;
import com.cxy7.data.fense.server.dao.DataSourceDao;
import com.cxy7.data.fense.server.model.DataSourceInfo;
import com.cxy7.data.fense.utils.AesUtil;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/20 16:39
 */
@Component
public class DataSourceCache {
    private static Logger LOG = LoggerFactory.getLogger(DataSourceCache.class);
    private volatile static Map<Integer, DataSourceInfo> cache = new HashMap<>();
    @Autowired
    private DataSourceDao dataSourceDao;
    @Value("${encrypt.key.datasource.password}")
    private String aesKey;

    public DataSourceInfo getDataSourceInfo(int dsId) {
        Preconditions.checkNotNull(dsId, "datasource id can't be null");
        return getDataSource(dsId);
    }

    public DataSourceInfo getDataSource(int dsId) {
        Preconditions.checkNotNull(dsId, "datasource id can't be null");
        if (cache.containsKey(dsId)) {
            return cache.get(dsId);
        }
        DataSourceInfo ds = makeDataSource(dsId);
        if (ds != null) {
            cache.put(dsId, ds);
        }

        return ds;
    }

    private DataSourceInfo makeDataSource(int dsId) {
        Preconditions.checkNotNull(dsId, "dsId required.");
        Optional<com.cxy7.data.fense.server.model.DataSource> optional = dataSourceDao.getOne(dsId);
        if (!optional.isPresent()) {
            LOG.warn("unknown datasource: {}", dsId);
            return null;
        }
        com.cxy7.data.fense.server.model.DataSource ds = optional.get();
        String poolConf = ds.getPoolConf();
        Properties properties = new Properties();
        if (Objects.nonNull(poolConf)) {
            try {
                properties.load(new StringReader(poolConf));
            } catch (IOException e) {
                LOG.warn("error occurs during load pool config.");
            }
        }
        properties.put("url", ds.getJdbcUrl());
        properties.put("username", ds.getUser());
        String password = ds.getPass();
        properties.put("password", AesUtil.decrypt(password, aesKey));
        DataSource dataSource = null;
        try {
            dataSource = DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            LOG.warn("failed to create datasource.", e);
        }
        Engine engine = Objects.requireNonNull(Engine.lookup(ds.getEngine()), String.format("unknown engine: %s", ds.getEngine()));

        return new DataSourceInfo(dsId, dataSource, properties, engine);
    }
}
