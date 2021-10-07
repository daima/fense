package com.cxy7.data.fense.server.jdbc;

import com.cxy7.data.fense.jdbc.Engine;
import org.apache.calcite.avatica.AvaticaParameter;
import org.apache.calcite.avatica.ColumnMetaData;
import org.apache.calcite.avatica.Meta;
import org.apache.calcite.avatica.SqlType;

import java.sql.ParameterMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/5 20:04
 */
public class SignatureFactory {
    protected static Meta.Signature signature(ResultSetMetaData metaData,
                                              ParameterMetaData parameterMetaData, String sql,
                                              Meta.StatementType statementType, Engine engine) throws SQLException {
        final Meta.CursorFactory cf = Meta.CursorFactory.LIST;
        return new Meta.Signature(columns(metaData, engine), sql, parameters(parameterMetaData),
                null, cf, statementType);
    }

    protected static Meta.Signature signature(ResultSetMetaData metaData, Engine engine)
            throws SQLException {
        return signature(metaData, null, null, null, engine);
    }

    /**
     * Converts from JDBC metadata to Avatica columns.
     */
    protected static List<ColumnMetaData>
    columns(ResultSetMetaData metaData, Engine engine) throws SQLException {
        if (metaData == null) {
            return Collections.emptyList();
        }
        final List<ColumnMetaData> columns = new ArrayList<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            final SqlType sqlType = SqlType.valueOf(metaData.getColumnType(i));
            final ColumnMetaData.Rep rep = ColumnMetaData.Rep.of(sqlType.internal);
            final ColumnMetaData.AvaticaType t;
            if (sqlType == SqlType.ARRAY || sqlType == SqlType.STRUCT || sqlType == SqlType.MULTISET) {
                ColumnMetaData.AvaticaType arrayValueType = ColumnMetaData.scalar(Types.JAVA_OBJECT,
                        metaData.getColumnTypeName(i), ColumnMetaData.Rep.OBJECT);
                t = ColumnMetaData.array(arrayValueType, metaData.getColumnTypeName(i), rep);
            } else {
                t = ColumnMetaData.scalar(metaData.getColumnType(i), metaData.getColumnTypeName(i), rep);
            }
            boolean isSearchable = false;
            boolean isSigned = false;
            String schemaName = "";
            String tableName = "";
            String catalogName = "";
            boolean isWritable = false;
            boolean isDefinitelyWritable = false;
            if (engine != Engine.HIVE) {
                isSearchable = metaData.isSearchable(i);
                isSigned = metaData.isSigned(i);
                schemaName = metaData.getSchemaName(i);
                tableName = metaData.getTableName(i);
                catalogName = metaData.getCatalogName(i);
                isWritable = metaData.isWritable(i);
                isDefinitelyWritable = metaData.isDefinitelyWritable(i);
            }
            ColumnMetaData md =
                    new ColumnMetaData(i - 1, metaData.isAutoIncrement(i),
                            metaData.isCaseSensitive(i), isSearchable,
                            metaData.isCurrency(i), metaData.isNullable(i),
                            isSigned, metaData.getColumnDisplaySize(i),
                            metaData.getColumnLabel(i), metaData.getColumnName(i),
                            schemaName, metaData.getPrecision(i),
                            metaData.getScale(i), tableName,
                            catalogName, t, metaData.isReadOnly(i),
                            isWritable, isDefinitelyWritable,
                            metaData.getColumnClassName(i));
            columns.add(md);
        }
        return columns;
    }

    /**
     * Converts from JDBC metadata to Avatica parameters
     */
    protected static List<AvaticaParameter> parameters(ParameterMetaData metaData)
            throws SQLException {
        if (metaData == null) {
            return Collections.emptyList();
        }
        final List<AvaticaParameter> params = new ArrayList<>();
        for (int i = 1; i <= metaData.getParameterCount(); i++) {
            params.add(
                    new AvaticaParameter(metaData.isSigned(i), metaData.getPrecision(i),
                            metaData.getScale(i), metaData.getParameterType(i),
                            metaData.getParameterTypeName(i),
                            metaData.getParameterClassName(i), "?" + i));
        }
        return params;
    }
}
