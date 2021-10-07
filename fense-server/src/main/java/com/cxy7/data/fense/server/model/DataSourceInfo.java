package com.cxy7.data.fense.server.model;

import com.cxy7.data.fense.jdbc.Engine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/4 16:52
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataSourceInfo {
    private int dsId;
    private DataSource dataSource;
    private Properties properties;
    private Engine engine;
}
