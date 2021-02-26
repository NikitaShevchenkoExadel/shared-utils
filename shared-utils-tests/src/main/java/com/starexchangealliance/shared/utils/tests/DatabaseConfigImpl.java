package com.starexchangealliance.shared.utils.tests;

public final class DatabaseConfigImpl implements DatabaseConfig {

    private final String url;
    private final String driverClassName;
    private final String username;
    private final String password;
    private final String schema;

    public DatabaseConfigImpl(String url,
                              String driverClassName,
                              String username,
                              String password,
                              String schema) {
        this.url = url;
        this.driverClassName = driverClassName;
        this.username = username;
        this.password = password;
        this.schema = schema;
    }

    @Override
    public String getSchema() {
        return this.schema;
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public String getDriverClassName() {
        return this.driverClassName;
    }

    @Override
    public String getUser() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }
}
