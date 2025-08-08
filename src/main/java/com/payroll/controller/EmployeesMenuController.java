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
import javafx.scene.layout.HBox;
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
    private Text companyNameText;
    
    @FXML
    private Button backButton;
    
    @FXML
    private TextField searchField;
    
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
    private TableColumn<Employee, LocalDate> dateOfBirthColumn;
    
    @FXML
    private TableColumn<Employee, BigDecimal> baseSalaryColumn;
    
    @FXML
    private TableColumn<Employee, LocalDate> hireDateColumn;
    
    @FXML
    private TableColumn<Employee, Integer> dependentsColumn;
    
    @FXML
    private TableColumn<Employee, Integer> irsTableColumn;
    
    @FXML
    private TableColumn<Employee, String> statusColumn;
    
    @FXML
    private TableColumn<Employee, Void> actionsColumn;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label countLabel;

    private String companyName;
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
        loadEmployees();
    }

    private void setupTable() {
        // Configure columns
        idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getId()));
        nameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getFirstName() + " " + data.getValue().getLastName()));
        taxIdColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTaxId()));
        socialSecurityIdColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSocialSecurityId()));
        dateOfBirthColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDateOfBirth()));
        baseSalaryColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getBaseSalary()));
        hireDateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getHireDate()));
        dependentsColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDependents()));
        irsTableColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getIrsTable()));
        statusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        
        // Format salary column
        baseSalaryColumn.setCellFactory(column -> new TableCell<Employee, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("€%.2f", item));
                }
            }
        });
        
        // Format date columns
        dateOfBirthColumn.setCellFactory(column -> new TableCell<Employee, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });
        
        hireDateColumn.setCellFactory(column -> new TableCell<Employee, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });
        
        // Format status column
        statusColumn.setCellFactory(column -> new TableCell<Employee, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if ("ACTIVE".equals(item)) {
                        setStyle("-fx-text-fill: #34c759; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #ff3b30; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        // Setup actions column
        actionsColumn.setCellFactory(column -> new TableCell<Employee, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttonBox = new HBox(5, editButton, deleteButton);
            
            {
                editButton.getStyleClass().addAll("button", "secondary");
                deleteButton.getStyleClass().addAll("button", "danger");
                
                editButton.setOnAction(event -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    editEmployee(employee);
                });
                
                deleteButton.setOnAction(event -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    deleteEmployee(employee);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });
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

    private void loadEmployees() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            employeesList.clear();
            employeesList.addAll(employees);
            
            updateStatus("Loaded " + employees.size() + " employees", "success");
            updateCount(employees.size());
            
        } catch (Exception e) {
            updateStatus("Error loading employees: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void searchEmployees() {
        // Search is handled by the FilteredList
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

    public void refreshEmployees() {
        Platform.runLater(this::loadEmployees);
    }

    private void updateStatus(String message, String type) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            if ("error".equals(type)) {
                statusLabel.setStyle("-fx-text-fill: #ff3b30;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #34c759;");
            }
        });
    }

    private void updateCount(int count) {
        Platform.runLater(() -> {
            countLabel.setText(count + " employee" + (count != 1 ? "s" : ""));
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
