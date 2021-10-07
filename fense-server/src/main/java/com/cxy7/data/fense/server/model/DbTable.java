package com.cxy7.data.fense.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/4 17:35
 */
@Data
@Builder
@AllArgsConstructor
public class DbTable {
    private String databaseName;
    private String tableName;

    public static DbTable of(String databaseName, String tableName) {
        return new DbTable(databaseName, tableName);
    }
    @Override
    public String toString() {
        return String.format("%s.%s", databaseName, tableName);
    }
}
