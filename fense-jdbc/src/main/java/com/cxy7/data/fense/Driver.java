package com.cxy7.data.fense;

import com.cxy7.data.fense.jdbc.FenseJdbc41Factory;
import com.cxy7.data.fense.jdbc.utils.Version;
import org.apache.calcite.avatica.*;
import org.apache.calcite.avatica.remote.*;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.security.Constraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/12 23:53
 */
public class Driver extends UnregisteredDriver {
    private static Logger LOG = LoggerFactory.getLogger(Driver.class);
    public static final String KEY_USERNAME = "user";
    public static final String KEY_PASSWORD = "password";
    private static final String SPLITER = "&";
    private static final String EQ = "=";
    public static final String CONNECT_STRING_PREFIX = "jdbc:fense://";

    static {
        new Driver().register();
    }

    @Override
    protected DriverVersion createDriverVersion() {
        return new DriverVersion(Version.NAME, Version.VERSION, Version.PRODUCT_NAME, Version.VERSION,
                Version.JDBC_COMPLIANT, Version.majorVersion(), Version.minorVersion(), Version.majorVersion(),
                Version.minorVersion());
    }

    @Override
    protected String getConnectStringPrefix() {
        return CONNECT_STRING_PREFIX;
    }

    @Override
    protected Collection<ConnectionProperty> getConnectionProperties() {
        final List<ConnectionProperty> list = new ArrayList<>();
        Collections.addAll(list, BuiltInConnectionProperty.values());
        Collections.addAll(list, AvaticaRemoteConnectionProperty.values());
        return list;
    }

    @Override
    protected String getFactoryClassName(JdbcVersion jdbcVersion) {
        switch(jdbcVersion) {
            case JDBC_30:
            case JDBC_40:
                throw new IllegalArgumentException("JDBC version not supported: " + jdbcVersion);
            case JDBC_41:
            default:
                return FenseJdbc41Factory.class.getName();
        }
    }

    @Override
    public Meta createMeta(AvaticaConnection avaticaConnection) {
        final ConnectionConfig config = avaticaConnection.config();

        // Create a single Service and set it on the Connection instance
        final Service service = createService(avaticaConnection, config);
        avaticaConnection.setService(service);
        return new ClientMeta(avaticaConnection, service);
    }

    private Service createService(AvaticaConnection avaticaConnection, ConnectionConfig config) {
        final Service.Factory metaFactory = config.factory();
        final Service service;
        if (metaFactory != null) {
            service = metaFactory.create(avaticaConnection);
        } else if (config.url() != null) {
            final AvaticaHttpClient httpClient = getHttpClient(avaticaConnection, config);
            final Serialization serializationType = getSerialization(config);
            switch (serializationType) {
                case JSON:
                    service = new JsonRpcService(httpClient);
                    break;
                case PROTOBUF:
                    service = new RemoteProtobufService(httpClient, new ProtobufTranslationImpl());
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled serialization type: " + serializationType);
            }
        } else {
            service = new MockJsonService(Collections.<String, String>emptyMap());
        }
        return service;
    }

    /**
     * Creates the HTTP client that communicates with the Avatica server.
     *
     * @param connection The {@link AvaticaConnection}.
     * @param config The configuration.
     * @return An {@link AvaticaHttpClient} implementation.
     */
    AvaticaHttpClient getHttpClient(AvaticaConnection connection, ConnectionConfig config) {
        URL url;
        try {
            url = new URL(config.url());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        AvaticaHttpClientFactory httpClientFactory = config.httpClientFactory();

        return httpClientFactory.getClient(url, config, connection.getKerberosConnection());
    }

    Serialization getSerialization(ConnectionConfig config) {
        final String serializationStr = config.serialization();
        Serialization serializationType = Serialization.JSON;
        if (null != serializationStr) {
            try {
                serializationType =
                        Serialization.valueOf(serializationStr.toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                // Log a warning instead of failing harshly? Intentionally no loggers available?
                throw new RuntimeException(e);
            }
        }

        return serializationType;
    }

    public static Properties parseArgs(String s, int urlSuffix, Properties props){
        s = s.substring(urlSuffix + 1);
        String[] arr = s.split(SPLITER);
        for (String pair : arr) {
            String[] kv = pair.split(EQ);
            if (kv.length != 2) {
                continue;
            }
            props.put(kv[0], kv[1]);
        }
        return props;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        AvaticaConnection conn = null;
        if (!this.acceptsURL(url)) {
            return null;
        } else {
            String prefix = this.getConnectStringPrefix();

            assert url.startsWith(prefix);

            String urlSuffix = url.substring(prefix.length());
            String serverUrl = urlSuffix.substring(0, urlSuffix.indexOf('/'));
            int paramIndex = urlSuffix.indexOf('?') != -1 ? urlSuffix.indexOf('?') : urlSuffix.length();
            String currentDataBase = urlSuffix.substring(urlSuffix.indexOf('/') + 1, paramIndex);
            if (StringUtils.isBlank(currentDataBase)) {
                LOG.error("current database can't be empty!");
                return null;
            }

            if (urlSuffix.indexOf('?') != -1) {
                parseArgs(urlSuffix, paramIndex, info);
            }

            String httpSchema = "https://";
            if (!serverUrl.contains(".com")) {
                httpSchema = "http://";
            }
            info.put("url", httpSchema + serverUrl);
            info.put("current_database", currentDataBase);

            info.put(BuiltInConnectionProperty.AVATICA_USER.camelName(), info.getProperty(KEY_USERNAME));
            info.put(BuiltInConnectionProperty.AVATICA_PASSWORD.camelName(), info.getProperty(KEY_PASSWORD));
            info.put(BuiltInConnectionProperty.SERIALIZATION.camelName(), Serialization.JSON.name());
            info.put(BuiltInConnectionProperty.AUTHENTICATION.camelName(), Constraint.__BASIC_AUTH);

            try {
                InetAddress addr = InetAddress.getLocalHost();
                String ip = addr.getHostAddress();
                info.put("client_ip", ip);
                String hostName = addr.getHostName();
                info.put("client_hostname", hostName);
                Properties props = System.getProperties();
                info.put("os_user", props.getProperty("user.name"));
                DriverVersion version = getDriverVersion();
                info.put("client_version", version.versionString);
                info.put("client_name", version.productName);
            } catch (Exception e) {
                LOG.error("connect: {}", e);
            }
            conn = this.factory.newConnection(this, this.factory, url, info);
            this.handler.onConnectionInit(conn);
        }
        if (conn == null) {
            // It's not an url for our driver
            return null;
        }
        if (info.getProperty("type","").equals("server")) {
            return conn;
        }

        Service service = conn.getService();

        // super.connect(...) should be creating a service and setting it in the AvaticaConnection
        assert null != service;
        Map<String, String> props = Service.OpenConnectionRequest.serializeProperties(info);
        service.apply(
                new Service.OpenConnectionRequest(conn.id, props));

        return conn;
    }
}
