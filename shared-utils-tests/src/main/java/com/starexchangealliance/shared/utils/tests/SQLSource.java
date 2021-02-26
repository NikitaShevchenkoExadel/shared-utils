package com.starexchangealliance.shared.utils.tests;

import java.util.List;

public abstract class SQLSource {
    public static final String SQL_DELIMITER = ";";

    public abstract List<String> getSQLs();
}
