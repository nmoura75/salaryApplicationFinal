package com.payroll.controller;

import com.payroll.config.Messages;
import com.payroll.controller.ScreenNavigator.WindowSize;
import com.payroll.i18n.MessageKeys;
import com.payroll.model.ContractType;
import com.payroll.model.Employee;
import com.payroll.model.EmploymentStatus;
import com.payroll.model.WithholdingTable;
import com.payroll.service.EmployeeService;
import com.payroll.service.StoredDates;
import jakarta.persistence.EntityNotFoundException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EmployeesMenuController {

    private static final WindowSize EMPLOYEE_DIALOG = new WindowSize(900, 700);
    private static final WindowSize REMUNERATION_WINDOW = new WindowSize(800, 900);

    private final EmployeeService employeeService;
    private final ScreenNavigator screenNavigator;
    private final Messages messages;

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
    private TableColumn<Employee, WithholdingTable> withholdingTableColumn;
    @FXML
    private TableColumn<Employee, EmploymentStatus> statusColumn;
    @FXML
    private TableColumn<Employee, ContractType> contractTypeColumn;
    @FXML
    private TableColumn<Employee, String> professionColumn;
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
        if (this.companyId != null) {
            loadEmployees();
        }
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
        if (this.companyName != null && this.companyId != null) {
            loadEmployees();
        }
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getFirstName() + " " + data.getValue().getLastName()));
        taxIdColumn.setCellValueFactory(new PropertyValueFactory<>("taxId"));
        socialSecurityIdColumn.setCellValueFactory(new PropertyValueFactory<>("socialSecurityId"));
        baseSalaryColumn.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
        hireDateColumn.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        dateOfBirthColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        dependentsColumn.setCellValueFactory(new PropertyValueFactory<>("dependents"));
        withholdingTableColumn.setCellValueFactory(new PropertyValueFactory<>("withholdingTable"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        contractTypeColumn.setCellValueFactory(new PropertyValueFactory<>("contractType"));
        professionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));

        baseSalaryColumn.setCellFactory(column -> new TableCell<>() {
            {
                getStyleClass().add("money-cell");
                setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            }

            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "€" + item.toPlainString());
            }
        });

        hireDateColumn.setCellFactory(column -> createDateCell());
        dateOfBirthColumn.setCellFactory(column -> createDateCell());
        withholdingTableColumn.setCellFactory(column -> createEnumCell());
        contractTypeColumn.setCellFactory(column -> createEnumCell());
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(EmploymentStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("status-active", "status-inactive");
                    return;
                }
                setText(messages.enumText(item));
                getStyleClass().removeAll("status-active", "status-inactive");
                if (item == EmploymentStatus.ACTIVE) {
                    getStyleClass().add("status-active");
                } else {
                    getStyleClass().add("status-inactive");
                }
            }
        });

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
                setText(empty || item == null || item.isEmpty() ? null : StoredDates.display(item));
            }
        };
    }

    private <E extends Enum<E>> TableCell<Employee, E> createEnumCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(E item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : messages.enumText(item));
            }
        };
    }

    private void setupSearch() {
        employeesList = FXCollections.observableArrayList();
        filteredEmployees = new FilteredList<>(employeesList, employee -> true);
        employeesTable.setItems(filteredEmployees);

        searchField.textProperty().addListener((observable, oldValue, newValue) ->
                filteredEmployees.setPredicate(employee -> matchesSearch(employee, newValue)));
    }

    private boolean matchesSearch(Employee employee, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String filter = query.toLowerCase();
        return contains(employee.getFirstName(), filter)
                || contains(employee.getLastName(), filter)
                || contains(employee.getTaxId(), filter)
                || contains(employee.getSocialSecurityId(), filter)
                || contains(employee.getDateOfBirth(), filter)
                || contains(employee.getPosition(), filter)
                || contains(messages.enumText(employee.getStatus()), filter)
                || contains(messages.enumText(employee.getContractType()), filter);
    }

    private static boolean contains(String value, String filter) {
        return value != null && value.toLowerCase().contains(filter);
    }

    public void refreshEmployees() {
        loadEmployees();
    }

    private void loadEmployees() {
        try {
            if (companyId == null) {
                updateStatus(messages.text(MessageKeys.COMPANY_MISSING));
                return;
            }
            List<Employee> employees = employeeService.getEmployeesByCompany(companyId);
            employeesList.clear();
            employeesList.addAll(employees);
            updateStatus(messages.format(MessageKeys.LOADED_EMPLOYEES, employees.size()));
        } catch (EntityNotFoundException exception) {
            updateStatus(messages.format(MessageKeys.LOAD_FAILED, exception.getMessage()));
        }
    }

    @FXML
    private void editSelectedEmployee() {
        Employee employee = employeesTable.getSelectionModel().getSelectedItem();
        if (employee == null) {
            showError(messages.text(MessageKeys.ERROR_TITLE), messages.text(MessageKeys.SELECT_EMPLOYEE));
            return;
        }
        showEmployeeForm(employee);
    }

    @FXML
    private void deleteSelectedEmployee() {
        Employee employee = employeesTable.getSelectionModel().getSelectedItem();
        if (employee == null) {
            showError(messages.text(MessageKeys.ERROR_TITLE), messages.text(MessageKeys.SELECT_EMPLOYEE));
            return;
        }
        deleteEmployee(employee);
    }

    @FXML
    private void showAddEmployeeDialog() {
        showEmployeeForm(null);
    }

    @FXML
    private void showRemunerationManagement() {
        screenNavigator.<RemunerationManagementController>openWindow("/fxml/RemunerationManagement.fxml",
                "Remuneration Management - " + companyName, REMUNERATION_WINDOW, controller -> {
                    controller.setCompanyId(companyId);
                    controller.setCompanyName(companyName);
                });
    }

    private void showEmployeeForm(Employee employee) {
        String title = messages.text(employee == null ? MessageKeys.ADD_EMPLOYEE : MessageKeys.EDIT_EMPLOYEE);
        screenNavigator.<EmployeeFormDialogController>showModalDialog(backButton, "/fxml/EmployeeFormDialog.fxml",
                title, EMPLOYEE_DIALOG, controller -> {
                    controller.setParentController(this);
                    controller.setCompany(companyId, companyName);
                    if (employee == null) {
                        controller.prepareNew();
                    } else {
                        controller.setEmployee(employee);
                    }
                });
    }

    private void deleteEmployee(Employee employee) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(messages.text(MessageKeys.CONFIRM_DELETE_TITLE));
        alert.setHeaderText(messages.text(MessageKeys.CONFIRM_DELETE_HEADER));
        alert.setContentText(messages.format(MessageKeys.CONFIRM_DELETE_CONTENT,
                employee.getFirstName() + " " + employee.getLastName()));

        alert.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }
            try {
                employeeService.deleteEmployee(employee.getId());
                loadEmployees();
                updateStatus(messages.text(MessageKeys.EMPLOYEE_DELETED));
            } catch (EntityNotFoundException exception) {
                showError(messages.text(MessageKeys.ERROR_TITLE),
                        messages.format(MessageKeys.DELETE_FAILED, exception.getMessage()));
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
