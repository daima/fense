package com.cxy7.data.fense.server.jdbc;

import com.alibaba.fastjson.JSONObject;
import com.cxy7.data.fense.jdbc.Engine;
import com.cxy7.data.fense.jdbc.FenseConnection;
import com.cxy7.data.fense.server.model.DataSourceInfo;
import com.cxy7.data.fense.server.model.DbTable;
import com.cxy7.data.fense.server.model.QueryTask;
import com.cxy7.data.fense.server.model.User;
import com.cxy7.data.fense.server.service.DataSourceCache;
import com.cxy7.data.fense.server.service.LoginService;
import com.cxy7.data.fense.utils.ReflectUtils;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.avatica.AvaticaUtils;
import org.apache.calcite.avatica.NoSuchStatementException;
import org.apache.calcite.avatica.jdbc.JdbcMeta;
import org.apache.calcite.avatica.jdbc.StatementInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: XiaoYu
 * @date: 2021/3/13 12:09 下午
 */
@Component
@Slf4j
public class ServerMeta extends JdbcMeta implements Closeable {
    @Value("${jdbc.fetch_size}")
    private int fetchSize;

    @Autowired
    private DataSourceCache dataSourceCache;

    @Autowired
    private SqlParser sqlParser;

    @Autowired
    private PrivilegeChecker privilegeChecker;

    @Autowired
    private JdbcExecutor executor;

    @Autowired
    private LoginService loginService;


    public ServerMeta() throws SQLException {
        super(Driver.CONNECT_STRING_PREFIX, "", "");
    }

    @SneakyThrows
    @Override
    public void openConnection(ConnectionHandle ch, Map<String, String> info) {
        super.openConnection(ch, info);
        Connection conn = Objects.requireNonNull(getConnection(ch.id), String.format("error while create connection:%s", ch.id));
        FenseConnection fenseConnection = (FenseConnection)conn;
        int dsId = fenseConnection.getDsId();
        DataSourceInfo dataSourceInfo = Objects.requireNonNull(dataSourceCache.getDataSourceInfo(dsId), "unknown dsId:" + dsId);
        DataSource dataSource = dataSourceInfo.getDataSource();
        Connection innerConnection = dataSource.getConnection();
        fenseConnection.setDelegate(innerConnection);
        fenseConnection.setEngine(dataSourceInfo.getEngine());
    }

    @SneakyThrows
    @Override
    public StatementHandle createStatement(ConnectionHandle ch) {
        Connection conn = Objects.requireNonNull(getConnection(ch.id), String.format("error while create connection:%s", ch.id));
        FenseConnection fenseConnection = (FenseConnection)conn;

        String schema = StringUtils.firstNonBlank(fenseConnection.getSchema(), fenseConnection.getInfo("current_database"));
        Preconditions.checkArgument(StringUtils.isNotEmpty(schema), "schema can't be empty!");
        fenseConnection.setSchema(schema);

        Statement statement = executor.createStatement(fenseConnection, schema, fetchSize);
        final int id = statementIdGenerator.getAndIncrement();
        statementCache.put(id, new StatementInfo(statement));
        log.trace("created statement {}", id);
        return new StatementHandle(ch.id, id, null);
    }

