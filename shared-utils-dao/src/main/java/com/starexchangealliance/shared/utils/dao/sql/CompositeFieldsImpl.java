package com.starexchangealliance.shared.utils.dao.sql;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CompositeFieldsImpl implements DAO.CompositeFields {
    private Map<String, Object> result;

    private CompositeFieldsImpl(int size) {
        this.result = new LinkedHashMap<>(size, 1F);
    }

    public DAO.CompositeFields and(String key, Object value) {
        if (value instanceof UUID) {
            result.put(key, value.toString());
        } else if (value instanceof Optional<?>) {
            Optional o = ((Optional<?>) value);
            and(key, o.orElse(null));
        } else if (value instanceof Enum<?>) {
            and(key, ((Enum) value).name());
        } else {
            result.put(key, value);
        }
        return this;
    }

    public Map<String, Object> get() {
        return this.result;
    }

    public static DAO.CompositeFields of(String key, Object value) {
        return of().and(key, value);
    }

    public static DAO.CompositeFields of() {
        return new CompositeFieldsImpl(8);
    }
}
