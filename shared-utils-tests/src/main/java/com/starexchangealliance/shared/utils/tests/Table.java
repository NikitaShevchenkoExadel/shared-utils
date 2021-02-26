package com.starexchangealliance.shared.utils.tests;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class Table {
    final String sql;
    final List<Map<String, String>> content = new ArrayList<>();

    Table(String sql) {
        this.sql = sql;
    }

    void addRow(Map<String, String> row) {
        this.content.add(row);
    }
}
