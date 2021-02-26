package com.starexchangealliance.shared.utils.tests;

public interface DatabaseConfig {
    String getSchema();

    String getUrl();

    String getDriverClassName();

    String getUser();

    String getPassword();
}
