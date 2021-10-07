package com.cxy7.data.fense.server.model;

import com.cxy7.data.fense.jdbc.Engine;
import lombok.Builder;
import lombok.Data;

import java.sql.Statement;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/5 19:36
 */
@Data
@Builder
public class QueryTask {
    private int dsId;
    private String sql;
    private String schema;
    private String user;
    private Engine engine;
    private Statement statement;
}
