package com.payroll.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Autowired
    private Environment env;

    @Bean
    @DependsOn("dataSource")
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .cleanDisabled(false)
                .load();
        
        // Run migrations
        flyway.migrate();
        
        return flyway;
    }
    
    @Bean
    @DependsOn("flyway")
    public String flywayComplete() {
        // This bean ensures that Flyway completes before other beans are created
        return "Flyway migration completed";
    }
}
