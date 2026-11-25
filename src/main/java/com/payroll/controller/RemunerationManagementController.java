package com.payroll.controller;

import com.payroll.model.*;
import com.payroll.service.*;
import jakarta.persistence.EntityNotFoundException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
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
    private final ScreenNavigator screenNavigator;

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
    private TextField commissionField;

    @FXML
    private TextField commissionNotes;
    
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
        addNumericValidation(commissionField);
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
    }
    
    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
        loadEmployees();
        loadCategories();
    }

    private void loadEmployees() {
        if (companyId == null) {
            showError("Error", "Company ID not set");
            return;
        }
        try {
            List<Employee> employees = employeeService.getEmployeesByCompany(companyId);
            employeeComboBox.setItems(FXCollections.observableArrayList(employees));
        } catch (EntityNotFoundException exception) {
            showError("Error", "Failed to load employees: " + exception.getMessage());
        }
    }

    private void loadCategories() {
        if (companyId == null) {
            showError("Error", "Company ID not set");
            return;
        }
        categories = remunerationService.getCategoriesForCompany(companyId);
    }

    @FXML
    private void loadEmployeeRemunerations() {
        Employee selectedEmployee = employeeComboBox.getValue();
        if (selectedEmployee == null) {
            showError("Error", "Please select an employee first.");
            return;
        }

        List<EmployeeRemuneration> remunerations = remunerationService.getEmployeeRemunerations(selectedEmployee.getId());
        populateFields(remunerations);
        remunerationContainer.setVisible(true);
        updateStatus("Remunerations loaded for " + selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName(), "success");
    }

    private void populateFields(List<EmployeeRemuneration> remunerations) {
        // Clear all fields first
        clearFields();
        
        // Populate based on category names (this is a simplified approach)
        for (EmployeeRemuneration remuneration : remunerations) {
            RemunerationCategory category = categoryById(remuneration.getCategoryId());
            if (category == null) {
                continue;
            }
            if (StandardRemunerations.MEAL_ALLOWANCE.equals(category.getName())) {
                mealAllowanceField.setText(text(remuneration.getValue()));
                mealAllowanceNotes.setText(text(remuneration.getNotes()));
            } else if (StandardRemunerations.CHRISTMAS_BONUS.equals(category.getName())) {
                christmasBonusField.setText(text(remuneration.getValue()));
                christmasBonusNotes.setText(text(remuneration.getNotes()));
            } else if (StandardRemunerations.VACATION_SUBSIDY.equals(category.getName())) {
                vacationSubsidyField.setText(text(remuneration.getValue()));
                vacationSubsidyNotes.setText(text(remuneration.getNotes()));
            } else if (StandardRemunerations.TRANSPORT_ALLOWANCE.equals(category.getName())) {
                transportAllowanceField.setText(text(remuneration.getValue()));
                transportAllowanceNotes.setText(text(remuneration.getNotes()));
            } else if ("Commission".equals(category.getName())) {
                commissionField.setText(text(remuneration.getValue()));
                commissionNotes.setText(text(remuneration.getNotes()));
            }
        }
    }

    private RemunerationCategory categoryById(Integer categoryId) {
        if (categories == null) {
            return null;
        }
        return categories.stream()
                .filter(category -> category.getId().equals(categoryId))
                .findFirst()
                .orElse(null);
    }

    private RemunerationCategory categoryByName(String name) {
        if (categories == null) {
            return null;
        }
        return categories.stream()
                .filter(category -> name.equals(category.getName()))
                .findFirst()
                .orElse(null);
    }

    private String text(Object value) {
        return value == null ? "" : value.toString();
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
        commissionField.clear();
        commissionNotes.clear();
    }

    @FXML
    private void saveDefaultRemunerations() {
        Employee selectedEmployee = employeeComboBox.getValue();
        if (selectedEmployee == null) {
            showError("Error", "Please select an employee first.");
            return;
        }

        saveNamed(selectedEmployee.getId(), StandardRemunerations.MEAL_ALLOWANCE, mealAllowanceField.getText(), mealAllowanceNotes.getText());
        saveNamed(selectedEmployee.getId(), StandardRemunerations.CHRISTMAS_BONUS, christmasBonusField.getText(), christmasBonusNotes.getText());
        saveNamed(selectedEmployee.getId(), StandardRemunerations.VACATION_SUBSIDY, vacationSubsidyField.getText(), vacationSubsidyNotes.getText());
        saveNamed(selectedEmployee.getId(), StandardRemunerations.TRANSPORT_ALLOWANCE, transportAllowanceField.getText(), transportAllowanceNotes.getText());
        saveNamed(selectedEmployee.getId(), "Commission", commissionField.getText(), commissionNotes.getText());

        updateStatus("Default remunerations saved successfully!", "success");
    }

    private void saveNamed(Integer employeeId, String categoryName, String valueStr, String notes) {
        RemunerationCategory category = categoryByName(categoryName);
        if (category == null) {
            return;
        }
        saveRemuneration(employeeId, category.getId(), valueStr, notes);
    }

    private void saveRemuneration(Integer employeeId, Integer categoryId, String valueStr, String notes) {
        if (valueStr != null && !valueStr.trim().isEmpty()) {
            try {
                BigDecimal value = new BigDecimal(valueStr.trim());
                remunerationService.setEmployeeRemuneration(employeeId, categoryId, value, notes);
            } catch (NumberFormatException e) {
                // Skip invalid values
            }
        }
    }

    @FXML
    private void goBackToEmployees() {
        screenNavigator.<EmployeesMenuController>showScreen(backButton, "/fxml/EmployeesMenu.fxml",
                "Employees - " + companyName, controller -> {
                    controller.setCompanyId(companyId);
                    controller.setCompanyName(companyName);
                });
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
