package com.starexchangealliance.shared.utils.tests;

import java.util.List;
import java.util.stream.Collectors;

public class ListSource extends SQLSource {

    private final List<String> queries;

    public ListSource(List<String> queries) {
        this.queries = queries.stream().map(s ->
                s.replaceAll("\n", " ")).filter(s -> s.trim().length() > 0).
                collect(Collectors.toList());
    }

    @Override
    public List<String> getSQLs() {
        return queries;
    }
}
