package com.cxy7.data.fense.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 申请单
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Apply {
    private long id;
    private int applicant;
    private int roleId;
    private long privilegeId;
    private String reason;
    private int status;
    private Date createTime;
    private Date updateTime;

    private User applicantUser;
    private Role role;
    private Privilege privilege;
    private List<Approval> approvals;
}
