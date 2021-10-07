package com.cxy7.data.fense.server.jdbc;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.MetaImpl;
import org.apache.calcite.avatica.QueryState;
import org.apache.calcite.avatica.remote.TypedValue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/6 10:55
 */
@Slf4j
public class FenseMetaImpl extends MetaImpl {
    public FenseMetaImpl(AvaticaConnection connection) {
        super(connection);
        this.connProps
                .setAutoCommit(false)
                .setReadOnly(false)
                .setTransactionIsolation(Connection.TRANSACTION_NONE);
        this.connProps.setDirty(false);
    }

    @Override
    public void closeStatement(StatementHandle h) {
        log.info("close statement");
    }

    @Override
    public void openConnection(ConnectionHandle ch, Map<String, String> info) {
        super.openConnection(ch, info);
    }

    @Override
    public void closeConnection(ConnectionHandle ch) {
        super.closeConnection(ch);
    }

//    @Override
//    public Map<String, String> getDataSource(ConnectionHandle ch, String dsKey) {
//        return null;
//    }
//
//    @Override
//    public List<String> getQueryLog(StatementHandle h, boolean incremental, int fetchSize) {
//        return null;
//    }

    @Override
    public StatementHandle prepare(ConnectionHandle ch, String sql,
                                   long maxRowCount) {
        return super.createStatement(ch);
    }

    @Override
    public ExecuteResult prepareAndExecute(StatementHandle h,
                                           String sql, long maxRowCount, PrepareCallback callback) {
        return prepareAndExecute(h, sql, maxRowCount, -1, callback);
    }

    @Override
    public ExecuteResult prepareAndExecute(StatementHandle h,
                                           String sql, long maxRowCount, int maxRowsInFirstFrame,
                                           PrepareCallback callback) {
        try {
            synchronized (callback.getMonitor()) {
                callback.clear();
                callback.assign(h.signature, null, -1);
            }
            callback.execute();
            final MetaResultSet metaResultSet =
                    MetaResultSet.create(h.connectionId, h.id, false, h.signature, null);
            return new ExecuteResult(ImmutableList.of(metaResultSet));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // TODO: share code with prepare and createIterable
    }

    @Override
    public Frame fetch(StatementHandle h, long offset, int fetchMaxRowCount) {
        return null;
    }


    @Deprecated
    @Override
    public ExecuteResult execute(StatementHandle h,
                                 List<TypedValue> parameterValues, long maxRowCount) {
        final MetaResultSet metaResultSet = MetaResultSet.create(h.connectionId, h.id, false, h.signature, null);
        return new ExecuteResult(Collections.singletonList(metaResultSet));
    }

    @Override
    public ExecuteResult execute(StatementHandle h,
                                 List<TypedValue> parameterValues, int maxRowsInFirstFrame) {
        final MetaResultSet metaResultSet = MetaResultSet.create(h.connectionId, h.id, false, h.signature, null);
        return new ExecuteResult(Collections.singletonList(metaResultSet));
    }

    @Override
    public ExecuteBatchResult prepareAndExecuteBatch(
            final StatementHandle h, List<String> sqlCommands) {
        return new ExecuteBatchResult(new long[]{});
    }

    @Override
    public ExecuteBatchResult executeBatch(StatementHandle h, List<List<TypedValue>> parameterValues) {
        return new ExecuteBatchResult(new long[]{});
    }

    public boolean syncResults(StatementHandle h, QueryState state, long offset) {
        return false;
    }

    @Override
    public void commit(ConnectionHandle ch) {
    }

    @Override
    public void rollback(ConnectionHandle ch) {
    }
}
