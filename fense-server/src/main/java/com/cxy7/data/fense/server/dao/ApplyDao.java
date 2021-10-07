package com.cxy7.data.fense.server.dao;

import com.cxy7.data.fense.server.model.Apply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 15:08
 */
@Mapper
public interface ApplyDao {
    int save(Apply apply);
    int update(Apply apply);
    Optional<Apply> getOne(long id);
    int deleteById(long id);
    int countByApplicant(@Param("applicant") int applicant);
    List<Apply> findByApplicant(@Param("applicant") int applicant);
}
