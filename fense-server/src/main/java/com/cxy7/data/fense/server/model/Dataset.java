package com.cxy7.data.fense.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Dataset {
    public static final long DB_PARENT = 1L;
    private long id;
    private long parent;
    private String name;
    private int datasourceId;
    private int type;
    private int owner;
    private Date createTime;
    private Date updateTime;
}
