package com.payroll.controller;

import com.payroll.model.Employee;
import com.payroll.service.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
@RequiredArgsConstructor
public class EditEmployeeDialogController {

    private final EmployeeService employeeService;

    @FXML
    private Text companyNameText;
    
    @FXML
    private TextField firstNameField;
    
    @FXML
    private TextField lastNameField;
    
    @FXML
    private TextField taxIdField;
    
    @FXML
    private TextField socialSecurityIdField;
    
    @FXML
    private DatePicker dateOfBirthPicker;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private TextField baseSalaryField;
    
    @FXML
    private DatePicker hireDatePicker;
    
    @FXML
    private TextField dependentsField;
    
    @FXML
    private ComboBox<String> familiarSituationComboBox;
    
    @FXML
    private ComboBox<String> irsTableComboBox;
    
    @FXML
    private ComboBox<String> statusComboBox;

    private EmployeesMenuController parentController;
    private Employee employee;
    private String companyName;

    @FXML
    public void initialize() {
        // Initialize familiar situation options
        familiarSituationComboBox.getItems().addAll("Single", "Married", "Divorced", "Widowed");
        irsTableComboBox.getItems().addAll("Table 1", "Table 2", "Table 3", "Table 4", "Table 5", "Table 6");
        statusComboBox.getItems().addAll("ACTIVE", "INACTIVE");
    }

    public void setParentController(EmployeesMenuController parentController) {
        this.parentController = parentController;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
        populateFields();
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
        if (companyNameText != null) {
            companyNameText.setText(companyName);
        }
    }

    private void populateFields() {
        if (employee != null) {
            firstNameField.setText(employee.getFirstName());
            lastNameField.setText(employee.getLastName());
            taxIdField.setText(employee.getTaxId());
            socialSecurityIdField.setText(employee.getSocialSecurityId());
            emailField.setText(employee.getEmail() != null ? employee.getEmail() : "");
            phoneField.setText(employee.getPhone() != null ? employee.getPhone() : "");
            baseSalaryField.setText(employee.getBaseSalary().toString());
            
            if (employee.getHireDate() != null && !employee.getHireDate().isEmpty()) {
                LocalDate hireDate = parseDate(employee.getHireDate());
                hireDatePicker.setValue(hireDate != null ? hireDate : LocalDate.now());
            }
            
            dependentsField.setText(String.valueOf(employee.getDependents()));
            
            if (employee.getDateOfBirth() != null && !employee.getDateOfBirth().isEmpty()) {
                dateOfBirthPicker.setValue(parseDate(employee.getDateOfBirth()));
            }
            
            // Set familiar situation
            switch (employee.getFamiliarSituation()) {
                case 1:
                    familiarSituationComboBox.setValue("Single");
                    break;
                case 2:
                    familiarSituationComboBox.setValue("Married");
                    break;
                case 3:
                    familiarSituationComboBox.setValue("Divorced");
                    break;
                case 4:
                    familiarSituationComboBox.setValue("Widowed");
                    break;
                default:
                    familiarSituationComboBox.setValue("Single");
            }
            
            // Set IRS table
            String irsTableValue = "Table " + employee.getIrsTable();
            irsTableComboBox.setValue(irsTableValue);
            
            // Set status
            statusComboBox.setValue(employee.getStatus());
        }
    }

    @FXML
    private void updateEmployee() {
        try {
            // Validate required fields
            if (firstNameField.getText().trim().isEmpty() || 
                lastNameField.getText().trim().isEmpty() ||
                taxIdField.getText().trim().isEmpty() ||
                socialSecurityIdField.getText().trim().isEmpty() ||
                baseSalaryField.getText().trim().isEmpty() ||
                hireDatePicker.getValue() == null) {
                
                showError("Validation Error", "Please fill in all required fields (*).");
                return;
            }

            // Update employee
            employee.setFirstName(firstNameField.getText().trim());
            employee.setLastName(lastNameField.getText().trim());
            employee.setTaxId(taxIdField.getText().trim());
            employee.setSocialSecurityId(socialSecurityIdField.getText().trim());
            employee.setEmail(emailField.getText().trim());
            employee.setPhone(phoneField.getText().trim());
            
            // Set date of birth
            if (dateOfBirthPicker.getValue() != null) {
                employee.setDateOfBirth(dateOfBirthPicker.getValue().toString());
            }
            
            // Parse salary
            try {
                BigDecimal salary = new BigDecimal(baseSalaryField.getText().trim());
                employee.setBaseSalary(salary);
            } catch (NumberFormatException e) {
                showError("Validation Error", "Please enter a valid salary amount.");
                return;
            }
            
            employee.setHireDate(hireDatePicker.getValue().toString());
            
            // Parse dependents
            if (!dependentsField.getText().trim().isEmpty()) {
                try {
                    int dependents = Integer.parseInt(dependentsField.getText().trim());
                    employee.setDependents(dependents);
                } catch (NumberFormatException e) {
                    showError("Validation Error", "Please enter a valid number of dependents.");
                    return;
                }
            } else {
                employee.setDependents(0);
            }
            
            // Set familiar situation (convert string to integer)
            String familiarSituation = familiarSituationComboBox.getValue();
            if ("Single".equals(familiarSituation)) {
                employee.setFamiliarSituation(1);
            } else if ("Married".equals(familiarSituation)) {
                employee.setFamiliarSituation(2);
            } else if ("Divorced".equals(familiarSituation)) {
                employee.setFamiliarSituation(3);
            } else if ("Widowed".equals(familiarSituation)) {
                employee.setFamiliarSituation(4);
            } else {
                employee.setFamiliarSituation(1); // Default to Single
            }
            
            // Set IRS table (convert string to integer)
            String irsTable = irsTableComboBox.getValue();
            if (irsTable != null && irsTable.startsWith("Table ")) {
                try {
                    int tableNumber = Integer.parseInt(irsTable.substring(6));
                    employee.setIrsTable(tableNumber);
                } catch (NumberFormatException e) {
                    employee.setIrsTable(1); // Default to Table 1
                }
            } else {
                employee.setIrsTable(1); // Default to Table 1
            }
            
            // Set status
            String status = statusComboBox.getValue();
            employee.setStatus(status != null ? status : "ACTIVE");

            // Save employee
            Employee updatedEmployee = employeeService.updateEmployee(employee.getId(), employee);
            
            // Close dialog
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.close();
            
            // Refresh parent controller
            if (parentController != null) {
                Platform.runLater(parentController::refreshEmployees);
            }
            
        } catch (IllegalArgumentException | EntityNotFoundException exception) {
            showError("Error", "Failed to update employee: " + exception.getMessage());
        }
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        // Try different date formats
        String[] formats = {
            "yyyy-MM-dd",      // ISO format
            "dd-MM-yyyy",      // European format with dashes
            "dd/MM/yyyy",      // European format with slashes
            "MM/dd/yyyy",      // US format
            "yyyy/MM/dd"       // ISO format with slashes
        };
        
        for (String format : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException notThisFormat) {
                // Fall through to the next candidate format.
            }
        }
        
        return null; // Could not parse with any format
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
