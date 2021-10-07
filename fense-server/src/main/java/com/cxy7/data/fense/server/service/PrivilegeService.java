package com.cxy7.data.fense.server.service;

import com.cxy7.data.fense.server.dao.DatasetDao;
import com.cxy7.data.fense.server.dao.PrivilegeDao;
import com.cxy7.data.fense.server.model.Dataset;
import com.cxy7.data.fense.server.model.Privilege;
import com.cxy7.data.fense.server.model.PrivilegeMode;
import com.cxy7.data.fense.server.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Service
public class PrivilegeService {
    private static final Logger logger = LoggerFactory.getLogger(PrivilegeService.class);
    @Autowired
    private PrivilegeDao privilegeDao;
    @Autowired
    private DatasetDao datasetDao;
    @Autowired
    private DefaultAuthenticationService defaultAuthenticationService;

    public Privilege add(String name, long datasetId, String mode){
        PrivilegeMode pm = PrivilegeMode.findByMode(mode);
        if (pm == null) {
            logger.error("不支持的权限模式：" + mode);
            return null;
        }
        Privilege privilege = new Privilege();
        privilege.setName(name);
        privilege.setDatasetId(datasetId);
        privilege.setMode(pm.value());
        User user = defaultAuthenticationService.getCurrentUser();
        privilege.setCreateUser(user.getId());
        Date now = new Date();
        privilege.setCreateTime(now);
        privilege.setUpdateTime(now);
        privilegeDao.save(privilege);
        return privilege;
    }

    public boolean update(long id, String name, Dataset dataset, PrivilegeMode mode) {
        boolean succ = false;
        Privilege privilege = privilegeDao.getOne(id).orElseThrow(() -> new IllegalArgumentException("privilege does not exist!" + id));
        privilege.setName(name);
        privilege.setDatasetId(dataset.getId());
        privilege.setMode(mode.value());
        privilegeDao.update(privilege);
        succ = true;
        return succ;
    }

    public boolean check(int userId, int dsId, String databaseName, String tableName) {
        int rows = privilegeDao.check(userId, dsId, databaseName, tableName);
        return rows > 0;
    }

    public Optional<Privilege> get(long id) {
        return privilegeDao.getOne(id);
    }

    public boolean delete(long id) {
        privilegeDao.deleteById(id);
        return true;
    }
}
