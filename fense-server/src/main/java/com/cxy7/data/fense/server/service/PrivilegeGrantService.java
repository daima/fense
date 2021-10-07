package com.cxy7.data.fense.server.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.server.dao.DatasetDao;
import com.cxy7.data.fense.server.dao.PrivilegeDao;
import com.cxy7.data.fense.server.dao.RoleDao;
import com.cxy7.data.fense.server.model.Dataset;
import com.cxy7.data.fense.server.model.Privilege;
import com.cxy7.data.fense.server.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Service
public class PrivilegeGrantService {
    private static final Logger logger = LoggerFactory.getLogger(PrivilegeGrantService.class);
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private PrivilegeDao privilegeDao;
    @Autowired
    private DatasetDao datasetDao;
    @Autowired
    private RoleDao roleDao;

    /**
     * 将权限授予给角色
     * @param privilegeId
     * @param roleId
     * @return
     */
    public void grant(long privilegeId, int roleId){
        privilegeDao.getOne(privilegeId).orElseThrow(() -> new IllegalArgumentException("privilege does not exist" + privilegeId));
        roleDao.getOne(roleId).orElseThrow(() -> new IllegalArgumentException("role does not exist" + roleId));
        privilegeDao.grant(privilegeId, roleId);
    }

    public int totalPage(int total, int pageSize) {
        int rel = total % pageSize;
        int totalPage = rel == 0 ? total / pageSize : total / pageSize + 1;
        return totalPage;
    }

    public void revoke(long privilegeId, int roleId){
        privilegeDao.getOne(privilegeId).orElseThrow(() -> new IllegalArgumentException("privilege does not exist" + privilegeId));
        roleDao.getOne(roleId).orElseThrow(() -> new IllegalArgumentException("role does not exist" + roleId));
        privilegeDao.revoke(privilegeId, roleId);
    }

    /**
     * 查询指定Dataset有权限的角色列表
     * @param pageable
     * @param datasetId
     * @return 有权限的角色列表
     */
    public JSONObject listByDatasetId(PageRequest pageable, long datasetId) {
        Dataset dataset = datasetDao.getOne(datasetId).orElseThrow(() -> new IllegalArgumentException("dataset does not exist" + datasetId));
        String datasetName = dataset.getName();
        JSONArray arr = new JSONArray();
        List<Privilege> privileges = privilegeDao.findByDatasetId(datasetId);
        for (Privilege privilege : privileges) {
            long privilegeId = privilege.getId();
            List<Role> roles = roleDao.findByPrivilegeId(privilegeId);
            for (Role role : roles) {
                JSONObject tmpObj = new JSONObject();
                tmpObj.put("id", privilegeId + "_" + role.getId());
                tmpObj.put("pid", privilegeId);
                tmpObj.put("name", privilege.getName());
                tmpObj.put("dataset", datasetName);
                tmpObj.put("mode", privilege.getMode());
                tmpObj.put("role_id", role.getName());
                arr.add(tmpObj);
            }
        }
        int total = arr.size();
        JSONObject obj = new JSONObject();
        obj.put("rows", arr);
        obj.put("records", total);
        obj.put("page", pageable.getPageNumber() + 1);
        obj.put("total", totalPage((int)total, pageable.getPageSize()));
        return obj;
    }

    /**
     * 查询当前用户对该数据集的权限
     * @param pageable
     * @param datasetId
     * @param userId
     * @return
     */
    public JSONObject listByDatasetIdAndUser(PageRequest pageable, long datasetId, int userId) {
        JSONObject obj = new JSONObject();
        List<Role> userRoles = roleDao.findByUserId(userId);
        Map<Integer, Role> map = new HashMap<>();
        if (userRoles != null) {
            for (Role role : userRoles)
                map.put(role.getId(), role);
        }

        Optional<Dataset> op = datasetDao.getOne(datasetId);
        if (!op.isPresent()) {
            logger.error("数据集不存在! id: {}", datasetId);
            return obj;
        }
        Dataset dataset = op.get();

        JSONArray arr = new JSONArray();
        List<Privilege> privileges = privilegeDao.findByDatasetId(datasetId);
        for (Privilege privilege : privileges) {
            List<Role> roles = roleDao.findByPrivilegeId(privilege.getId());
            for (Role role : roles) {
                int roleId = role.getId();
                if (!map.containsKey(roleId)){
                    continue;
                }
                JSONObject tmpObj = new JSONObject();
                tmpObj.put("id", privilege.getId());
                tmpObj.put("name", privilege.getName());
                tmpObj.put("mode", privilege.getMode());
                tmpObj.put("role_id", roleId);
                tmpObj.put("dataset", dataset.getName());
                tmpObj.put("role_name", role.getName());
                arr.add(tmpObj);
            }
        }
        int total = arr.size();
        obj.put("rows", arr);
        obj.put("records", total);
        obj.put("page", pageable.getPageNumber() + 1);
        obj.put("total", totalPage((int)total, pageable.getPageSize()));
        return obj;
    }
}
