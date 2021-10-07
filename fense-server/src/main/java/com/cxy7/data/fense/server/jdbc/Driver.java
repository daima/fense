package com.cxy7.data.fense.server.jdbc;

import com.cxy7.data.fense.jdbc.FenseJdbc41Factory;
import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.DriverVersion;
import org.apache.calcite.avatica.Meta;
import org.apache.calcite.avatica.UnregisteredDriver;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/20 17:47
 */
public class Driver extends UnregisteredDriver {
    public static final String CONNECT_STRING_PREFIX = "jdbc:fense:server:";

    static {
        new Driver().register();
    }

    @Override
    protected String getFactoryClassName(JdbcVersion jdbcVersion) {
        switch (jdbcVersion) {
            case JDBC_30:
            case JDBC_40:
                throw new IllegalArgumentException("JDBC version not supported: " + jdbcVersion);
            case JDBC_41:
            default:
                return FenseJdbc41Factory.class.getName();
        }
    }

    @Override
    protected DriverVersion createDriverVersion() {
        return DriverVersion.load(
                Driver.class,
                "version.properties",
                "Avatica Remote JDBC Driver",
                "unknown version",
                "Avatica",
                "unknown version");
    }

    @Override
    protected String getConnectStringPrefix() {
        return CONNECT_STRING_PREFIX;
    }

    @Override
    public Meta createMeta(AvaticaConnection avaticaConnection) {
        return new FenseMetaImpl(avaticaConnection);
    }
}
