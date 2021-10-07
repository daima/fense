package com.cxy7.data.fense.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/20 16:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataSource implements Serializable {
    private int id;
    private String name;
    private String jdbcUrl;
    private String user;
    private String pass;
    private String engine;
    private String poolConf;
    private int createUser;
    private Date createTime;
    private Date updateTime;
}
