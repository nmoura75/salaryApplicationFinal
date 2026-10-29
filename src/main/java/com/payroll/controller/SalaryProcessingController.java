package com.payroll.controller;

import com.payroll.dto.ProcessMonthResult;
import com.payroll.service.SalaryReceiptService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class SalaryProcessingController {

    private final SalaryReceiptService salaryReceiptService;
    private final ScreenNavigator screenNavigator;

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
        } catch (NumberFormatException exception) {
            showError("Error", "Enter a valid year.");
        } catch (IllegalStateException exception) {
            showError("Error", "Could not create receipts: " + exception.getMessage());
        }
    }

    @FXML
    private void goBackToMainMenu() {
        screenNavigator.showMainMenu(backButton, companyId, companyName);
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
