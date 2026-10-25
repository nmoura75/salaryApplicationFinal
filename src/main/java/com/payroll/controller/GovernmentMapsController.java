package com.payroll.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GovernmentMapsController {

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private Button backButton;

    @FXML
    private Label companyNameText;

    private String companyName;
    private Integer companyId;

    @FXML
    public void initialize() {
        // Reports are not available yet.
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
        if (companyNameText != null) {
            companyNameText.setText(companyName);
        }
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    @FXML
    private void goBackToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            MainMenuController controller = loader.getController();
            controller.setCompanyId(companyId);
            controller.setCompanyName(companyName);
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/ios-style.css").toExternalForm());
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Salary Calculator - " + companyName);
        } catch (Exception exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to return to the main menu: " + exception.getMessage());
            alert.showAndWait();
        }
    }
}
