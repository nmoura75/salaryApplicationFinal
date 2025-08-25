package com.payroll.controller;

import com.payroll.model.*;
import com.payroll.service.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
@RequiredArgsConstructor
public class RemunerationManagementController implements Initializable {

    private final RemunerationService remunerationService;
    private final EmployeeService employeeService;
    
    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private Label companyNameText;
    
    @FXML
    private Button backButton;
    
    @FXML
    private ComboBox<Employee> employeeComboBox;
    
    @FXML
    private VBox remunerationContainer;
    
    @FXML
    private TextField mealAllowanceField;
    
    @FXML
    private TextField mealAllowanceNotes;
    
    @FXML
    private TextField christmasBonusField;
    
    @FXML
    private TextField christmasBonusNotes;
    
    @FXML
    private TextField vacationSubsidyField;
    
    @FXML
    private TextField vacationSubsidyNotes;
    
    @FXML
    private TextField transportAllowanceField;
    
    @FXML
    private TextField transportAllowanceNotes;
    
    @FXML
    private Label statusText;

    private String companyName;
    private Integer companyId;
    private List<RemunerationCategory> categories;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEmployeeComboBox();
        setupValidation();
    }

    private void setupEmployeeComboBox() {
        employeeComboBox.setCellFactory(param -> new ListCell<Employee>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFirstName() + " " + item.getLastName() + " (ID: " + item.getId() + ")");
                }
            }
        });
        
        employeeComboBox.setButtonCell(employeeComboBox.getCellFactory().call(null));
    }

    private void setupValidation() {
        // Add numeric validation to all amount fields
        addNumericValidation(mealAllowanceField);
        addNumericValidation(christmasBonusField);
        addNumericValidation(vacationSubsidyField);
        addNumericValidation(transportAllowanceField);
    }

    private void addNumericValidation(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                field.setText(oldValue);
            }
        });
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
        if (companyNameText != null) {
            companyNameText.setText("Company: " + companyName);
        }
        loadEmployees();
        loadCategories();
    }
    
    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    private void loadEmployees() {
        try {
            if (companyId != null) {
                List<Employee> employees = employeeService.getEmployeesByCompany(companyId);
                employeeComboBox.setItems(FXCollections.observableArrayList(employees));
            } else {
                showError("Error", "Company ID not set");
            }
        } catch (Exception e) {
            showError("Error", "Failed to load employees: " + e.getMessage());
        }
    }

    private void loadCategories() {
        try {
            if (companyId != null) {
                categories = remunerationService.getCategoriesForCompany(companyId);
            } else {
                showError("Error", "Company ID not set");
            }
        } catch (Exception e) {
            showError("Error", "Failed to load remuneration categories: " + e.getMessage());
        }
    }

    @FXML
    private void loadEmployeeRemunerations() {
        Employee selectedEmployee = employeeComboBox.getValue();
        if (selectedEmployee == null) {
            showError("Error", "Please select an employee first.");
            return;
        }

        try {
            List<EmployeeRemuneration> remunerations = remunerationService.getEmployeeRemunerations(selectedEmployee.getId());
            populateFields(remunerations);
            remunerationContainer.setVisible(true);
            updateStatus("Remunerations loaded for " + selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName(), "success");
        } catch (Exception e) {
            showError("Error", "Failed to load employee remunerations: " + e.getMessage());
        }
    }

    private void populateFields(List<EmployeeRemuneration> remunerations) {
        // Clear all fields first
        clearFields();
        
        // Populate based on category names (this is a simplified approach)
        for (EmployeeRemuneration remuneration : remunerations) {
            // In a real implementation, you'd get the category name and map it properly
            // For now, we'll use a simple approach based on category ID
            if (remuneration.getCategoryId() == 1) { // Meal Allowance
                mealAllowanceField.setText(remuneration.getValue() != null ? remuneration.getValue().toString() : "");
                mealAllowanceNotes.setText(remuneration.getNotes() != null ? remuneration.getNotes() : "");
            } else if (remuneration.getCategoryId() == 2) { // Christmas Bonus
                christmasBonusField.setText(remuneration.getValue() != null ? remuneration.getValue().toString() : "");
                christmasBonusNotes.setText(remuneration.getNotes() != null ? remuneration.getNotes() : "");
            } else if (remuneration.getCategoryId() == 3) { // Vacation Subsidy
                vacationSubsidyField.setText(remuneration.getValue() != null ? remuneration.getValue().toString() : "");
                vacationSubsidyNotes.setText(remuneration.getNotes() != null ? remuneration.getNotes() : "");
            } else if (remuneration.getCategoryId() == 5) { // Transport Allowance
                transportAllowanceField.setText(remuneration.getValue() != null ? remuneration.getValue().toString() : "");
                transportAllowanceNotes.setText(remuneration.getNotes() != null ? remuneration.getNotes() : "");
            }
        }
    }

    private void clearFields() {
        mealAllowanceField.clear();
        mealAllowanceNotes.clear();
        christmasBonusField.clear();
        christmasBonusNotes.clear();
        vacationSubsidyField.clear();
        vacationSubsidyNotes.clear();
        transportAllowanceField.clear();
        transportAllowanceNotes.clear();
    }

    @FXML
    private void saveDefaultRemunerations() {
        Employee selectedEmployee = employeeComboBox.getValue();
        if (selectedEmployee == null) {
            showError("Error", "Please select an employee first.");
            return;
        }

        try {
            // Save meal allowance
            saveRemuneration(selectedEmployee.getId(), 1, mealAllowanceField.getText(), mealAllowanceNotes.getText());
            
            // Save Christmas bonus
            saveRemuneration(selectedEmployee.getId(), 2, christmasBonusField.getText(), christmasBonusNotes.getText());
            
            // Save vacation subsidy
            saveRemuneration(selectedEmployee.getId(), 3, vacationSubsidyField.getText(), vacationSubsidyNotes.getText());
            
            // Save transport allowance
            saveRemuneration(selectedEmployee.getId(), 5, transportAllowanceField.getText(), transportAllowanceNotes.getText());
            
            updateStatus("Default remunerations saved successfully!", "success");
        } catch (Exception e) {
            showError("Error", "Failed to save remunerations: " + e.getMessage());
        }
    }

    private void saveRemuneration(Integer employeeId, Integer categoryId, String valueStr, String notes) {
        if (valueStr != null && !valueStr.trim().isEmpty()) {
            try {
                BigDecimal value = new BigDecimal(valueStr.trim());
                remunerationService.setEmployeeRemuneration(employeeId, categoryId, value);
            } catch (NumberFormatException e) {
                // Skip invalid values
            }
        }
    }

    @FXML
    private void goBackToEmployees() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EmployeesMenu.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            EmployeesMenuController controller = loader.getController();
            controller.setCompanyName(companyName);
            
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/ios-style.css").toExternalForm());
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Employees - " + companyName);
            
        } catch (Exception e) {
            showError("Error", "Failed to return to employees menu: " + e.getMessage());
        }
    }

    private void updateStatus(String message, String type) {
        if (statusText != null) {
            Platform.runLater(() -> statusText.setText(message));
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
