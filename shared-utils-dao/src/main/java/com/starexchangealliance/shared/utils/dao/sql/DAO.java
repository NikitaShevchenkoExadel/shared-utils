package com.starexchangealliance.shared.utils.dao.sql;

import com.starexchangealliance.shared.utils.dao.entity.ID;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DAO<E extends ID> {
    String ID = "id";
    /* insert API */

    E insert(E e);
    E insert(E e, String sql);
    List<E> insert(List<E> e);
    long insert(CompositeFields cf);

    /* update API */

    int update(E e);
    int update(List<E> e);
    int update(CompositeFields cf);
    int update(Long id, CompositeFields cf);
    int update(E e, String sql);

    /* delete API */

    int delete(E e);
    int delete(Long id);
    int delete(List<Long> ids);
    int delete(CompositeFields cf);

    /* search API */

    List<Long> fetchAllIds();
    List<E> fetchAllEntities();
    List<E> fetchAllEntities(long offset, long limit);

    /* get API */

    E getByIdOrThrow(Long id);
    E getByIdOrThrow(Long id, String message);
    Optional<E> getByIdOpt(Long id);
    Optional<E> getFirstOpt(String sql, Map<String, Object> kv);
    Optional<E> getFirstOpt(CompositeFields cf);

    boolean exists(Long id);

    long count();

    /* utils */

    default CompositeFields compose() {
        return CompositeFieldsImpl.of();
    }

    default CompositeFields compose(String key, Object value) {
        return CompositeFieldsImpl.of(key, value);
    }

    interface CompositeFields {
        CompositeFields and(String key, Object value);

        Map<String, Object> get();
    }
}
