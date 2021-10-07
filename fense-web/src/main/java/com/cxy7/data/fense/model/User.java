package com.cxy7.data.fense.model;

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
public class User implements Serializable {
    public static final String SESSION_KEY = "USER_INFO";
    private int id;
    private String name;
    private String email;
    private String password;
    private int isAdmin;
    private Date lastLoginTime;
    private Date createTime;
    private Date updateTime;
}
