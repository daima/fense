package com.cxy7.data.fense;

import org.apache.calcite.avatica.*;
import org.apache.calcite.avatica.remote.Service;
import org.apache.calcite.avatica.remote.TypedValue;

import java.sql.SQLException;
import java.util.*;

/**
 * @author: XiaoYu
 * @date: 2021/3/13 5:05 下午
 */
public class ClientMeta extends MetaImpl {
    private final Service service;
    final Map<String, ConnectionPropertiesImpl> propsMap = new HashMap<>();
    private Map<DatabaseProperty, Object> databaseProperties;

    public ClientMeta(AvaticaConnection connection, Service service) {
        super(connection);
        this.service = service;
    }
    @Override public void openConnection(final ConnectionHandle ch, final Map<String, String> info) {
        connection.invokeWithRetries(
                () -> {
                    propsMap.get(ch.id).setDirty(true);
                    final Service.OpenConnectionResponse response =
                            service.apply(new Service.OpenConnectionRequest(ch.id, info));
                    return null;
                });
    }

    @Override public ConnectionProperties connectionSync(final ConnectionHandle ch,
                                                         final ConnectionProperties connProps) {
        return connection.invokeWithRetries(
                () -> {
                    ConnectionPropertiesImpl localProps = propsMap.get(ch.id);
                    if (localProps == null) {
                        localProps = new ConnectionPropertiesImpl();
                        localProps.setDirty(true);
                        propsMap.put(ch.id, localProps);
                    }

                    // Only make an RPC if necessary. RPC is necessary when we have local changes that need
                    // flushed to the server (be sure to introduce any new changes from connProps before
                    // checking AND when connProps.isEmpty() (meaning, this was a request for a value, not
                    // overriding a value). Otherwise, accumulate the change locally and return immediately.
                    if (localProps.merge(connProps).isDirty() && connProps.isEmpty()) {
                        final Service.ConnectionSyncResponse response = service.apply(
                                new Service.ConnectionSyncRequest(ch.id, localProps));
                        propsMap.put(ch.id, (ConnectionPropertiesImpl) response.connProps);
                        return response.connProps;
                    } else {
                        return localProps;
                    }
                });
    }

    @Override public StatementHandle createStatement(final ConnectionHandle ch) {
        return connection.invokeWithRetries(
                () -> {
                    // sync connection state if necessary
                    connectionSync(ch, new ConnectionPropertiesImpl());
                    final Service.CreateStatementResponse response =
                            service.apply(new Service.CreateStatementRequest(ch.id));
                    return new StatementHandle(response.connectionId, response.statementId, null);
                });
    }

    @Override
    public StatementHandle prepare(ConnectionHandle ch, String sql, long maxRowCount) {
        return connection.invokeWithRetries(
                () -> {
                    connectionSync(ch,
                            new ConnectionPropertiesImpl()); // sync connection state if necessary
                    final Service.PrepareResponse response = service.apply(
                            new Service.PrepareRequest(ch.id, sql, maxRowCount));
                    return response.statement;
                });
    }

    @Override
    public ExecuteResult prepareAndExecute(StatementHandle h, String sql, long maxRowCount, PrepareCallback callback) throws NoSuchStatementException {
        // The old semantics were that maxRowCount was also treated as the maximum number of
        // elements in the first Frame of results. A value of -1 would also preserve this, but an
        // explicit (positive) number is easier to follow, IMO.
        return prepareAndExecute(h, sql, maxRowCount, AvaticaUtils.toSaturatedInt(maxRowCount),
                callback);
    }

