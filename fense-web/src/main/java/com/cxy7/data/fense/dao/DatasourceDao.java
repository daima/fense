package com.cxy7.data.fense.dao;

import com.cxy7.data.fense.model.Datasource;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 15:23
 */
@Mapper
public interface DatasourceDao {
    Optional<Datasource> getOne(int id);
    int save(Datasource datasource);
    int update(Datasource datasource);
    int deleteById(int id);
    int count();
    List<Datasource> findAll();
}
