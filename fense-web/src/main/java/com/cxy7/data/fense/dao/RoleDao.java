package com.cxy7.data.fense.dao;

import com.cxy7.data.fense.model.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 15:24
 */
@Mapper
public interface RoleDao {
    Optional<Role> getOne(int id);
    int deleteById(int id);
    int save(Role role);
    int update(Role role);
    int count();
    List<Role> findAll();
    Optional<Role> findByName(@Param("name") String name);
    List<Role> findByUserId(int userId);
    int grant(int roleId, int userId);
    int revoke(int roleId, int userId);
    List<Role> findByPrivilegeId(long privilegeId);
}
