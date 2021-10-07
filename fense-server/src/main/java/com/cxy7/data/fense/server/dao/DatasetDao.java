package com.cxy7.data.fense.server.dao;

import com.cxy7.data.fense.server.model.Dataset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 15:22
 */
@Mapper
public interface DatasetDao {
    Optional<Dataset> getOne(long id);
    int save(Dataset dataset);
    int update(Dataset dataset);
    int deleteById(long id);
    List<Dataset> findAllByTypeAndDatasource(@Param("type") int type, @Param("datasource_id") int datasourceId);
    List<Dataset> findAllByParent(long parent);
    Optional<Dataset> getOneByPrivilegeId(long privilegeId);
}
