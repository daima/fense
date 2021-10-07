package com.cxy7.data.fense.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.dao.RoleDao;
import com.cxy7.data.fense.dao.UserDao;
import com.cxy7.data.fense.model.Role;
import com.cxy7.data.fense.model.User;
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
public class RoleService {
    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private UserDao userDao;

    public Optional<Role> get(int roleId) {
        return roleDao.getOne(roleId);
    }
    public Role add(String name){
        Optional<Role> op = roleDao.findByName(name);
        if (op.isPresent()) {
            logger.warn("该角色已存在:{}", name);
            return op.get();
        }
        Role role = new Role();
        role.setName(name);
        Date now = new Date();
        role.setCreateTime(now);
        role.setUpdateTime(now);
        roleDao.save(role);
        return role;
    }

    public JSONObject list(PageRequest pageable) {
        long total = roleDao.count();

        List<Role> roles = roleDao.findAll();
        JSONArray arr = new JSONArray();
        for (Role role : roles) {
            JSONObject obj = new JSONObject();
            obj.put("id", role.getId());
            obj.put("name", role.getName());
            obj.put("create_time", df.format(role.getCreateTime()));
            obj.put("update_time", df.format(role.getUpdateTime()));
            obj.put("edit_url", "<a target='_blank' href='/role/edit?id=" + role.getId() + "'>編輯模式</a>");
            arr.add(obj);
        }
        JSONObject obj = new JSONObject();
        obj.put("rows", arr);
        obj.put("records", total);
        obj.put("page", pageable.getPageNumber() + 1);
        obj.put("total", totalPage((int)total, pageable.getPageSize()));
        return obj;
    }

    public JSONArray listGrants() {
        JSONArray dsArr = new JSONArray();
        List<Role> roles = roleDao.findAll();
        for (Role role : roles) {
            int roleId = role.getId();
            JSONObject dsObj = new JSONObject();
            dsObj.put("id", roleId);
            dsObj.put("text", role.getName());
            dsObj.put("icon", "fa fa-users");
            JSONArray dbArr = new JSONArray();
            List<User> users = userDao.findByRoleId(roleId);
            for (User user : users) {
                int userId = user.getId();
                JSONObject dbObj = new JSONObject();
                dbObj.put("id", roleId + "_" + userId);
                dbObj.put("text", user.getName());
                dbObj.put("icon", "fa fa-user-o");
                dbArr.add(dbObj);
            }
            dsObj.put("children", dbArr);
            dsArr.add(dsObj);
        }
        return dsArr;
    }

    public int totalPage(int total, int pageSize) {
        int rel = total % pageSize;
        int totalPage = rel == 0 ? total / pageSize : total / pageSize + 1;
        return totalPage;
    }

    public boolean edit(int id, String name) {
        boolean succ = false;
        Optional<Role> roleOptional = roleDao.findByName(name);
        if (roleOptional.isPresent()) {
            logger.warn("该角色已存在:{}", name);
            return succ;
        }
        Optional<Role> op = roleDao.getOne(id);
        if (op.isPresent()) {
            Role role = op.get();
            role.setName(name);
            roleDao.update(role);
            succ = true;
        }
        return succ;
    }

    public boolean delete(int id) {
        roleDao.deleteById(id);
        return true;
    }

    public String listForGrant() {
        StringBuilder sb = new StringBuilder();
        sb.append("<select>");
        String pattern = "<option value='%d'>%s</option>";
        List<Role> roles = roleDao.findAll();
        for (Role role : roles) {
            String option = String.format(pattern, role.getId(), role.getName());
            sb.append(option);
        }
        sb.append("</select>");
        return sb.toString();
    }

    public String listByUser(int userId) {
        List<Role> roles = roleDao.findByUserId(userId);

        StringBuilder sb = new StringBuilder();
        String pattern = "<option value='%d'>%s</option>";
        for (Role role : roles) {
            String option = String.format(pattern, role.getId(), role.getName());
            sb.append(option);
        }
        return sb.toString();
    }
    public void grant(int roleId, int userId) {
        Optional<Role> op = roleDao.getOne(roleId);
        if (!op.isPresent()) {
            logger.error("未找到该角色:{}", roleId);
            return;
        }
        Role role = op.get();
        Optional<User> userOptional = userDao.getOne(userId);
        if (!userOptional.isPresent()) {
            logger.error("未找到该用户:{}", userId);
            return;
        }
        User user = userOptional.get();
        roleDao.grant(roleId, userId);
    }

    public void revoke(int roleId, int userId) {
        Optional<Role> op = roleDao.getOne(roleId);
        if (!op.isPresent()) {
            logger.error("未找到该角色:{}", roleId);
            return;
        }
        Role role = op.get();

        Optional<User> userOptional = userDao.getOne(userId);
        if (!userOptional.isPresent()) {
            logger.error("未找到该用户:{}", userId);
            return;
        }
        User user = userOptional.get();
        roleDao.revoke(roleId, userId);
    }

    public JSONObject getAuthenticatedUsers(PageRequest pageable, int roleId) {
        Role role = roleDao.getOne(roleId).orElseThrow(() -> new IllegalArgumentException("row does not exist!" + roleId));
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();
        List<User> users = userDao.findByRoleId(roleId);
        for (User user : users) {
            int userId = user.getId();
            JSONObject tmpObj = new JSONObject();
            tmpObj.put("id", roleId + "_" + userId);
            tmpObj.put("role_id", roleId);
            tmpObj.put("user_id", userId);
            tmpObj.put("name", user.getName());
            tmpObj.put("create_time", df.format(user.getCreateTime()));
            arr.add(tmpObj);
        }
        int total = arr.size();
        obj.put("rows", arr);
        obj.put("records", total);
        obj.put("page", pageable.getPageNumber() + 1);
        obj.put("total", totalPage((int)total, pageable.getPageSize()));
        return obj;
    }
}
