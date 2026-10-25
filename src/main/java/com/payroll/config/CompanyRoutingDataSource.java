package com.payroll.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Opens a separate SQLite pool for the company file selected in {@link CurrentCompanyHolder}.
 * Startup can connect to an in-memory database so Hibernate can boot before a company is chosen.
 */
public class CompanyRoutingDataSource extends AbstractRoutingDataSource {

    private final Map<String, HikariDataSource> pools = new ConcurrentHashMap<>();
    private volatile boolean applicationReady;
    private HikariDataSource bootstrap;

    @Override
    protected Object determineCurrentLookupKey() {
        return CurrentCompanyHolder.get();
    }

    @Override
    protected DataSource determineTargetDataSource() {
        String path = CurrentCompanyHolder.get();
        if (path == null) {
            if (applicationReady) {
                throw new IllegalStateException("No company database is open");
            }
            return bootstrap();
        }
        return pools.computeIfAbsent(path, this::createCompanyPool);
    }

    /**
     * After the Spring context is up, repository calls must target a company file.
     */
    public void markReady() {
        this.applicationReady = true;
    }

    /**
     * Closes the pool so the file lock is released.
     */
    public void closePool(String databasePath) {
        if (databasePath == null) {
            return;
        }
        HikariDataSource dataSource = pools.remove(databasePath);
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private DataSource bootstrap() {
        if (bootstrap == null) {
            bootstrap = createPool("jdbc:sqlite::memory:", "bootstrap");
        }
        return bootstrap;
    }

    private HikariDataSource createCompanyPool(String databasePath) {
        return createPool(sqliteUrl(databasePath), "company");
    }

    private String sqliteUrl(String databasePath) {
        return "jdbc:sqlite:" + databasePath.replace('\\', '/');
    }

    private HikariDataSource createPool(String jdbcUrl, String poolName) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(0);
        config.setPoolName(poolName);
        config.setConnectionInitSql("PRAGMA foreign_keys=ON");
        return new HikariDataSource(config);
    }
}
