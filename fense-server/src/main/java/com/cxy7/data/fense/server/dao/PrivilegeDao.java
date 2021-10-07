package com.cxy7.data.fense.server.dao;

import com.cxy7.data.fense.server.model.Privilege;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 15:24
 */
@Mapper
public interface PrivilegeDao {
    Optional<Privilege> getOne(long id);
    int save(Privilege privilege);
    int update(Privilege privilege);
    int deleteById(long id);
    int grant(long privilegeId, int roleId);
    int revoke(long privilegeId, int roleId);
    List<Privilege> findByDatasetId(long datasetId);
    int check(int userId, int dsId, String databaseName, String tableName);
}
