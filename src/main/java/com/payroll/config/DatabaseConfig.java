package com.payroll.config;

import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.File;

@Configuration
public class DatabaseConfig {

    @PostConstruct
    public void init() {
        // Create data directory if it doesn't exist
        File dataDir = new File("./data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }
}
