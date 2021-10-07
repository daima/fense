package com.cxy7.data.fense.jdbc;

import org.apache.calcite.avatica.*;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;
import java.util.TimeZone;

/**
 * @Author: XiaoYu
 * @Date: 2021/9/27 23:15
 */
public class FenseJdbc41Factory implements AvaticaFactory {
    private final int major;
    private final int minor;

    /** Creates a JDBC factory. */
    // CHECKSTYLE: stop RedundantModifierCheck
    public FenseJdbc41Factory() {
        this(4, 1);
    }
    // CHECKSTYLE: resume RedundantModifierCheck


    /** Creates a JDBC factory with given major/minor version number. */
    protected FenseJdbc41Factory(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public int getJdbcMajorVersion() {
        return major;
    }

    public int getJdbcMinorVersion() {
        return minor;
    }

    @Override
    public AvaticaConnection newConnection(UnregisteredDriver unregisteredDriver, AvaticaFactory avaticaFactory, String url, Properties info) throws SQLException {
        return new FenseConnection(unregisteredDriver, avaticaFactory, url, info);
    }

    @Override
    public AvaticaStatement newStatement(AvaticaConnection connection,
                                         Meta.StatementHandle h,
                                         int resultSetType,
                                         int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        return new FenseStatement(connection,
                h,
                resultSetType, resultSetConcurrency,
                resultSetHoldability);
    }

    @Override
    public AvaticaPreparedStatement newPreparedStatement(
            AvaticaConnection connection, Meta.StatementHandle h,
            Meta.Signature signature, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        return new FenseJdbc41PreparedStatement(
                (FenseConnection) connection, h,
                signature, resultSetType,
                resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public AvaticaResultSet newResultSet(AvaticaStatement statement,
                                         QueryState state, Meta.Signature signature, TimeZone timeZone, Meta.Frame firstFrame)
            throws SQLException {
        final ResultSetMetaData metaData =
                newResultSetMetaData(statement, signature);
        return new AvaticaResultSet(statement, state, signature, metaData, timeZone,
                firstFrame);
    }

    @Override
    public AvaticaSpecificDatabaseMetaData newDatabaseMetaData(AvaticaConnection connection) {
        return new AvaticaJdbc41DatabaseMetaData(connection);
    }

    @Override
    public ResultSetMetaData newResultSetMetaData(AvaticaStatement statement, Meta.Signature signature) throws SQLException {
        return new AvaticaResultSetMetaData(statement, null, signature);
    }

    /**
     * Implementation of PreparedStatement for JDBC 4.1.
     */
    private static class FenseJdbc41PreparedStatement
            extends AvaticaPreparedStatement {
        FenseJdbc41PreparedStatement(FenseConnection connection,
                                     Meta.StatementHandle h, Meta.Signature signature, int resultSetType,
                                     int resultSetConcurrency, int resultSetHoldability)
                throws SQLException {
            super(connection, h, signature, resultSetType, resultSetConcurrency,
                    resultSetHoldability);
        }
    }

    /**
     * Implementation of DatabaseMetaData for JDBC 4.1.
     */
    private static class AvaticaJdbc41DatabaseMetaData
            extends AvaticaDatabaseMetaData {
        AvaticaJdbc41DatabaseMetaData(AvaticaConnection connection) {
            super(connection);
        }
    }
}