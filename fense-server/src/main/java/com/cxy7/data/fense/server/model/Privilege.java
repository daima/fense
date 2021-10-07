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
public class Privilege {
    private long id;
    private String name;
    private long datasetId;
    private String mode;
    private int createUser;
    private Date createTime;
    private Date updateTime;
}
