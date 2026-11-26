package com.payroll.controller;

import com.payroll.dto.PayrollInput;
import com.payroll.dto.PayrollResult;
import com.payroll.dto.RemunerationLine;
import com.payroll.model.Employee;
import com.payroll.model.EmploymentStatus;
import com.payroll.service.EmployeeService;
import com.payroll.service.PayrollCalculator;
import com.payroll.service.RemunerationService;
import jakarta.persistence.EntityNotFoundException;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SalaryPreparationController {

    private final EmployeeService employeeService;
    private final RemunerationService remunerationService;
    private final PayrollCalculator payrollCalculator;
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
    private TextField businessDaysField;
    @FXML
    private TableView<PreviewRow> previewTable;
    @FXML
    private TableColumn<PreviewRow, String> employeeColumn;
    @FXML
    private TableColumn<PreviewRow, String> grossColumn;
    @FXML
    private TableColumn<PreviewRow, String> employeeSocialSecurityColumn;
    @FXML
    private TableColumn<PreviewRow, String> employerSocialSecurityColumn;
    @FXML
    private TableColumn<PreviewRow, String> taxColumn;
    @FXML
    private TableColumn<PreviewRow, String> netColumn;
    @FXML
    private VBox overrideBox;
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
        employeeColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getEmployeeName()));
        grossColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getGross()));
        employeeSocialSecurityColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getEmployeeSocialSecurity()));
        employerSocialSecurityColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getEmployerSocialSecurity()));
        taxColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getTax()));
        netColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getNet()));
        styleMoneyColumn(grossColumn);
        styleMoneyColumn(employeeSocialSecurityColumn);
        styleMoneyColumn(employerSocialSecurityColumn);
        styleMoneyColumn(taxColumn);
        styleMoneyColumn(netColumn);
        previewTable.getSelectionModel().selectedItemProperty().addListener((observable, oldRow, newRow) -> showOverrides(newRow));
    }

    private void styleMoneyColumn(TableColumn<PreviewRow, String> column) {
        column.setCellFactory(col -> new TableCell<>() {
            {
                getStyleClass().add("money-cell");
                setAlignment(Pos.CENTER_RIGHT);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
            }
        });
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
        if (companyNameText != null) {
            companyNameText.setText(companyName);
        }
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
        refreshPreview();
    }

    @FXML
    private void saveBusinessDays() {
        try {
            int days = Integer.parseInt(businessDaysField.getText().trim());
            remunerationService.setMonthlyBusinessDays(companyId, selectedYear(), selectedMonth(), days);
            refreshPreview();
            setStatus("Business days saved.");
        } catch (NumberFormatException exception) {
            showError("Error", "Enter a whole number of business days and a valid year.");
        }
    }

    @FXML
    private void refreshPreview() {
        if (companyId == null) {
            return;
        }
        try {
            int year = selectedYear();
            int month = selectedMonth();
            businessDaysField.setText(String.valueOf(remunerationService.getEffectiveBusinessDays(companyId, year, month)));
            List<PreviewRow> rows = new ArrayList<>();
            for (Employee employee : employeeService.getEmployeesByCompany(companyId)) {
                if (employee.getStatus() != null && employee.getStatus() != EmploymentStatus.ACTIVE) {
                    continue;
                }
                List<RemunerationLine> lines = remunerationService.calculateLines(employee.getId(), companyId, year, month);
                PayrollResult result = payrollCalculator.calculate(PayrollInput.forEmployee(employee, lines));
                rows.add(new PreviewRow(employee.getId(), employee.getFirstName() + " " + employee.getLastName(), result));
            }
            previewTable.getItems().setAll(rows);
            overrideBox.getChildren().clear();
            setStatus("Preview for " + month + "/" + year + ".");
        } catch (NumberFormatException exception) {
            showError("Error", "Enter a valid year.");
        } catch (IllegalArgumentException | EntityNotFoundException exception) {
            showError("Error", "Could not build the preview: " + exception.getMessage());
        }
    }

    private void showOverrides(PreviewRow row) {
        overrideBox.getChildren().clear();
        if (row == null || companyId == null) {
            return;
        }
        List<RemunerationLine> lines = remunerationService.calculateLines(row.getEmployeeId(), companyId, selectedYear(), selectedMonth());
        for (RemunerationLine line : lines) {
            TextField amountField = new TextField(line.getAmount() == null ? "" : line.getAmount().toPlainString());
            amountField.setPrefWidth(120);
            Button saveButton = new Button("Save override");
            saveButton.setOnAction(event -> saveOverride(row.getEmployeeId(), line.getCategoryId(), amountField.getText()));
            HBox rowBox = new HBox(10, new Label(line.getCategoryName()), amountField, saveButton);
            rowBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            overrideBox.getChildren().add(rowBox);
        }
    }

    private void saveOverride(Integer employeeId, Integer categoryId, String amountText) {
        try {
            BigDecimal amount = new BigDecimal(amountText.trim());
            remunerationService.setMonthlyRemuneration(employeeId, categoryId, selectedYear(), selectedMonth(), amount, true);
            refreshPreview();
            setStatus("Monthly override saved.");
        } catch (NumberFormatException exception) {
            showError("Error", "Enter a valid amount.");
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

    private void setStatus(String message) {
        if (statusText != null) {
            statusText.setText(message);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Getter
    public static class PreviewRow {
        private final Integer employeeId;
        private final String employeeName;
        private final String gross;
        private final String employeeSocialSecurity;
        private final String employerSocialSecurity;
        private final String tax;
        private final String net;

        public PreviewRow(Integer employeeId, String employeeName, PayrollResult result) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.gross = result.getGross().toPlainString();
            this.employeeSocialSecurity = result.getEmployeeSocialSecurity().toPlainString();
            this.employerSocialSecurity = result.getEmployerSocialSecurity().toPlainString();
            this.tax = result.getIncomeTax().toPlainString();
            this.net = result.getNet().toPlainString();
        }
    }
}
