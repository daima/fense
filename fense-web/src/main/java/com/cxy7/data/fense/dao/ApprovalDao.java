package com.cxy7.data.fense.dao;

import com.cxy7.data.fense.model.Approval;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 15:22
 */
@Mapper
public interface ApprovalDao {
    Optional<Approval> getOne(long id);
    int save(Approval approval);
    int upate(Approval approval);
    int deleteById(long id);
    List<Approval> findByApprover(int approver);
}
