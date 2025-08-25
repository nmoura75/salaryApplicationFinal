package com.payroll.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@DependsOn("flywayComplete")
public class ServiceConfig {
    // This configuration ensures that all services are created after Flyway completes
}

