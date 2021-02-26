package com.starexchangealliance.shared.utils.dao.sql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


// TODO refactor
@Slf4j
public class SQLLoggerHelper {

    private SQLLoggerHelper() {
        // hidden
    }

    public static String formatQuery(String sql, SqlParameterSource paramSource) {
        if (sql == null)
            return "NULL SQL";
        if (paramSource == null)
            return sql;
        try {
            ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
            List<String> paramNames = getParameterNamesFromParsedSqlObj(parsedSql);
            List<int[]> parameterIndexes = parameterIndexesFromParsedSqlObj(parsedSql);
            String formatedSql = substituteNamedParameters(sql, paramNames, parameterIndexes, paramSource);
            return formatedSql;
        } catch (Throwable e) {
            log.error("Unable to transform SQL: " + sql, e);
        }
        return sql;
    }

    public static String formatQuery(String sql, Map<String, ?> paramMap) {
        return formatQuery(sql, new MapSqlParameterSource(paramMap));
    }

    private static List<String> getParameterNamesFromParsedSqlObj(ParsedSql parsedSql) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = parsedSql.getClass().getDeclaredMethod("getParameterNames");
        method.setAccessible(true);
        List<String> paramNames = (List<String>) method.invoke(parsedSql);
        return paramNames;
    }

    private static List<int[]> parameterIndexesFromParsedSqlObj(ParsedSql parsedSql) throws NoSuchFieldException, IllegalAccessException {
        Field f = parsedSql.getClass().getDeclaredField("parameterIndexes");
        f.setAccessible(true);
        List<int[]> parameterIndexes = (List<int[]>) f.get(parsedSql);
        return parameterIndexes;
    }

    private static String substituteNamedParameters(String originalSql, List<String> paramNames, List<int[]> parameterIndexes, SqlParameterSource paramSource) {
        StringBuilder actualSql = new StringBuilder();
        int lastIndex = 0;
        for (int i = 0; i < paramNames.size(); i++) {
            String paramName = paramNames.get(i);
            int[] indexes = parameterIndexes.get(i);
            int startIndex = indexes[0];
            int endIndex = indexes[1];
            actualSql.append(originalSql, lastIndex, startIndex);
            if (paramSource != null && paramSource.hasValue(paramName)) {
                Object value = paramSource.getValue(paramName);
                if (value instanceof SqlParameterValue) {
                    value = ((SqlParameterValue) value).getValue();
                }
                if (value instanceof Collection) {
                    Iterator<?> entryIter = ((Collection<?>) value).iterator();
                    int k = 0;
                    while (entryIter.hasNext()) {
                        if (k > 0) {
                            actualSql.append(", ");
                        }
                        k++;
                        Object entryItem = entryIter.next();
                        if (entryItem instanceof Object[]) {
                            Object[] expressionList = (Object[]) entryItem;
                            actualSql.append("(");
                            for (int m = 0; m < expressionList.length; m++) {
                                if (m > 0) {
                                    actualSql.append(", ");
                                }
                                actualSql.append(convertToString(expressionList[m]));
                            }
                            actualSql.append(")");
                        } else {
                            actualSql.append(convertToString(entryItem));
                        }
                    }
                } else {
                    actualSql.append(convertToString(value));
                }
            } else {
                actualSql.append(":" + paramName);
            }
            lastIndex = endIndex;
        }
        actualSql.append(originalSql, lastIndex, originalSql.length());
        return actualSql.toString();
    }

    private static String convertToString(Object each) {
        if (each instanceof Number) {
            return each.toString();
        } else if (each instanceof String) {
            return (String) each;
        } else if (each instanceof Date) {
            return "'" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) each)) + "'";
        } else if (each instanceof byte[]) {
            // BLOB case
            int size = ((byte[]) each).length;
            return "[BLOB size:" + size + " byte]";
        } else {
            return String.valueOf(each);
        }
    }
}
