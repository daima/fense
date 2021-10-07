package com.cxy7.data.fense.model;

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
public class Role {
    private int id;
    private String name;
    private Date createTime;
    private Date updateTime;
}
