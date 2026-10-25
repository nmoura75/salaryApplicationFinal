package com.payroll.config;

import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Applies the schema migrations to one company SQLite file.
 */
@Component
public class CompanyDatabaseMigrator {

    /**
     * Creates the parent directory when needed and migrates the file to the current schema.
     */
    public void migrate(Path databaseFile) {
        try {
            Files.createDirectories(databaseFile.getParent());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create the company database directory", exception);
        }

        Flyway.configure()
                .dataSource("jdbc:sqlite:" + databaseFile.toAbsolutePath().toString().replace('\\', '/'), "", "")
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .cleanDisabled(true)
                .load()
                .migrate();
    }
}