    @Override
    public ExecuteResult prepareAndExecute(StatementHandle h, String sql, long maxRowCount, int maxRowsInFirstFrame, PrepareCallback callback) throws NoSuchStatementException {
        try {
            return connection.invokeWithRetries(
                    () -> {
                        // sync connection state if necessary
                        connectionSync(new ConnectionHandle(h.connectionId), new ConnectionPropertiesImpl());
                        final Service.ExecuteResponse response;
                        try {
                            synchronized (callback.getMonitor()) {
                                callback.clear();
                                response = service.apply(
                                        new Service.PrepareAndExecuteRequest(h.connectionId,
                                                h.id, sql, maxRowCount));
                                if (response.missingStatement) {
                                    throw new RuntimeException(new NoSuchStatementException(h));
                                }
                                if (response.results.size() > 0) {
                                    final Service.ResultSetResponse result = response.results.get(0);
                                    callback.assign(result.signature, result.firstFrame,
                                            result.updateCount);
                                }
                            }
                            callback.execute();
                            List<MetaResultSet> metaResultSets = new ArrayList<>();
                            for (Service.ResultSetResponse result : response.results) {
                                metaResultSets.add(toResultSet(null, result));
                            }
                            return new ExecuteResult(metaResultSets);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof NoSuchStatementException) {
                throw (NoSuchStatementException) cause;
            }
            throw e;
        }
    }

    private MetaResultSet toResultSet(Class clazz,
                                      Service.ResultSetResponse response) {
        if (response.updateCount != -1) {
            return MetaResultSet.count(response.connectionId, response.statementId,
                    response.updateCount);
        }
        Signature signature0 = response.signature;
        if (signature0 == null) {
            final List<ColumnMetaData> columns =
                    clazz == null
                            ? Collections.<ColumnMetaData>emptyList()
                            : fieldMetaData(clazz).columns;
            signature0 = Signature.create(columns,
                    "?", Collections.<AvaticaParameter>emptyList(), CursorFactory.ARRAY,
                    StatementType.SELECT);
        }
        return MetaResultSet.create(response.connectionId, response.statementId,
                response.ownStatement, signature0, response.firstFrame);
    }

    @Override
    public ExecuteBatchResult prepareAndExecuteBatch(StatementHandle h, List<String> sqlCommands) throws NoSuchStatementException {
        return connection.invokeWithRetries(() -> {
            Service.ExecuteBatchResponse response =
                    service.apply(
                            new Service.PrepareAndExecuteBatchRequest(h.connectionId, h.id, sqlCommands));
            return new ExecuteBatchResult(response.updateCounts);
        });
    }

    @Override
    public ExecuteBatchResult executeBatch(StatementHandle h, List<List<TypedValue>> parameterValues) throws NoSuchStatementException {
        return connection.invokeWithRetries(() -> {
            Service.ExecuteBatchResponse response =
                    service.apply(new Service.ExecuteBatchRequest(h.connectionId, h.id, parameterValues));
            return new ExecuteBatchResult(response.updateCounts);
        });
    }

    @Override
    public Frame fetch(StatementHandle h, long offset, int fetchMaxRowCount) throws NoSuchStatementException, MissingResultsException {
        try {
            return connection.invokeWithRetries(
                    () -> {
                        final Service.FetchResponse response =
                                service.apply(
                                        new Service.FetchRequest(h.connectionId, h.id, offset, fetchMaxRowCount));
                        if (response.missingStatement) {
                            throw new RuntimeException(new NoSuchStatementException(h));
                        }
                        if (response.missingResults) {
                            throw new RuntimeException(new MissingResultsException(h));
                        }
                        return response.frame;
                    });
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof NoSuchStatementException) {
                throw (NoSuchStatementException) cause;
            } else if (cause instanceof MissingResultsException) {
                throw (MissingResultsException) cause;
            }
            throw e;
        }
    }

    @Override
    public MetaResultSet getFunctions(ConnectionHandle ch, String catalog, Pat schemaPattern, Pat functionNamePattern) {
        return connection.invokeWithRetries(
                () -> {
                    final Service.ResultSetResponse response =
                            service.apply(
                                    new Service.FunctionsRequest(ch.id, catalog, schemaPattern.s,
                                            functionNamePattern.s));
                    return toResultSet(MetaTable.class, response);
                });
    }

    @Override
    public Map<String, String> getDataSource(ConnectionHandle ch, String dsKey) {
        Map<String, String> map = connection.invokeWithRetries(
                () -> {
                    final Service.DataSourceResponse response =
                            service.apply(
                                    new Service.DataSourceRequest(ch.id, dsKey));
                    return response.props;
                });
        return map;
    }

    @Override
    public List<String> getQueryLog(StatementHandle h, boolean incremental, int fetchSize) {
        return connection.invokeWithRetries(
                () -> {
                    final Service.QueryLogResponse response =
                            service.apply(
                                    new Service.QueryLogRequest(h.connectionId, h.id, incremental, fetchSize));
                    return response.logs;
                });
    }

    @Override
    public ExecuteResult execute(StatementHandle h, List<TypedValue> parameterValues, long maxRowCount) throws NoSuchStatementException {
        return execute(h, parameterValues, AvaticaUtils.toSaturatedInt(maxRowCount));
    }

    @Override
    public ExecuteResult execute(StatementHandle h, List<TypedValue> parameterValues, int maxRowsInFirstFrame) throws NoSuchStatementException {
        try {
            return connection.invokeWithRetries(
                    () -> {
                        final Service.ExecuteResponse response = service.apply(
                                new Service.ExecuteRequest(h, parameterValues, maxRowsInFirstFrame));

                        if (response.missingStatement) {
                            throw new RuntimeException(new NoSuchStatementException(h));
                        }

                        List<MetaResultSet> metaResultSets = new ArrayList<>();
                        for (Service.ResultSetResponse result : response.results) {
                            metaResultSets.add(toResultSet(null, result));
                        }

                        return new ExecuteResult(metaResultSets);
                    });
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof NoSuchStatementException) {
                throw (NoSuchStatementException) cause;
            }
            throw e;
        }
    }

