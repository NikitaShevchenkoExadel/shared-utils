package com.starexchangealliance.shared.utils.tests;

import java.util.Arrays;
import java.util.List;

public class StringsSource extends SQLSource {

    private final ListSource listSource;

    public StringsSource(String queries) {
        this.listSource = new ListSource(Arrays.asList(queries.split(SQL_DELIMITER)));
    }

    @Override
    public List<String> getSQLs() {
        return listSource.getSQLs();
    }
}
