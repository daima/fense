package com.cxy7.data.fense.jdbc;

import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.AvaticaFactory;
import org.apache.calcite.avatica.UnregisteredDriver;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * @Author: XiaoYu
 * @Date: 2021/9/27 23:25
 */
@Slf4j
public class FenseConnection extends AvaticaConnection {
    public static final String DS_ID = "dsId";
    private static Set<String> notExtProperties = new HashSet<>(30);
    static {
        String[] notExt = {"user", "password", "client_ip", "os_user", "client_hostname", "client_name",
                "client_version", "acceptedResultsNum"};
        notExtProperties.addAll(Arrays.asList(notExt));
    }

    private Connection delegate;
    private Engine engine;

    protected FenseConnection(UnregisteredDriver driver, AvaticaFactory factory, String url, Properties info) {
        super(driver, factory, url, info);
    }

    public String getInfo(String name){
        return this.info.getProperty(name);
    }

    public Properties getAllInfo() {
        return this.info;
    }

    public JSONObject getExt() {
        JSONObject root = new JSONObject();
        for (String key : info.stringPropertyNames()) {
            if (notExtProperties.contains(key))
                continue;
            root.put(key, getInfo(key));
        }
        return root;
    }

    @SneakyThrows
    public int getDsId() {
        String dsId = StringUtils.firstNonBlank(getCatalog(), getInfo(DS_ID));
        return Integer.parseInt(dsId);
    }

    @SneakyThrows
    public void setDelegate(Connection delegate) {
        if (Objects.nonNull(delegate)) {
            log.debug("get connection: {}, {}", id, delegate);
        }
        Connection oldConnection = this.delegate;
        if (Objects.nonNull(oldConnection)) {
            log.debug("recyle connection {}, {}", id, oldConnection);
            oldConnection.close();
        }
        this.delegate = delegate;
    }

    public Connection getDelegate() {
        return delegate;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void close() throws SQLException {
        setDelegate(null);
        super.close();
    }
}
