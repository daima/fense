package com.cxy7.data.fense.server;

import com.cxy7.data.fense.utils.ResultSetUtils;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;
import java.util.Properties;

public class DriverTest {
    private static final Properties props = new Properties();
    private static final String url = "jdbc:fense://127.0.0.1:8081/fense?dsId=1";
    @Before
    public void init() throws ClassNotFoundException {
        Class.forName("com.cxy7.data.fense.Driver");
        props.put("user", "XiaoYu");
        props.put("password", "GoodMan");
    }

    @Test
    public void testQuery() throws SQLException {
        String sql = "select * from query_log;";
        Connection conn = DriverManager.getConnection(url, props);
        Statement ps = conn.createStatement();
        ResultSet results = ps.executeQuery(sql);
        ResultSetUtils.printResultSet(results);
        if (conn != null) {
            conn.close();
        }
        if (ps != null) {
            ps.close();
        }
    }

}
