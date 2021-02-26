package com.starexchangealliance.shared.utils.dao.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starexchangealliance.shared.utils.dao.entity.ID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public abstract class BaseDAO<E extends ID> implements DAO<E>, SQLConstructor {

    private final BeanPropertyRowMapper<E> mapper;
    private final String table;

    protected QueryExecutor queryExecutor;

    public BaseDAO(String table, Class<E> cls) {
        this.table = table;
        this.mapper = BeanPropertyRowMapper.newInstance(cls);
    }

    @PostConstruct
    public void init() {
        QueryExecutor executor = queryExecutor();
        if (executor == null) {
            throw new RuntimeException("To use BaseDAO you must provide QueryExecutor");
        }
        this.queryExecutor = executor;
    }

    protected abstract QueryExecutor queryExecutor();

    @Override
    public String getTableName() {
        return table;
    }

    protected RowMapper<E> rowMapper() {
        return mapper;
    }

    @Override
    public boolean exists(Long id) {
        return queryExecutor.count("SELECT 1 FROM " + table + " WHERE " +
                ID + "=:" + ID, compose(ID, id).get()) > 0;
    }

    @Override
    public E insert(E e) {
        Map<String, Object> kv = asMap(e, false);
        e.setId(insert(kv));
        return e;
    }

    //TODO apply batch insert
    @Override
    public List<E> insert(List<E> e) {
        List<E> result = new ArrayList<>();
        for (E each : e) {
            result.add(insert(each));
        }
        return result;
    }

    @Override
    public E insert(E e, String sql) {
        Map<String, Object> kv = asMap(e, false);
        e.setId(insert(kv, sql));
        return e;
    }

    @Override
    public long insert(CompositeFields cf) {
        return insert(cf.get());
    }

    private long insert(Map<String, Object> kv) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = insertFor(kv);

        int count = queryExecutor.apply(sql, kv, keyHolder);

        if (count == 0) {
            throw new RuntimeException("Creating failed, no rows affected after insert. SQL " + sql);
        }

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new RuntimeException("Creating failed, no rows affected after insert. SQL " + sql);
        }

        return key.longValue();
    }

    @Override
    public int update(E e) {
        Map<String, Object> kv = asMap(e, true);
        return queryExecutor.apply(updateFor(kv), kv);
    }

    @Override
    public int update(CompositeFields args) {
        Map<String, Object> kv = args.get();
        return queryExecutor.apply(updateFor(kv), kv);
    }

    @Override
    public int update(Long id, CompositeFields cf) {
        Map<String, Object> kv = cf.get();
        kv.put(ID, id);
        return queryExecutor.apply(updateFor(kv), kv);
    }

    //TODO apply batch update
    @Override
    public int update(List<E> e) {
        int count = 0;
        for (E each : e) {
            count += update(each);
        }
        return count;
    }

    @Override
    public int update(E e, String sql) {
        Map<String, Object> kv = asMap(e, true);
        return queryExecutor.apply(sql, kv);
    }

    //TODO apply batch delete
    @Override
    public int delete(List<Long> e) {
        int count = 0;
        for (Long each : e) {
            count += delete(each);
        }
        return count;
    }

    //TODO implement
    @Override
    public int delete(CompositeFields cf) {
        throw new NotImplementedException();
    }

    @Override
    public int delete(Long id) {
        return queryExecutor.apply(deleteWhere(ID + "=:" + ID), compose(ID, id).get());
    }

    @Override
    public int delete(E e) {
        long id = e.getId();
        return id > 0 ? delete(id) : 0;
    }

    @Override
    public List<E> fetchAllEntities() {
        return queryExecutor.list("SELECT * FROM " + table, rowMapper());
    }

    @Override
    public List<E> fetchAllEntities(long offset, long limit) {
        return queryExecutor.list("SELECT * FROM " + table +
                " LIMIT " + limit + " OFFSET " + offset, rowMapper());
    }

    @Override
    public List<Long> fetchAllIds() {
        return queryExecutor.list("SELECT " + ID + " FROM " + table + " ORDER BY ID", (rs, idx) -> rs.getLong(ID));
    }

    @Override
    public Optional<E> getByIdOpt(Long id) {
        return queryExecutor.oneOptional(searchBy(ID), compose(ID, id).get(), rowMapper());
    }

    @Override
    public Optional<E> getFirstOpt(String sql, Map<String, Object> kv) {
        return queryExecutor.oneOptional(sql, kv, rowMapper());
    }

    //TODO implement
    @Override
    public Optional<E> getFirstOpt(CompositeFields cf) {
        throw new NotImplementedException();
    }

    @Override
    public E getByIdOrThrow(Long id) {
        return getByIdOrThrow(id, "Record by id " + id + " not found ");
    }

    @Override
    public E getByIdOrThrow(Long id, String msg) {
        Optional<E> maybeId = getByIdOpt(id);

        if (!maybeId.isPresent()) {
            log.error("Unable to find record by id = {} for entity {} message {}", id, table, msg);

            throw new RuntimeException(String.format(msg, String.valueOf(id)));
        }

        return maybeId.get();
    }

    @Override
    public long count() {
        return queryExecutor.count("SELECT COUNT(*) FROM " + table, Collections.emptyMap());
    }

    private long insert(Map<String, Object> kv, String sql) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int count = queryExecutor.apply(sql, kv, keyHolder);

        if (count == 0) {
            throw new RuntimeException("Creating failed, no rows affected after insert. SQL " + sql);
        }

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys == null || keys.isEmpty()) {
            throw new RuntimeException("Creating failed, no rows affected after insert. SQL " + sql);
        }

        return (long) keys.get(ID);
    }

    private Map<String, Object> asMap(E e, boolean includeId) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map result = objectMapper.convertValue(e, Map.class);
        if (!includeId) {
            result.remove(ID);
        }
        return result;
    }

}
