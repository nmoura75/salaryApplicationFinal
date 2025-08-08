package com.payroll;

import com.payroll.ui.StartMenu;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class SalaryCalculatorApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);
        initDataDirectory();
        this.applicationContext = new SpringApplicationBuilder()
                .sources(SalaryCalculatorApplication.class)
                .run(args);
    }

    private void initDataDirectory() {
        // Get the current working directory
        String currentDir = System.getProperty("user.dir");
        // Create data directory path using File for proper path handling
        File dataDir = new File(currentDir, "data");

        if (!dataDir.exists()) {
            if (!dataDir.mkdirs()) {
                throw new RuntimeException("Failed to create data directory at: " + dataDir.getAbsolutePath());
            }
        }

        // Update application.properties programmatically
        System.setProperty("spring.datasource.url",
                "jdbc:sqlite:" + new File(dataDir, "salary_calculator.db").getAbsolutePath());
    }

    @Override
    public void start(Stage stage) {
        StartMenu startMenu = new StartMenu();
        Scene scene = new Scene(startMenu, 400, 300);
        stage.setTitle("Salary Calculator");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 