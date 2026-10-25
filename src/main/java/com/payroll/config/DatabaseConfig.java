package com.payroll.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
public class DatabaseConfig {

    private final CompanyRoutingDataSource routingDataSource = new CompanyRoutingDataSource();

    @Bean
    @Primary
    public DataSource dataSource() {
        routingDataSource.setTargetDataSources(new HashMap<>());
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }

    @Bean
    public CompanyRoutingDataSource companyRoutingDataSource() {
        return routingDataSource;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void markRoutingReady() {
        routingDataSource.markReady();
    }
}