    @Override public MetaResultSet getCatalogs(final ConnectionHandle ch) {
        return connection.invokeWithRetries(
                () -> {
                    final Service.ResultSetResponse response =
                            service.apply(new Service.CatalogsRequest(ch.id));
                    return toResultSet(MetaCatalog.class, response);
                });
    }

    @Override public MetaResultSet getSchemas(final ConnectionHandle ch, final String catalog,
                                              final Pat schemaPattern) {
        return connection.invokeWithRetries(
                () -> {
                    final Service.ResultSetResponse response =
                            service.apply(
                                    new Service.SchemasRequest(ch.id, catalog, schemaPattern.s));
                    return toResultSet(MetaSchema.class, response);
                });
    }

    @Override public MetaResultSet getTables(final ConnectionHandle ch, final String catalog,
                                             final Pat schemaPattern, final Pat tableNamePattern, final List<String> typeList) {
        return connection.invokeWithRetries(
                () -> {
                    final Service.ResultSetResponse response =
                            service.apply(
                                    new Service.TablesRequest(ch.id, catalog, schemaPattern.s,
                                            tableNamePattern.s, typeList));
                    return toResultSet(MetaTable.class, response);
                });
    }

    @Override public MetaResultSet getTableTypes(final ConnectionHandle ch) {
        return connection.invokeWithRetries(
                () -> {
                    final Service.ResultSetResponse response =
                            service.apply(new Service.TableTypesRequest(ch.id));
                    return toResultSet(MetaTableType.class, response);
                });
    }

    @Override public MetaResultSet getTypeInfo(final ConnectionHandle ch) {
        return connection.invokeWithRetries(
                () -> {
                    final Service.ResultSetResponse response =
                            service.apply(new Service.TypeInfoRequest(ch.id));
                    return toResultSet(MetaTypeInfo.class, response);
                });
    }

    @Override public MetaResultSet getColumns(final ConnectionHandle ch, final String catalog,
                                              final Pat schemaPattern, final Pat tableNamePattern, final Pat columnNamePattern) {
        return connection.invokeWithRetries(
                () -> {
                    final Service.ResultSetResponse response =
                            service.apply(
                                    new Service.ColumnsRequest(ch.id, catalog, schemaPattern.s,
                                            tableNamePattern.s, columnNamePattern.s));
                    return toResultSet(MetaColumn.class, response);
                });
    }

    @Override
    public void closeStatement(StatementHandle h) {
        connection.invokeWithRetries(
                () -> {
                    final Service.CloseStatementResponse response =
                            service.apply(
                                    new Service.CloseStatementRequest(h.connectionId, h.id));
                    return null;
                });
    }

    @Override public void closeConnection(final ConnectionHandle ch) {
        connection.invokeWithRetries(
                () -> {
                    final Service.CloseConnectionResponse response =
                            service.apply(new Service.CloseConnectionRequest(ch.id));
                    propsMap.remove(ch.id);
                    return null;
                });
    }

    @Override
    public boolean syncResults(StatementHandle sh, QueryState state, long offset) throws NoSuchStatementException {
        try {
            return connection.invokeWithRetries(
                    () -> {
                        final Service.SyncResultsResponse response =
                                service.apply(
                                        new Service.SyncResultsRequest(sh.connectionId, sh.id, state, offset));
                        if (response.missingStatement) {
                            throw new RuntimeException(new NoSuchStatementException(sh));
                        }
                        return response.moreResults;
                    });
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof NoSuchStatementException) {
                throw (NoSuchStatementException) cause;
            }
            throw e;
        }
    }

    @Override
    public void commit(ConnectionHandle ch) {
        connection.invokeWithRetries(() -> {
            final Service.CommitResponse response =
                    service.apply(new Service.CommitRequest(ch.id));
            return null;
        });
    }

    @Override
    public void rollback(ConnectionHandle ch) {
        connection.invokeWithRetries(() -> {
            final Service.RollbackResponse response =
                    service.apply(new Service.RollbackRequest(ch.id));
            return null;
        });
    }
}