    @Override
    public ExecuteResult prepareAndExecute(StatementHandle h, String sql, long maxRowCount,
                                           int maxRowsInFirstFrame, PrepareCallback callback) throws NoSuchStatementException {
        int stmtId = h.id;
        String connId = h.connectionId;
        final StatementInfo info = getStatementCache().getIfPresent(stmtId);
        Preconditions.checkNotNull(info, String.format("statement cannot be null! shId: %s, connectionId: %s ", stmtId, connId));
        Statement statement = (Statement) ReflectUtils.getFieldValue(info, "statement");
        Preconditions.checkNotNull(sql, "sql required.");
        log.trace("prepareAndExecute: {}, sql: {}", h, sql.replaceAll("\\s+", " "));
        long startMil = System.currentTimeMillis();
        long endMil = 0;
        FenseConnection connection = null;
        String exception = null;
        User user = null;
        JSONObject ext = new JSONObject();
        try {
            Connection conn = Objects.requireNonNull(getConnection(connId), String.format("get connection failed:%s", connId));
            connection = (FenseConnection)conn;

            String schema = connection.getSchema();
            int dsId = connection.getDsId();
            String username = Objects.requireNonNull(connection.getInfo("user"), "login required.");

            DataSourceInfo dataSourceInfo = dataSourceCache.getDataSourceInfo(dsId);
            Engine engine = dataSourceInfo.getEngine();

            List<DbTable> dbTables = sqlParser.parseDbTable(dataSourceInfo.getEngine(), schema, sql);
            user = loginService.getUser(username);
            privilegeChecker.check(user, engine, dsId, dbTables);

            // Make sure that we limit the number of rows for the query
            setMaxRows(statement, maxRowCount);

            QueryTask task = QueryTask.builder().dsId(dsId).engine(engine).schema(schema).sql(sql).statement(statement)
                    .user(username).build();
            long cost = executor.execute(task);
            info.setResultSet(statement.getResultSet());

            final List<MetaResultSet> resultSets = new ArrayList<>();
            if (null == info.getResultSet()) {
                // Create a special result set that just carries update count
                long updateCount = AvaticaUtils.getLargeUpdateCount(statement);
                if (updateCount == -1) {
                    updateCount = 0;
                }
                resultSets.add(FenseResultSet.count(connId, stmtId, updateCount));
            } else {
                resultSets.add(FenseResultSet.create(connId, stmtId, info.getResultSet(), fetchSize, maxRowsInFirstFrame, engine));
            }
            return new ExecuteResult(resultSets);
        } catch (Exception e) {
            throw new RuntimeException(exception, e);
        } finally {

        }
    }

//    @SneakyThrows
//    @Override
//    public Map<String, String> getDataSource(ConnectionHandle ch, String dsKey) {
//        String connId = ch.id;
//        Connection conn = Objects.requireNonNull(getConnection(connId), String.format("get connection failed:%s", connId));
//        FenseConnection fenseConnection = (FenseConnection)conn;
//        String dsId = fenseConnection.getDsId();
//        DataSourceInfo dataSourceInfo = Objects.requireNonNull(dataSourceCache.getDataSourceInfo(dsId), "unknown dsId:" + dsId);
//        Properties properties = dataSourceInfo.getProperties();
//        Preconditions.checkArgument(properties != null && !properties.isEmpty(), "config empty! dsId:" + dsId);
//        Map<String, String> map = new HashMap<>();
//        for (Object key : properties.keySet()) {
//            map.put((String) key, (String) properties.get(key));
//        }
//        return map;
//    }
//
//    @SneakyThrows
//    @Override
//    public List<String> getQueryLog(StatementHandle h, boolean incremental, int fetchSize) {
//        int stmtId = h.id;
//        String connId = h.connectionId;
//        final StatementInfo info = getStatementCache().getIfPresent(stmtId);
//        Preconditions.checkNotNull(info, String.format("statement cannot be null! shId: %s, connectionId: %s ", stmtId, connId));
//        Statement statement = (Statement) ReflectUtils.getFieldValue(info, "statement");
//        if (statement.isClosed() || !(statement instanceof HiveStatement)) {
//            return Collections.emptyList();
//        }
//        List<String> logs = Collections.emptyList();
//        try {
//            HiveStatement hiveStatement = (HiveStatement)statement;
//            logs = hiveStatement.getQueryLog(incremental, fetchSize);
//        } catch (ClosedOrCancelledStatementException e) {
//            log.error("{}, {}", stmtId, e.getMessage());
//        }
//        return logs;
//    }

    @Override
    public void close() {
        executor.close();
    }
}
