package com.cxy7.data.fense.server.jdbc;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.clickhouse.parser.ClickhouseStatementParser;
import com.alibaba.druid.sql.dialect.hive.parser.HiveStatementParser;
import com.alibaba.druid.sql.dialect.hive.visitor.HiveSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.cxy7.data.fense.jdbc.Engine;
import com.cxy7.data.fense.server.model.DbTable;
import com.cxy7.data.fense.server.model.Mode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/4 17:34
 */
@Component
@Slf4j
public class SqlParser {

    public List<DbTable> parseDbTable(Engine engine, String schema, String sql) {
        List<DbTable> list = new ArrayList<>();
        Set<String> tableNames = new HashSet<>();
        List<SQLStatement> statements = getSQLStatements(engine, sql);
        for (SQLStatement statement : statements) {
            SchemaStatVisitor visitor = getSchemaStatVisitor(engine);
            statement.accept(visitor);
            Map<TableStat.Name, TableStat> tables = visitor.getTables();
            for (TableStat.Name name : tables.keySet()) {
                TableStat ts = tables.get(name);
                Mode mode = Objects.requireNonNull(Mode.lookup(ts.toString()), String.format("unknown rule: %s", ts));
                switch (mode) {
                    case CREATE:
                    case SELECT:
                        break;
                    default:
                        throw new UnsupportedOperationException("only create & select support.");
                }
                tableNames.add(name.getName().toLowerCase());
            }
        }
        for (String tableName : tableNames) {
            final String[] arr = tableName.split("\\.", -1);
            if (arr.length == 2) {
                list.add(DbTable.of(arr[0], arr[1]));
            } else if (arr.length == 1){
                list.add(DbTable.of(schema, arr[0]));
            }
        }

        return list;
    }

    public SchemaStatVisitor getSchemaStatVisitor(Engine engine) {
        SchemaStatVisitor visitor;
        switch (engine) {
            case HIVE:
                visitor = new HiveSchemaStatVisitor();
                break;
            case TIDB:
            case DORIS:
            case MYSQL:
                visitor = new MySqlSchemaStatVisitor();
                break;
            default:
                throw new UnsupportedOperationException(String.format("unknown engine: %s", engine));
        }
        return visitor;
    }

    public List<SQLStatement> getSQLStatements(Engine engine, String sql) {
        SQLStatementParser parser;
        switch (engine) {
            case HIVE:
                parser = new HiveStatementParser(sql);
                break;
            case CLICKHOUSE:
                parser = new ClickhouseStatementParser(sql);
                break;
            case TIDB:
            case DORIS:
            case MYSQL:
                parser = new MySqlStatementParser(sql);
                break;
            default:
                throw new UnsupportedOperationException(String.format("unknown engine: %s", engine));
        }
        return parser.parseStatementList();
    }
}
