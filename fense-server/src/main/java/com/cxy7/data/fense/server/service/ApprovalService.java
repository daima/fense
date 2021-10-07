package com.cxy7.data.fense.server.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.server.dao.ApplyDao;
import com.cxy7.data.fense.server.dao.ApprovalDao;
import com.cxy7.data.fense.server.dao.DatasetDao;
import com.cxy7.data.fense.server.dao.UserDao;
import com.cxy7.data.fense.server.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Service
public class ApprovalService {
    private static final Logger logger = LoggerFactory.getLogger(ApprovalService.class);
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private ApprovalDao approvalDao;
    @Autowired
    private ApplyDao applyDao;
    @Autowired
    private PrivilegeGrantService privilegeGrantService;
    @Autowired
    private DatasetDao datasetDao;
    @Autowired
    private RoleService roleService;
    @Autowired
    private UserDao userDao;

    /**
     * 创建审批单
     * 发送给数据的负责人，如果没有负责人，则默认为管理员
     * @param apply
     * @param datasetId
     * @return
     */
    public Approval add(Apply apply, long datasetId){
        Optional<Dataset> op = datasetDao.getOne(datasetId);
        if (!op.isPresent()) {
            logger.error("未找到该数据集：" + datasetId);
            return null;
        }
        int approver = op.get().getOwner();
        Approval approvals = new Approval();
        approvals.setApplyId(apply.getId());
        approvals.setApprover(approver);
        String opinion = "同意";// 审批意见默认为空
        approvals.setOpinion(opinion);
//        approvals.setNextapprover(nextapprover);
        approvals.setStatus(ApprovalStatus.IN_PROGRESS.value()); // 第一个状态为处理中
        Date now = new Date();
        approvals.setCreateTime(now);
        approvals.setUpdateTime(now);
        approvalDao.save(approvals);
        return approvals;
    }

    public JSONObject list(PageRequest pageable, int approver) {
        userDao.getOne(approver).orElseThrow(() -> new IllegalArgumentException("This user does not exit!"));
        List<Approval> approvals = approvalDao.findByApprover(approver);
        long total = approvals.size();
        JSONArray arr = new JSONArray();
        for (Approval approval : approvals) {
            JSONObject obj = new JSONObject();
            obj.put("id", approval.getId());
            Apply apply = approval.getApply();
            obj.put("applyid", apply.getId());
            obj.put("applicant", apply.getApplicant());
            obj.put("reason", apply.getReason());
            Role role = apply.getRole();
            if (role != null)
                obj.put("role", role.getName());
            obj.put("opinion", approval.getOpinion());
            obj.put("status", ApprovalStatus.valueOf(approval.getStatus()));
            obj.put("update_time", df.format(approval.getUpdateTime()));
            arr.add(obj);
        }
        JSONObject obj = new JSONObject();
        obj.put("rows", arr);
        obj.put("records", total);
        obj.put("page", pageable.getPageNumber() + 1);
        obj.put("total", totalPage((int)total, pageable.getPageSize()));
        return obj;
    }

    public Optional<Approval> get(long id) {
        return approvalDao.getOne(id);
    }

    public boolean delete(long id) {
        approvalDao.deleteById(id);
        return true;
    }

    public int totalPage(int total, int pageSize) {
        int rel = total % pageSize;
        int totalPage = rel == 0 ? total / pageSize : total / pageSize + 1;
        return totalPage;
    }

    /**
     * 审批权限
     * @param approval
     */
    public boolean disagree(Approval approval) {
        boolean succ = false;
        approval.setStatus(ApprovalStatus.DISAGREE.value());
        approval.setOpinion("不同意");
        approvalDao.save(approval);

        Apply apply = applyDao.getOne(approval.getApplyId()).get();
        apply.setStatus(ApplyStatus.FAILED.value());
        applyDao.save(apply);
        return succ;
    }

    /**
     * 新建权限，将权限赋给角色
     * @param approval
     * @param roleId
     * @return
     */
    public boolean agree(Approval approval, int roleId, String opinion) {

        approval.setStatus(ApprovalStatus.AGREE.value());
        approval.setOpinion(opinion);
        approvalDao.save(approval);

        Apply apply = applyDao.getOne(approval.getApplyId()).get();
        apply.setRoleId(roleId);
        apply.setStatus(ApplyStatus.SUCCESS.value());
        applyDao.save(apply);

        // 授权给角色
        int userId = apply.getApplicant();
        privilegeGrantService.grant(apply.getPrivilegeId(), roleId);
        // 把角色赋给用户
        roleService.grant(roleId, userId);

        return true;
    }
}
