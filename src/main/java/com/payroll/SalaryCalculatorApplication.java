package com.payroll;

import com.payroll.controller.ScreenNavigator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SalaryCalculatorApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(SalaryCalculatorApplication.class).run();
    }

    @Override
    public void start(Stage stage) {
        // Controllers catch only the failures they expect; anything else on the UI thread lands here.
        Thread.currentThread().setUncaughtExceptionHandler((thread, exception) -> showUnexpectedError(exception));

        ScreenNavigator navigator = applicationContext.getBean(ScreenNavigator.class);
        navigator.showScreen(stage, "/fxml/CompanySelection.fxml", "Salary Calculator", controller -> { });
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.show();
    }

    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit();
    }

    private static void showUnexpectedError(Throwable exception) {
        log.error("Unexpected error", exception);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Unexpected error");
        alert.setHeaderText(null);
        alert.setContentText(String.valueOf(exception.getMessage()));
        // show() rather than showAndWait(): the failure may have happened during layout or animation.
        alert.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
