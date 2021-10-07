package com.cxy7.data.fense.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.dao.ApplyDao;
import com.cxy7.data.fense.dao.DatasetDao;
import com.cxy7.data.fense.model.*;
import com.cxy7.data.fense.security.DefaultAuthenticationService;
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
public class ApplyService {
    private static final Logger logger = LoggerFactory.getLogger(ApplyService.class);
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private ApplyDao applyDao;
    @Autowired
    private PrivilegeService privilegeService;
    @Autowired
    private DefaultAuthenticationService authenticationService;
    @Autowired
    private DatasetDao datasetDao;

    public Apply add(User applicant, long datasetId, String name, String mode){
        PrivilegeMode pm = PrivilegeMode.findByMode(mode);
        Privilege privilege = privilegeService.add(name, datasetId, mode);
        if (privilege == null) {
            logger.error("添加权限时出现异常");
            return null;
        }
        Apply apply = new Apply();
        apply.setPrivilegeId(privilege.getId());
        apply.setApplicant(applicant.getId());
        apply.setReason(name);
        apply.setStatus(ApplyStatus.IN_PROGRESS.value());
        Date now = new Date();
        apply.setCreateTime(now);
        apply.setUpdateTime(now);
        applyDao.save(apply);
        return apply;
    }

    /**
     * 获取我的申请单列表
     * @param pageable
     * @return
     */
    public JSONObject list(PageRequest pageable) {
        User user = authenticationService.getCurrentUser();
        long total = applyDao.countByApplicant(user.getId());

        List<Apply> applies = applyDao.findByApplicant(user.getId());
        JSONArray arr = new JSONArray();
        for (Apply apply : applies) {
            Privilege privilege = apply.getPrivilege();
            long privilegeId = apply.getPrivilegeId();
            JSONObject obj = new JSONObject();
            obj.put("id", apply.getId());
            obj.put("privilege_id", privilegeId);
            Dataset dataset = datasetDao.getOneByPrivilegeId(privilegeId).orElseThrow(() -> new IllegalArgumentException("can't find dataset by privilegeId:" + privilegeId));
            obj.put("dataset", dataset.getName());
            obj.put("privilege_mode", PrivilegeMode.valueOf(privilege.getMode()).name());
            obj.put("applicant", apply.getApplicantUser().getName());
            List<Approval> approvals = apply.getApprovals();
            if (!approvals.isEmpty())
                obj.put("approver", approvals.get(0).getApprover());
            Role role = apply.getRole();
            if (role != null)
                obj.put("role", role.getName());
            obj.put("reason", apply.getReason());
            obj.put("status", ApplyStatus.valueOf(apply.getStatus()).name());
            obj.put("create_time", df.format(apply.getCreateTime()));
            obj.put("update_time", df.format(apply.getUpdateTime()));
            arr.add(obj);
        }
        JSONObject obj = new JSONObject();
        obj.put("rows", arr);
        obj.put("records", total);
        obj.put("page", pageable.getPageNumber() + 1);
        obj.put("total", totalPage((int)total, pageable.getPageSize()));
        return obj;
    }

    public Optional<Apply> get(long id) {
        return applyDao.getOne(id);
    }

    public boolean delete(long id) {
        applyDao.deleteById(id);
        return true;
    }
    public int save(Apply apply) {
        return applyDao.save(apply);
    }

    public int totalPage(int total, int pageSize) {
        int rel = total % pageSize;
        int totalPage = rel == 0 ? total / pageSize : total / pageSize + 1;
        return totalPage;
    }

    public void update(long applyId, String reason) {
        Optional<Apply> op = applyDao.getOne(applyId);
        if (!op.isPresent()) {
            logger.warn("未找到申请单：{}", applyId);
            return;
        }
        Apply apply = op.get();
        apply.setReason(reason);
        applyDao.update(apply);
    }
}
