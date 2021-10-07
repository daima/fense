package com.cxy7.data.fense.server.jdbc;

import org.apache.calcite.avatica.Meta;
import org.apache.calcite.avatica.remote.LocalService;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/6 12:35
 */
public class RpcService extends LocalService {
    public RpcService(Meta meta) {
        super(meta);
    }

//    @Override
//    public ResultSetResponse apply(FunctionsRequest request) {
//        final Meta.ConnectionHandle ch =
//                new Meta.ConnectionHandle(request.connectionId);
//        final Meta.MetaResultSet resultSet =
//                getMeta().getFunctions(ch,
//                        request.catalog,
//                        Meta.Pat.of(request.schemaPattern),
//                        Meta.Pat.of(request.functionNamePattern));
//        return toResponse(resultSet);
//    }
//
//    @Override
//    public DataSourceResponse apply(DataSourceRequest request) {
//        final Meta.ConnectionHandle ch =
//                new Meta.ConnectionHandle(request.connectionId);
//        final Map<String, String> props =
//                getMeta().getDataSource(ch,
//                        request.dsKey);
//        return new DataSourceResponse(props, getServerLevelRpcMetadata());
//    }
//
//    @Override
//    public QueryLogResponse apply(QueryLogRequest request) {
//        final Meta.StatementHandle sh =
//                new Meta.StatementHandle(request.connectionId, request.statementId, null);
//        final List<String> logs =
//                getMeta().getQueryLog(sh, request.incremental, request.fetchSize);
//        return new QueryLogResponse(logs, getServerLevelRpcMetadata());
//    }
}
