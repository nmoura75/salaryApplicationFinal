package com.payroll.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
@DependsOn("flywayComplete")
public class DatabaseConfig {

    @PostConstruct
    public void init() {
        // Create data directory if it doesn't exist
        File dataDir = new File("./data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        // Ensure database file exists by creating a connection
        ensureDatabaseExists();
    }
    
    private void ensureDatabaseExists() {
        try {
            // This will create the database file if it doesn't exist
            String url = "jdbc:sqlite:./data/salary_calculator.db";
            try (Connection connection = DriverManager.getConnection(url)) {
                // Connection successful, database file exists or was created
                System.out.println("Database connection established successfully");
            }
        } catch (SQLException e) {
            System.err.println("Failed to create/connect to database: " + e.getMessage());
        }
    }
}
