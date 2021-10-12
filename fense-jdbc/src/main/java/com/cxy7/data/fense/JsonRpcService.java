package com.cxy7.data.fense;

import org.apache.calcite.avatica.remote.AvaticaHttpClient;
import org.apache.calcite.avatica.remote.JsonService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/12 23:29
 */
public class JsonRpcService extends JsonService {
    private final AvaticaHttpClient client;

    public JsonRpcService(AvaticaHttpClient client) {
        this.client = client;
    }

    @Override
    public String apply(String request) {
        byte[] response = client.send(request.getBytes(StandardCharsets.UTF_8));
        return new String(response, StandardCharsets.UTF_8);
    }

    @Override
    public ResultSetResponse apply(FunctionsRequest request) {
        try {
            return finagle(decode(apply(encode(request)), ResultSetResponse.class));
        } catch (IOException e) {
            throw handle(e);
        }
    }

    @Override
    public DataSourceResponse apply(DataSourceRequest request) {
        try {
            return finagle(decode(apply(encode(request)), DataSourceResponse.class));
        } catch (IOException e) {
            throw handle(e);
        }
    }

    @Override
    public QueryLogResponse apply(QueryLogRequest request) {
        try {
            return finagle(decode(apply(encode(request)), QueryLogResponse.class));
        } catch (IOException e) {
            throw handle(e);
        }
    }
}
