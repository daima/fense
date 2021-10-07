package com.cxy7.data.fense.server.jdbc;

import com.cxy7.data.fense.jdbc.Engine;
import com.cxy7.data.fense.server.model.DbTable;
import com.cxy7.data.fense.server.model.User;
import com.cxy7.data.fense.server.service.PrivilegeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/4 17:33
 */
@Component
@Slf4j
public class PrivilegeChecker {
    @Autowired
    private PrivilegeService privilegeService;

    public boolean check(User user, Engine engine, int dsId, List<DbTable> dbTables) throws SQLException {
        boolean pass = true;
        if (dbTables.size() == 0) {
            return pass;
        }
        for (DbTable dt : dbTables) {
            boolean succ = checkPrivilege(user.getId(), dsId, engine, dt);
            if (!succ) {
                throw new SQLException(String.format("Permission denied! user: %s, dsId: %d, table: %s", user.getName(), dsId, dt.toString()));
            }
        }
        return pass;
    }

    public boolean checkPrivilege(int userId, int dsId, Engine engine, DbTable dbTable) {
        return privilegeService.check(userId, dsId, dbTable.getDatabaseName(), dbTable.getTableName());
    }
}

