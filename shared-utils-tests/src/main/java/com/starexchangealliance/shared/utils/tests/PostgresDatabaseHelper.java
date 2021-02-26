package com.starexchangealliance.shared.utils.tests;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.starexchangealliance.shared.utils.tests.SQLSource.SQL_DELIMITER;

@Slf4j
public class PostgresDatabaseHelper {

    private static final String FLYWAY_MIGRATION_TABLE = "flyway_schema_history";

    private PostgresDatabaseHelper() {
        // hidden
    }

    public static void apply(DataSource dataSource, SQLSource source) throws Exception {
        apply(dataSource, Collections.singletonList(source));
    }

    public static void apply(DataSource dataSource, List<SQLSource> sources) throws Exception {
        try (Connection c = dataSource.getConnection()) {
            for (SQLSource each : sources) {
                apply(c, each);
            }
        }
    }

    public static void apply(Connection c, SQLSource queries) {
        String runningSql = null;
        try {
            QueryRunner queryRunner = new QueryRunner();
            for (String sql : queries.getSQLs()) {
                runningSql = sql;
                queryRunner.update(c, runningSql);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to apply queries " + queries + ",\n line:" +
                    runningSql + " , message: " + e.getMessage(), e);
        }
    }

    public static ListSource dropDatabaseQuery(DataSource dataSource) throws SQLException {
        List<String> o = new ArrayList<>();
        for (String each : tables(dataSource, true)) {
            if (FLYWAY_MIGRATION_TABLE.equalsIgnoreCase(each)) {
                o.add("DROP TABLE IF EXISTS " + each + " CASCADE");
            } else {
                o.add("ALTER TABLE " + each + " DISABLE TRIGGER ALL");
                o.add("DROP TABLE IF EXISTS " + each + " CASCADE");
                o.add("DROP SEQUENCE IF EXISTS " + each + "_id_seq CASCADE");
            }
        }

        for (String each : enums(dataSource)) {
            o.add("DROP TYPE " + each);
        }

        return new ListSource(o);
    }

    public static ListSource clearDatabaseQuery(DataSource dataSource) throws SQLException {
        List<String> o = new ArrayList<>();
        for (String each : tables(dataSource, false)) {
            o.add("ALTER TABLE " + each + " DISABLE TRIGGER ALL");
            o.add("TRUNCATE TABLE " + each + " CASCADE");
            o.add("ALTER TABLE " + each + " ENABLE TRIGGER ALL");
        }
        return new ListSource(o);
    }

    public static List<String> tables(DataSource ds, boolean addSchemaHistory) throws SQLException {
        String filter = addSchemaHistory ? "" : " AND tablename != '" + FLYWAY_MIGRATION_TABLE + "'";
        String sql = String.format("select tablename from pg_tables where schemaname='public' %s" + SQL_DELIMITER, filter);
        QueryRunner runner = new QueryRunner(ds);
        return runner.query(sql, new ColumnListHandler<>(1));
    }

    public static List<String> enums(DataSource ds) throws SQLException {
        String sql = "SELECT DISTINCT pg_type.typname AS enumtype FROM pg_type JOIN pg_enum ON pg_enum.enumtypid = pg_type.oid";
        QueryRunner runner = new QueryRunner(ds);
        return runner.query(sql, new ColumnListHandler<>(1));
    }

}
