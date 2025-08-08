package com.payroll.controller;

import com.payroll.model.Employee;
import com.payroll.model.Company;
import com.payroll.service.EmployeeService;
import com.payroll.service.CompanyService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AddEmployeeDialogController {

    private final EmployeeService employeeService;
    private final CompanyService companyService;

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
    private TextField addressField;
    
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
    private String companyName;

    @FXML
    public void initialize() {
        // Set default values
        hireDatePicker.setValue(LocalDate.now());
        familiarSituationComboBox.getSelectionModel().selectFirst();
        irsTableComboBox.getSelectionModel().selectFirst();
        statusComboBox.getSelectionModel().selectFirst();
    }

    public void setParentController(EmployeesMenuController parentController) {
        this.parentController = parentController;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
        if (companyNameText != null) {
            companyNameText.setText(companyName);
        }
    }

    @FXML
    private void addEmployee() {
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

            // Create employee
            Employee employee = new Employee();
            employee.setFirstName(firstNameField.getText().trim());
            employee.setLastName(lastNameField.getText().trim());
            employee.setTaxId(taxIdField.getText().trim());
            employee.setSocialSecurityId(socialSecurityIdField.getText().trim());
            employee.setEmail(emailField.getText().trim());
            employee.setPhone(phoneField.getText().trim());
            employee.setPosition(""); // Default value
            employee.setDepartment(""); // Default value
            employee.setBankAccount(""); // Default value
            
            // Set date of birth
            if (dateOfBirthPicker.getValue() != null) {
                employee.setDateOfBirth(dateOfBirthPicker.getValue());
            }
            
            // Parse salary
            try {
                BigDecimal salary = new BigDecimal(baseSalaryField.getText().trim());
                employee.setBaseSalary(salary);
            } catch (NumberFormatException e) {
                showError("Validation Error", "Please enter a valid salary amount.");
                return;
            }
            
            employee.setHireDate(hireDatePicker.getValue());
            
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
            
            employee.setUsefulDaysOfWork(22); // Default value

            // Set company
            if (companyName != null && !companyName.trim().isEmpty()) {
                try {
                    Company company = companyService.getCompanyByName(companyName);
                    employee.setCompany(company);
                } catch (Exception e) {
                    showError("Error", "Failed to find company: " + e.getMessage());
                    return;
                }
            } else {
                showError("Error", "Company name is required");
                return;
            }

            // Save employee
            Employee savedEmployee = employeeService.createEmployee(employee);
            
            // Close dialog
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.close();
            
            // Refresh parent controller
            if (parentController != null) {
                Platform.runLater(parentController::refreshEmployees);
            }
            
        } catch (Exception e) {
            showError("Error", "Failed to create employee: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
