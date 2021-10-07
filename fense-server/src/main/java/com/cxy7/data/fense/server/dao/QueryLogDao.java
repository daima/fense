package com.cxy7.data.fense.server.dao;

import com.cxy7.data.fense.server.model.QueryLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/20 16:32
 */
@Mapper
public interface QueryLogDao {
    int save(QueryLog queryLog);
}
