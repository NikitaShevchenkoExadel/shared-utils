package com.starexchangealliance.shared.utils.dao.sql;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public interface SQLConstructor {
    String _ID = "id";
    String LIMIT = "limit";
    String OFFSET = "offset";

    String getTableName();

    default String insertFor(Map<String, ?> kv) {
        return insertFor(kv.keySet());
    }

    default String insertFor(Collection<String> k) {
        StringBuilder c = new StringBuilder();
        StringBuilder v = new StringBuilder();

        Iterator<String> itr = k.iterator();
        while (itr.hasNext()) {
            String next = itr.next();

            c.append(next);
            v.append(":").append(next);

            if (itr.hasNext()) {
                c.append(",");
                v.append(",");
            }
        }

        return "INSERT INTO " + getTableName() + " (" + c.toString() + ") VALUES (" + v.toString() + ")";
    }

    default String updateFor(Map<String, ?> kv) {
        return updateFor(kv.keySet());
    }

    default String updateFor(Collection<String> k) {
        StringBuilder c = new StringBuilder();

        Iterator<String> itr = k.iterator();
        while (itr.hasNext()) {
            String next = itr.next();

            c.append(next).append("=:").append(next);

            if (itr.hasNext()) {
                c.append(",");
            }
        }

        return "UPDATE " + getTableName() + " SET " + c.toString() + " WHERE " + _ID + "=:" + _ID;
    }

    default String searchWhere(String where) {
        return StringUtils.isEmpty(where) ? "SELECT * FROM " + getTableName() :
                "SELECT * FROM " + getTableName() + " WHERE " + where;
    }

    default String countWhere(String where) {
        return StringUtils.isEmpty(where) ? "SELECT COUNT(*) FROM " + getTableName() :
                "SELECT COUNT(*) FROM " + getTableName() + " WHERE " + where;
    }
    default String searchBy(String... fields) {
        StringBuilder w = new StringBuilder();

        Iterator<String> itr = Arrays.stream(fields).iterator();
        while (itr.hasNext()) {
            String next = itr.next();

            w.append(next).append("=:").append(next);

            if (itr.hasNext()) {
                w.append(" AND ");
            }
        }

        return searchWhere(w.toString());
    }

    default String deleteWhere(String where) {
        return "DELETE FROM " + getTableName() + " WHERE " + where;
    }

    default String searchInBy(String field) {
        return searchWhere(field + " IN (:ids)");
    }

    default String searchLike(String field) {
        return searchWhere(like(field));
    }

    default String like(String field) {
        return field + " LIKE :" + field;
    }

    default String limitAndOffset(String sql) {
        return sql + " " + LIMIT.toUpperCase() + " :" + LIMIT + " " + OFFSET.toUpperCase() + " :" + OFFSET;
    }
}
