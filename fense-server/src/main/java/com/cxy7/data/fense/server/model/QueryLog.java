package com.cxy7.data.fense.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: XiaoYu
 * @date: 2021/3/13 12:05 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QueryLog implements Serializable {
    private int type;
    private Date eventDate;
    private Date eventTime;
    private Date queryStartTime;
    private long queryDurationMs;
    private long resultRows;
    private long resultBytes;
    private String query;
    private String exception;
    private String user;
    private String queryId;
    private String address;
    private int port;
    private String osUser;
    private String clientHostname;
    private String clientName;
    private String clientVersion;
    private int httpMethod;
    private String httpUserAgent;
}
