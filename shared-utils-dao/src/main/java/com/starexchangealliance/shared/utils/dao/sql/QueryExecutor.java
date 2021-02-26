package com.starexchangealliance.shared.utils.dao.sql;

import com.starexchangealliance.shared.utils.codes.KeyCodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
public class QueryExecutor {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final boolean logSQL;
    private final long slowSqlLimit;
    private final long problemSqlLimit;

    public QueryExecutor(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                         boolean logSQL,
                         long slowSqlLimit,
                         long problemSqlLimit) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.logSQL = logSQL;
        this.slowSqlLimit = slowSqlLimit;
        this.problemSqlLimit = problemSqlLimit;
    }

    private NamedParameterJdbcTemplate getJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    public int apply(final String sql, final Map<String, Object> params) {
        return around(
                () -> format(sql, params),
                () -> getJdbcTemplate().update(sql, params));
    }

    public int apply(String sql, Map<String, Object> kv, KeyHolder keyHolder) {
        return around(
                () -> format(sql, kv),
                () -> getJdbcTemplate().update(sql, new MapSqlParameterSource(kv), keyHolder));
    }

    public <E> List<E> list(String sql, RowMapper<E> mapper) {
        return list(sql, Collections.emptyMap(), mapper);
    }

    public <E> List<E> list(String sql, Map<String, Object> kv, RowMapper<E> mapper) {
        return around(
                () -> format(sql, kv),
                () -> getJdbcTemplate().query(sql, kv, mapper));
    }

    public int count(String sql, Map<String, Object> kv) {
        return around(
                () -> format(sql, kv),
                () -> optional(() -> getJdbcTemplate().queryForObject(sql, kv, Integer.class))).orElse(0);
    }

    public <E> List<E> listIn(String sql, Collection<?> ids, RowMapper<E> mapper) {
        if (ids == null || ids.isEmpty()) {
            log.info("LIST IN (?:ids) IS NULL OR EMPTY");
            return Collections.emptyList();
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids", ids);

        Map<String, Object> kv = parameters.getValues();
        return around(
                () -> format(sql, kv),
                () -> list(sql, kv, mapper));
    }

    public <E> Optional<E> oneOptional(String sql, Map<String, Object> kv, RowMapper<E> mapper) {
        return around(
                () -> format(sql, kv),
                () -> optional(() -> getJdbcTemplate().queryForObject(sql, kv, mapper)));
    }

    private String format(String sql, Map<String, Object> kv) {
        return SQLLoggerHelper.formatQuery(sql, kv);
    }

    private <E> Optional<E> optional(Supplier<E> function) {
        try {
            return Optional.of(function.get());
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private <T> T around(Supplier<String> sqlSupplier, Supplier<T> op) {
        if (logSQL) {
            log.info(KeyCodes.SQL + " : " + sqlSupplier.get());
        }

        StopWatch stopWatch = StopWatch.createStarted();
        try {
            return op.get();
        } catch (Exception e) {
            log.error(KeyCodes.INCORRECT_SQL_ERROR + " : " + sqlSupplier.get());
            throw e;
        } finally {
            triggerOnHttpDelays(stopWatch.getTime(), sqlSupplier);
        }
    }

    private void triggerOnHttpDelays(long delay, Supplier<String> sqlSupplier) {
        if (delay >= problemSqlLimit) {
            log.error(KeyCodes.PROBLEM_SQL_QUERY + " : " + sqlSupplier.get());
        } else if (delay >= slowSqlLimit) {
            log.info(KeyCodes.SLOW_SQL_QUERY + " : " + sqlSupplier.get());
        }
    }

}
