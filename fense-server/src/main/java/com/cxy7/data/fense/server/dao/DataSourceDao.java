package com.cxy7.data.fense.server.dao;

import com.cxy7.data.fense.server.model.DataSource;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 15:23
 */
@Mapper
public interface DataSourceDao {
    Optional<DataSource> getOne(int id);
    int save(DataSource datasource);
    int update(DataSource datasource);
    int deleteById(int id);
    int count();
    List<DataSource> findAll();
}
