package com.payroll.controller;

import com.payroll.dto.ProcessMonthResult;
import com.payroll.service.SalaryReceiptService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class SalaryProcessingController {

    private final SalaryReceiptService salaryReceiptService;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private Button backButton;
    @FXML
    private Label companyNameText;
    @FXML
    private TextField yearField;
    @FXML
    private ComboBox<Integer> monthComboBox;
    @FXML
    private Text statusText;

    private String companyName;
    private Integer companyId;

    @FXML
    public void initialize() {
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        yearField.setText(String.valueOf(nextMonth.getYear()));
        monthComboBox.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        monthComboBox.setValue(nextMonth.getMonthValue());
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
    private void processMonth() {
        if (companyId == null) {
            showError("Error", "No company is open.");
            return;
        }
        try {
            ProcessMonthResult result = salaryReceiptService.processMonth(companyId, selectedYear(), selectedMonth());
            StringBuilder message = new StringBuilder();
            message.append("Created ").append(result.getCreated().size()).append(" receipt(s).");
            if (!result.getRefusedEmployeeNames().isEmpty()) {
                message.append(" Already processed: ");
                message.append(String.join(", ", result.getRefusedEmployeeNames()));
                message.append(".");
            }
            statusText.setText(message.toString());
        } catch (Exception exception) {
            showError("Error", "Could not create receipts: " + exception.getMessage());
        }
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
            showError("Error", "Failed to return to the main menu: " + exception.getMessage());
        }
    }

    private int selectedYear() {
        return Integer.parseInt(yearField.getText().trim());
    }

    private int selectedMonth() {
        return monthComboBox.getValue();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
