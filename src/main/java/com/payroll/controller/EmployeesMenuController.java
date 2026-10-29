package com.payroll.controller;

import com.payroll.controller.ScreenNavigator.WindowSize;
import com.payroll.model.Employee;
import com.payroll.service.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class EmployeesMenuController {

    private static final WindowSize EMPLOYEE_DIALOG = new WindowSize(600, 800);
    private static final WindowSize REMUNERATION_WINDOW = new WindowSize(800, 900);
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Employee dates are stored as free text, so several formats are accepted; tried in this order.
    private static final List<DateTimeFormatter> STORED_DATE_FORMATS = List.of(
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    private final EmployeeService employeeService;
    private final ScreenNavigator screenNavigator;

    @FXML
    private Label companyNameText;
    
    @FXML
    private Button backButton;
    
    @FXML
    private TableView<Employee> employeesTable;
    
    @FXML
    private TableColumn<Employee, Integer> idColumn;
    
    @FXML
    private TableColumn<Employee, String> nameColumn;
    
    @FXML
    private TableColumn<Employee, String> taxIdColumn;
    
    @FXML
    private TableColumn<Employee, String> socialSecurityIdColumn;
    
    @FXML
    private TableColumn<Employee, String> dateOfBirthColumn;
    
    @FXML
    private TableColumn<Employee, BigDecimal> baseSalaryColumn;
    
    @FXML
    private TableColumn<Employee, String> hireDateColumn;
    
    @FXML
    private TableColumn<Employee, Integer> dependentsColumn;
    
    @FXML
    private TableColumn<Employee, Integer> irsTableColumn;
    
    @FXML
    private TableColumn<Employee, String> statusColumn;
    
    @FXML
    private Button editButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Text statusText;
    
    @FXML
    private TextField searchField;

    private String companyName;
    private Integer companyId;
    private ObservableList<Employee> employeesList;
    private FilteredList<Employee> filteredEmployees;

    @FXML
    public void initialize() {
        setupTable();
        setupSearch();
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
        if (companyNameText != null) {
            companyNameText.setText(companyName);
        }
        // If companyId is already set, load employees immediately
        if (this.companyId != null) {
            loadEmployees();
        }
    }
    
    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
        // Load employees after both company name and ID are set
        if (this.companyName != null && this.companyId != null) {
            loadEmployees();
        }
    }

    private void setupTable() {
        // Setup table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getFirstName() + " " + data.getValue().getLastName()));
        taxIdColumn.setCellValueFactory(new PropertyValueFactory<>("taxId"));
        socialSecurityIdColumn.setCellValueFactory(new PropertyValueFactory<>("socialSecurityId"));
        baseSalaryColumn.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
        hireDateColumn.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        dateOfBirthColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        dependentsColumn.setCellValueFactory(new PropertyValueFactory<>("dependents"));
        irsTableColumn.setCellValueFactory(new PropertyValueFactory<>("irsTable"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Format salary column
        baseSalaryColumn.setCellFactory(column -> new TableCell<Employee, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("€" + item.toString());
                }
            }
        });
        
        hireDateColumn.setCellFactory(column -> createDateCell());
        dateOfBirthColumn.setCellFactory(column -> createDateCell());
        
        // Format status column with color coding
        statusColumn.setCellFactory(column -> new TableCell<Employee, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("ACTIVE".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        // Setup table selection listener
        employeesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean hasSelection = newValue != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
    }

    private TableCell<Employee, String> createDateCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText(null);
                } else {
                    setText(formatForDisplay(item));
                }
            }
        };
    }

    private String formatForDisplay(String storedDate) {
        LocalDate date = parseDate(storedDate);
        return date == null ? storedDate : date.format(DISPLAY_DATE_FORMAT);
    }

    private void setupSearch() {
        employeesList = FXCollections.observableArrayList();
        filteredEmployees = new FilteredList<>(employeesList, p -> true);
        employeesTable.setItems(filteredEmployees);
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredEmployees.setPredicate(employee -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                return employee.getFirstName().toLowerCase().contains(lowerCaseFilter) ||
                       employee.getLastName().toLowerCase().contains(lowerCaseFilter) ||
                       employee.getTaxId().toLowerCase().contains(lowerCaseFilter) ||
                       employee.getSocialSecurityId().toLowerCase().contains(lowerCaseFilter) ||
                       (employee.getDateOfBirth() != null && employee.getDateOfBirth().toString().toLowerCase().contains(lowerCaseFilter)) ||
                       (employee.getStatus() != null && employee.getStatus().toLowerCase().contains(lowerCaseFilter));
            });
        });
    }

    private LocalDate parseDate(String storedDate) {
        if (storedDate == null || storedDate.trim().isEmpty()) {
            return null;
        }
        for (DateTimeFormatter format : STORED_DATE_FORMATS) {
            try {
                return LocalDate.parse(storedDate, format);
            } catch (DateTimeParseException notThisFormat) {
                // Fall through to the next candidate format.
            }
        }
        return null;
    }
    
    public void refreshEmployees() {
        loadEmployees();
    }
    
    private void loadEmployees() {
        try {
            if (companyId != null) {
                List<Employee> employees = employeeService.getEmployeesByCompany(companyId);
                employeesList.clear();
                employeesList.addAll(employees);
                
                updateStatus("Loaded " + employees.size() + " employees");
            } else {
                updateStatus("Company ID not set");
            }
        } catch (EntityNotFoundException exception) {
            updateStatus("Error loading employees: " + exception.getMessage());
        }
    }

    @FXML
    private void editSelectedEmployee() {
        Employee employee = employeesTable.getSelectionModel().getSelectedItem();
        if (employee != null) {
            editEmployee(employee);
        } else {
            showError("Error", "Please select an employee to edit.");
        }
    }
    
    @FXML
    private void deleteSelectedEmployee() {
        Employee employee = employeesTable.getSelectionModel().getSelectedItem();
        if (employee != null) {
            deleteEmployee(employee);
        } else {
            showError("Error", "Please select an employee to delete.");
        }
    }

    @FXML
    private void showAddEmployeeDialog() {
        screenNavigator.<AddEmployeeDialogController>showModalDialog(backButton, "/fxml/AddEmployeeDialog.fxml",
                "Add New Employee", EMPLOYEE_DIALOG, controller -> {
                    controller.setParentController(this);
                    controller.setCompanyName(companyName);
                });
    }

    @FXML
    private void showRemunerationManagement() {
        screenNavigator.<RemunerationManagementController>openWindow("/fxml/RemunerationManagement.fxml",
                "Remuneration Management - " + companyName, REMUNERATION_WINDOW, controller -> {
                    controller.setCompanyId(companyId);
                    controller.setCompanyName(companyName);
                });
    }

    private void editEmployee(Employee employee) {
        screenNavigator.<EditEmployeeDialogController>showModalDialog(backButton, "/fxml/EditEmployeeDialog.fxml",
                "Edit Employee", EMPLOYEE_DIALOG, controller -> {
                    controller.setParentController(this);
                    controller.setEmployee(employee);
                    controller.setCompanyName(companyName);
                });
    }

    private void deleteEmployee(Employee employee) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Employee");
        alert.setContentText("Are you sure you want to delete " + employee.getFirstName() + " " + employee.getLastName() + "?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    employeeService.deleteEmployee(employee.getId());
                    loadEmployees();
                    updateStatus("Employee deleted successfully");
                } catch (EntityNotFoundException exception) {
                    showError("Error", "Failed to delete employee: " + exception.getMessage());
                }
            }
        });
    }

    @FXML
    private void goBackToMainMenu() {
        screenNavigator.showMainMenu(backButton, companyId, companyName);
    }

    private void updateStatus(String message) {
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
}
