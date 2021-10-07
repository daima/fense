package com.cxy7.data.fense.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 审批单
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Approval {
    private long id;
    private long applyId;
    private int approver;
    private int status;
    private String opinion;
    private int nextapprover = 1;// 默认为管理员
    private Date createTime;
    private Date updateTime;

    private Apply apply;
}
