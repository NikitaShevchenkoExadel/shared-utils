package com.starexchangealliance.shared.utils.tests;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SQLReader {

    private DataSource dataSource;

    public SQLReader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Table> read(List<String> list) throws Exception {
        try (Connection c = dataSource.getConnection()) {
            return list.stream().map(s -> dump(c, s)).collect(Collectors.toList());
        }
    }

    private Table dump(Connection c, String sql) {
        try {
            try (Statement statement = c.createStatement()) {
                try (ResultSet q = statement.executeQuery(sql)) {
                    Table table = new Table(sql.replaceAll("\n", "").
                            replaceAll(" +", " ").trim());

                    ResultSetMetaData metadata = q.getMetaData();
                    while (q.next()) {
                        table.addRow(readRow(q, metadata));
                    }
                    return table;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> readRow(ResultSet rs, ResultSetMetaData metadata) throws Exception {
        Map<String, String> map = new LinkedHashMap<>();

        int columnCount = metadata.getColumnCount();
        for (int index = 1; index <= columnCount; index++) {
            String key = metadata.getColumnLabel(index);
            String value = rs.getString(index);
            map.put(key, value);
        }

        return map;
    }
}
