package com.cxy7.data.fense.utils;

import com.alibaba.druid.util.JdbcUtils;
import com.cxy7.data.fense.model.Dataset;
import com.cxy7.data.fense.model.Datasource;
import com.cxy7.data.fense.service.DatasetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Component
public class DatasourceUtils {
    private static final Logger logger = LoggerFactory.getLogger(DatasourceUtils.class);
    @Autowired
    private DatasetService datasetService;
    @Value("${encrypt.key.datasource.password}")
    private String KEY;

    public Connection getConnection(Datasource ds) {
        return getConnection(ds.getJdbcUrl(), ds.getUser(), ds.getPass());
    }
    public Connection getConnection(String url, String user, String pass) {
        try{
            Class.forName("com.mysql.jdbc.Driver");
            pass = AesUtil.decrypt(pass, KEY);
            Connection conn = DriverManager.getConnection(url, user, pass);
            //获取连接对象
            return conn;
        } catch(ClassNotFoundException e){
            e.printStackTrace();
            return null;
        } catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean parseDatasource(Datasource ds) {
        boolean succ = false;
        Map<String, Long> dbMap = parseDb(ds);
        List<Dataset> datasets = parseTable(ds, dbMap);
        logger.info("解析完成：DB:{}, TABLE:{}", dbMap.size(), datasets.size());
        if (!datasets.isEmpty()) {
            succ = true;
        }
        return succ;
    }

    private Map<String, Long> parseDb(Datasource ds) {
        Map<String, Long> dbMap = new HashMap<>();
        try {
            Connection connection = getConnection(ds);
            if (connection != null) {
                List<Object> param = new ArrayList<>();
                List<Map<String, Object>> list = JdbcUtils.executeQuery(connection, "SHOW DATABASES;", param);
                for (Map<String, Object> map : list) {
                    String db = (String)map.get("Database");
                    Dataset dataset = datasetService.addDb(ds, Dataset.DB_PARENT, db);
                    dbMap.put(dataset.getName(), dataset.getId());
                }
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("", e);
        }
        return dbMap;
    }

    private List<Dataset> parseTable(Datasource ds, Map<String, Long> dbMap) {
        List<Dataset> datasets = new ArrayList<>();
        try {
            Connection connection = getConnection(ds);
            if (connection != null) {
                List<Object> param = new ArrayList<>();
                String sql = "SELECT TABLE_SCHEMA, TABLE_NAME FROM information_schema.TABLES;";
                List<Map<String, Object>> list = JdbcUtils.executeQuery(connection, sql, param);
                for (Map<String, Object> map : list) {
                    String db = (String)map.get("TABLE_SCHEMA");
                    String table = (String)map.get("TABLE_NAME");

                    long parent = dbMap.get(db);
                    Dataset dataset = datasetService.addTable(ds, parent, table);
                    datasets.add(dataset);
                }
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("", e);
        }
        return datasets;
    }
}
