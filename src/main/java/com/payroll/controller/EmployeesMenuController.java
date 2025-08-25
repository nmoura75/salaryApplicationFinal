package com.payroll.controller;

import com.payroll.model.Employee;
import com.payroll.service.EmployeeService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class EmployeesMenuController {

    private final EmployeeService employeeService;
    
    @Autowired
    private ApplicationContext applicationContext;

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
        
        // Format date columns
        hireDateColumn.setCellFactory(column -> new TableCell<Employee, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText(null);
                } else {
                    try {
                        LocalDate date = parseDate(item);
                        if (date != null) {
                            setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        } else {
                            setText(item); // Display as-is if parsing fails
                        }
                    } catch (Exception e) {
                        setText(item); // Display as-is if parsing fails
                    }
                }
            }
        });
        
        dateOfBirthColumn.setCellFactory(column -> new TableCell<Employee, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText(null);
                } else {
                    try {
                        LocalDate date = parseDate(item);
                        if (date != null) {
                            setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        } else {
                            setText(item); // Display as-is if parsing fails
                        }
                    } catch (Exception e) {
                        setText(item); // Display as-is if parsing fails
                    }
                }
            }
        });
        
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
        
        // Setup search functionality
        setupSearch();
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
            } catch (Exception e) {
                // Continue to next format
            }
        }
        
        return null; // Could not parse with any format
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
                
                updateStatus("Loaded " + employees.size() + " employees", "success");
            } else {
                updateStatus("Company ID not set", "error");
            }
        } catch (Exception e) {
            updateStatus("Error loading employees: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void searchEmployees() {
        // Search is handled by the FilteredList
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddEmployeeDialog.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            AddEmployeeDialogController controller = loader.getController();
            controller.setParentController(this);
            controller.setCompanyName(companyName);
            
            Scene scene = new Scene(root, 600, 800);
            scene.getStylesheets().add(getClass().getResource("/css/ios-style.css").toExternalForm());
            
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(backButton.getScene().getWindow());
            dialog.setTitle("Add New Employee");
            dialog.setScene(scene);
            dialog.setResizable(false);
            
            dialog.showAndWait();
            
        } catch (Exception e) {
            showError("Error", "Failed to open add employee dialog: " + e.getMessage());
        }
    }

    @FXML
    private void showRemunerationManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RemunerationManagement.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            RemunerationManagementController controller = loader.getController();
            controller.setCompanyName(companyName);
            controller.setCompanyId(companyId);
            
            Scene scene = new Scene(root, 800, 900);
            scene.getStylesheets().add(getClass().getResource("/css/ios-style.css").toExternalForm());
            
            Stage stage = new Stage();
            stage.setTitle("Remuneration Management - " + companyName);
            stage.setScene(scene);
            stage.setResizable(false);
            
            stage.show();
            
        } catch (Exception e) {
            showError("Error", "Failed to open remuneration management: " + e.getMessage());
        }
    }

    private void editEmployee(Employee employee) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditEmployeeDialog.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            EditEmployeeDialogController controller = loader.getController();
            controller.setParentController(this);
            controller.setEmployee(employee);
            controller.setCompanyName(companyName);
            
            Scene scene = new Scene(root, 600, 800);
            scene.getStylesheets().add(getClass().getResource("/css/ios-style.css").toExternalForm());
            
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(backButton.getScene().getWindow());
            dialog.setTitle("Edit Employee");
            dialog.setScene(scene);
            dialog.setResizable(false);
            
            dialog.showAndWait();
            
        } catch (Exception e) {
            showError("Error", "Failed to open edit employee dialog: " + e.getMessage());
        }
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
                    updateStatus("Employee deleted successfully", "success");
                } catch (Exception e) {
                    showError("Error", "Failed to delete employee: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void goBackToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            MainMenuController controller = loader.getController();
            controller.setCompanyName(companyName);
            
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/ios-style.css").toExternalForm());
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Salary Calculator - " + companyName);
            
        } catch (Exception e) {
            showError("Error", "Failed to return to main menu: " + e.getMessage());
        }
    }



    private void updateStatus(String message, String type) {
        if (statusText != null) {
            statusText.setText(message);
            // You can add styling based on type if needed
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
