package com.cxy7.data.fense.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: XiaoYu
 * @date: 2021/3/13 5:34 下午
 */
public class ResultSetUtils {
    public static void printResultSet(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            Map<String, Object> jo = new HashMap<>();
            int n = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= n; i++) {
                jo.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
                System.out.print(resultSet.getObject(i));
            }
            System.out.println("");
        }
    }
}
