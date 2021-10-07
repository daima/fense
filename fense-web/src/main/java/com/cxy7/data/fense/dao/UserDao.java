package com.cxy7.data.fense.dao;

import com.cxy7.data.fense.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 15:24
 */
@Mapper
public interface UserDao {
    Optional<User> getOne(int id);
    int deleteById(int id);
    int save(User role);
    int update(User role);
    User findByNameAndPassword(String name, String password);
    User findByEmailAndPassword(String email, String password);
    Optional<User> getOneByName(String name);
    Optional<User> getOneByEmail(String email);
    int count();
    List<User> findAll();
    List<User> findByRoleId(int roleId);
}
