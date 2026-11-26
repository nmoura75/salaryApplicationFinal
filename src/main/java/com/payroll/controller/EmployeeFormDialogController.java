package com.payroll.controller;

import com.payroll.config.Messages;
import com.payroll.i18n.MessageKeys;
import com.payroll.model.BankDetails;
import com.payroll.model.ContractType;
import com.payroll.model.Employee;
import com.payroll.model.EmploymentStatus;
import com.payroll.model.Gender;
import com.payroll.model.IncomeHolders;
import com.payroll.model.InsurancePolicy;
import com.payroll.model.MaritalStatus;
import com.payroll.model.RemunerationCategory;
import com.payroll.model.TerminationReason;
import com.payroll.model.WithholdingCategory;
import com.payroll.model.WorkingTime;
import com.payroll.reference.CountryDirectory;
import com.payroll.reference.CountryDirectory.Country;
import com.payroll.service.EmployeeService;
import com.payroll.service.IdentificationNumbers;
import com.payroll.service.MonthlyIncomeLine;
import com.payroll.service.PayrollConstants;
import com.payroll.service.RemunerationService;
import com.payroll.service.StandardRemunerations;
import com.payroll.service.StoredDates;
import com.payroll.service.WithholdingChoice;
import com.payroll.service.WithholdingTableResolver;
import jakarta.persistence.EntityNotFoundException;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EmployeeFormDialogController {

    private static final DateTimeFormatter BIRTH_DATE = DateTimeFormatter.ofPattern("d/M/uuuu");
    private static final DateTimeFormatter BIRTH_DATE_DISPLAY = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final int NIF_MAX_DIGITS = IdentificationNumbers.NIF_MAX_DIGITS;
    private static final int NISS_DIGITS = IdentificationNumbers.NISS_DIGITS;

    private final EmployeeService employeeService;
    private final RemunerationService remunerationService;
    private final WithholdingTableResolver withholdingTableResolver;
    private final Messages messages;

    @FXML
    private Text titleText;
    @FXML
    private Label companyNameText;
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
    private ComboBox<Gender> genderCombo;
    @FXML
    private ComboBox<Country> nationalityCombo;
    @FXML
    private TextField addressField;
    @FXML
    private TextField postalCodeField;
    @FXML
    private TextField localityField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private ComboBox<EmploymentStatus> statusCombo;
    @FXML
    private ComboBox<ContractType> contractTypeCombo;
    @FXML
    private ComboBox<WorkingTime> workingTimeCombo;
    @FXML
    private TextField professionField;
    @FXML
    private TextField departmentField;
    @FXML
    private DatePicker hireDatePicker;
    @FXML
    private DatePicker contractEndDatePicker;
    @FXML
    private DatePicker terminationDatePicker;
    @FXML
    private ComboBox<TerminationReason> terminationReasonCombo;
    @FXML
    private ComboBox<MaritalStatus> maritalStatusCombo;
    @FXML
    private ComboBox<IncomeHolders> incomeHoldersCombo;
    @FXML
    private Spinner<Integer> dependentsSpinner;
    @FXML
    private CheckBox disabilityCheck;
    @FXML
    private TextField baseSalaryField;
    @FXML
    private Label withholdingTableLabel;
    @FXML
    private ComboBox<RemunerationCategory> incomeTypeCombo;
    @FXML
    private TextField incomeAmountField;
    @FXML
    private Label incomeAmountLabel;
    @FXML
    private TableView<MonthlyIncome> monthlyIncomeTable;
    @FXML
    private TextField bankNameField;
    @FXML
    private TextField ibanField;
    @FXML
    private TextField swiftField;
    @FXML
    private TextField insurerField;
    @FXML
    private TextField policyNumberField;

    private EmployeesMenuController parentController;
    private Employee employee;
    private Integer companyId;
    private boolean editing;
    private String unmatchedNationality;
    private final ObservableList<MonthlyIncome> monthlyIncomes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        fill(genderCombo, Gender.class);
        fill(statusCombo, EmploymentStatus.class);
        fill(contractTypeCombo, ContractType.class);
        fill(workingTimeCombo, WorkingTime.class);
        fill(terminationReasonCombo, TerminationReason.class);
        fill(maritalStatusCombo, MaritalStatus.class);
        fill(incomeHoldersCombo, IncomeHolders.class);
        nationalityCombo.getItems().setAll(CountryDirectory.sorted(messages.locale()));
        nationalityCombo.setConverter(countryConverter());
        incomeTypeCombo.setConverter(categoryConverter());
        incomeTypeCombo.valueProperty().addListener((observable, previous, current) -> updateIncomeAmountLabel());
        prepareBirthDate();
        taxIdField.setTextFormatter(digitFormatter(NIF_MAX_DIGITS));
        socialSecurityIdField.setTextFormatter(digitFormatter(NISS_DIGITS));
        setupMonthlyIncomeTable();
        updateIncomeAmountLabel();

        dependentsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));
        statusCombo.getSelectionModel().select(EmploymentStatus.ACTIVE);
        contractTypeCombo.getSelectionModel().select(ContractType.PERMANENT);
        workingTimeCombo.getSelectionModel().select(WorkingTime.FULL_TIME);
        maritalStatusCombo.getSelectionModel().select(MaritalStatus.NOT_MARRIED);
        incomeHoldersCombo.getSelectionModel().select(IncomeHolders.SINGLE_HOLDER);
        hireDatePicker.setValue(LocalDate.now());
        nationalityCombo.setValue(CountryDirectory.find("PT").orElse(null));

        statusCombo.valueProperty().addListener((observable, previous, current) -> updateContractFields());
        contractTypeCombo.valueProperty().addListener((observable, previous, current) -> updateContractFields());
        maritalStatusCombo.valueProperty().addListener((observable, previous, current) -> {
            updateIncomeHolders();
            refreshWithholdingTable();
        });
        incomeHoldersCombo.valueProperty().addListener((observable, previous, current) -> refreshWithholdingTable());
        dependentsSpinner.valueProperty().addListener((observable, previous, current) -> refreshWithholdingTable());
        disabilityCheck.selectedProperty().addListener((observable, previous, current) -> refreshWithholdingTable());

        updateContractFields();
        updateIncomeHolders();
        refreshWithholdingTable();
    }

    public void setParentController(EmployeesMenuController parentController) {
        this.parentController = parentController;
    }

    public void setCompany(Integer companyId, String companyName) {
        this.companyId = companyId;
        if (companyNameText != null) {
            companyNameText.setText(companyName == null ? "" : companyName);
        }
        loadIncomeTypes();
    }

    public void prepareNew() {
        this.employee = new Employee();
        this.editing = false;
        this.unmatchedNationality = null;
        monthlyIncomes.clear();
        titleText.setText(messages.text(MessageKeys.ADD_EMPLOYEE));
        if (nationalityCombo.getValue() == null) {
            nationalityCombo.setValue(CountryDirectory.find("PT").orElse(null));
        }
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
        this.editing = true;
        titleText.setText(messages.text(MessageKeys.EDIT_EMPLOYEE));
        populateForm(employee);
    }

    @FXML
    private void save() {
        if (!requiredFieldsFilled()) {
            showError(messages.text(MessageKeys.VALIDATION_TITLE), messages.text(MessageKeys.REQUIRED_FIELDS));
            return;
        }
        if (IdentificationNumbers.nif(taxIdField.getText()) == null) {
            showError(messages.text(MessageKeys.VALIDATION_TITLE), messages.text(MessageKeys.INVALID_NIF));
            return;
        }
        if (IdentificationNumbers.niss(socialSecurityIdField.getText()) == null) {
            showError(messages.text(MessageKeys.VALIDATION_TITLE), messages.text(MessageKeys.INVALID_NISS));
            return;
        }
        if (!birthDateAccepted()) {
            showError(messages.text(MessageKeys.VALIDATION_TITLE), messages.text(MessageKeys.INVALID_BIRTH_DATE));
            return;
        }
        BigDecimal salary = parseSalary(baseSalaryField.getText());
        if (salary == null) {
            showError(messages.text(MessageKeys.VALIDATION_TITLE), messages.text(MessageKeys.INVALID_SALARY));
            return;
        }
        if (!editing && companyId == null) {
            showError(messages.text(MessageKeys.ERROR_TITLE), messages.text(MessageKeys.COMPANY_REQUIRED));
            return;
        }

        if (employee == null) {
            employee = new Employee();
        }
        readForm(employee, salary);
        if (!editing) {
            employee.setCompanyId(companyId);
            employee.setUsefulDaysOfWork(PayrollConstants.DEFAULT_WORKING_DAYS);
        }

        try {
            Employee saved;
            if (editing) {
                saved = employeeService.updateEmployee(employee.getId(), employee);
            } else {
                saved = employeeService.createEmployee(employee);
            }
            remunerationService.replaceMonthlyIncomes(saved.getId(), currentIncomes());
            close();
            if (parentController != null) {
                Platform.runLater(parentController::refreshEmployees);
            }
        } catch (IllegalArgumentException | EntityNotFoundException exception) {
            showError(messages.text(MessageKeys.ERROR_TITLE),
                    messages.format(MessageKeys.SAVE_FAILED, messages.text(exception.getMessage())));
        }
    }

    @FXML
    private void cancel() {
        close();
    }

    private void populateForm(Employee source) {
        firstNameField.setText(text(source.getFirstName()));
        lastNameField.setText(text(source.getLastName()));
        taxIdField.setText(text(source.getTaxId()));
        socialSecurityIdField.setText(text(source.getSocialSecurityId()));
        dateOfBirthPicker.setValue(StoredDates.parse(source.getDateOfBirth()));
        genderCombo.setValue(source.getGender());
        showNationality(source.getNationality());
        addressField.setText(text(source.getAddress()));
        postalCodeField.setText(text(source.getPostalCode()));
        localityField.setText(text(source.getLocality()));
        emailField.setText(text(source.getEmail()));
        phoneField.setText(text(source.getPhone()));
        statusCombo.setValue(source.getStatus() == null ? EmploymentStatus.ACTIVE : source.getStatus());
        contractTypeCombo.setValue(source.getContractType() == null ? ContractType.PERMANENT : source.getContractType());
        workingTimeCombo.setValue(source.getWorkingTime() == null ? WorkingTime.FULL_TIME : source.getWorkingTime());
        professionField.setText(text(source.getPosition()));
        departmentField.setText(text(source.getDepartment()));
        hireDatePicker.setValue(StoredDates.parse(source.getHireDate()));
        contractEndDatePicker.setValue(StoredDates.parse(source.getContractEndDate()));
        terminationDatePicker.setValue(StoredDates.parse(source.getTerminationDate()));
        terminationReasonCombo.setValue(source.getTerminationReason());
        if (source.getMaritalStatus() != null) {
            maritalStatusCombo.setValue(source.getMaritalStatus());
        }
        if (source.getIncomeHolders() != null) {
            incomeHoldersCombo.setValue(source.getIncomeHolders());
        }
        dependentsSpinner.getValueFactory().setValue(source.getDependents() == null ? 0 : source.getDependents());
        disabilityCheck.setSelected(source.isDisability());
        baseSalaryField.setText(source.getBaseSalary() == null ? "" : source.getBaseSalary().toPlainString());
        BankDetails bank = source.getBankDetails();
        if (bank != null) {
            bankNameField.setText(text(bank.getBankName()));
            ibanField.setText(text(bank.getIban()));
            swiftField.setText(text(bank.getSwift()));
        }
        InsurancePolicy policy = source.getInsurancePolicy();
        if (policy != null) {
            insurerField.setText(text(policy.getInsurer()));
            policyNumberField.setText(text(policy.getPolicyNumber()));
        }
        showMonthlyIncomes(source.getId());
        updateContractFields();
        updateIncomeHolders();
        refreshWithholdingTable();
    }

    private void readForm(Employee target, BigDecimal salary) {
        target.setFirstName(firstNameField.getText().trim());
        target.setLastName(lastNameField.getText().trim());
        target.setTaxId(IdentificationNumbers.nif(taxIdField.getText()));
        target.setSocialSecurityId(IdentificationNumbers.niss(socialSecurityIdField.getText()));
        target.setDateOfBirth(StoredDates.iso(dateOfBirthPicker.getValue()));
        target.setGender(genderCombo.getValue());
        target.setNationality(selectedNationality());
        target.setAddress(blankToNull(addressField.getText()));
        target.setPostalCode(blankToNull(postalCodeField.getText()));
        target.setLocality(blankToNull(localityField.getText()));
        target.setEmail(blankToNull(emailField.getText()));
        target.setPhone(blankToNull(phoneField.getText()));
        target.setStatus(statusCombo.getValue());
        target.setContractType(contractTypeCombo.getValue());
        target.setWorkingTime(workingTimeCombo.getValue());
        target.setPosition(blankToNull(professionField.getText()));
        target.setDepartment(blankToNull(departmentField.getText()));
        target.setHireDate(StoredDates.iso(hireDatePicker.getValue()));
        if (contractTypeCombo.getValue() == ContractType.FIXED_TERM) {
            target.setContractEndDate(StoredDates.iso(contractEndDatePicker.getValue()));
        } else {
            target.setContractEndDate(null);
        }
        if (statusCombo.getValue() == EmploymentStatus.INACTIVE) {
            target.setTerminationDate(StoredDates.iso(terminationDatePicker.getValue()));
            target.setTerminationReason(terminationReasonCombo.getValue());
        } else {
            target.setTerminationDate(null);
            target.setTerminationReason(null);
        }
        target.setMaritalStatus(maritalStatusCombo.getValue());
        if (maritalStatusCombo.getValue() == MaritalStatus.MARRIED) {
            target.setIncomeHolders(incomeHoldersCombo.getValue());
        } else {
            target.setIncomeHolders(IncomeHolders.SINGLE_HOLDER);
        }
        target.setDependents(dependentsSpinner.getValue());
        target.setDisability(disabilityCheck.isSelected());
        target.setBaseSalary(salary);

        BankDetails bank = target.getBankDetails() == null ? new BankDetails() : target.getBankDetails();
        bank.setBankName(blankToNull(bankNameField.getText()));
        bank.setIban(normalizeIban(ibanField.getText()));
        bank.setSwift(blankToNull(swiftField.getText()));
        target.setBankDetails(bank);

        InsurancePolicy policy = target.getInsurancePolicy() == null
                ? new InsurancePolicy()
                : target.getInsurancePolicy();
        policy.setInsurer(blankToNull(insurerField.getText()));
        policy.setPolicyNumber(blankToNull(policyNumberField.getText()));
        target.setInsurancePolicy(policy);
    }

    private boolean requiredFieldsFilled() {
        return !firstNameField.getText().isBlank()
                && !lastNameField.getText().isBlank()
                && !taxIdField.getText().isBlank()
                && !socialSecurityIdField.getText().isBlank()
                && !baseSalaryField.getText().isBlank()
                && hireDatePicker.getValue() != null
                && maritalStatusCombo.getValue() != null
                && statusCombo.getValue() != null
                && contractTypeCombo.getValue() != null;
    }

    private void updateContractFields() {
        boolean fixedTerm = contractTypeCombo.getValue() == ContractType.FIXED_TERM;
        contractEndDatePicker.setDisable(!fixedTerm);
        boolean inactive = statusCombo.getValue() == EmploymentStatus.INACTIVE;
        terminationDatePicker.setDisable(!inactive);
        terminationReasonCombo.setDisable(!inactive);
    }

    private void updateIncomeHolders() {
        boolean married = maritalStatusCombo.getValue() == MaritalStatus.MARRIED;
        incomeHoldersCombo.setDisable(!married);
        if (!married) {
            incomeHoldersCombo.setValue(IncomeHolders.SINGLE_HOLDER);
        }
    }

    private void refreshWithholdingTable() {
        if (maritalStatusCombo.getValue() == null
                || incomeHoldersCombo.getValue() == null
                || dependentsSpinner.getValue() == null) {
            withholdingTableLabel.setText("");
            return;
        }
        WithholdingChoice choice = withholdingTableResolver.resolve(
                maritalStatusCombo.getValue(),
                incomeHoldersCombo.getValue(),
                dependentsSpinner.getValue(),
                disabilityCheck.isSelected());
        String category = messages.text(choice.category() == WithholdingCategory.CATEGORY_H
                ? "withholding.category.h" : "withholding.category.a");
        withholdingTableLabel.setText(messages.format("withholding.selection", category, messages.enumText(choice.table())));
    }

    @FXML
    private void addMonthlyIncome() {
        RemunerationCategory category = incomeTypeCombo.getValue();
        if (category == null) {
            showError(messages.text(MessageKeys.VALIDATION_TITLE), messages.text(MessageKeys.INCOME_TYPE_REQUIRED));
            return;
        }
        BigDecimal amount = parseSalary(incomeAmountField.getText());
        if (amount == null) {
            showError(messages.text(MessageKeys.VALIDATION_TITLE), messages.text(MessageKeys.INVALID_AMOUNT));
            return;
        }
        boolean alreadyListed = monthlyIncomes.stream()
                .anyMatch(income -> category.getId().equals(income.category().getId()));
        if (alreadyListed) {
            showError(messages.text(MessageKeys.VALIDATION_TITLE), messages.text(MessageKeys.DUPLICATE_INCOME));
            return;
        }
        monthlyIncomes.add(new MonthlyIncome(category, amount));
        incomeTypeCombo.setValue(null);
        incomeAmountField.clear();
    }

    private void loadIncomeTypes() {
        if (companyId == null) {
            return;
        }
        remunerationService.ensureStandardTypes(companyId);
        List<RemunerationCategory> types = remunerationService.getCategoriesForCompany(companyId).stream()
                .filter(category -> !StandardRemunerations.isBaseSalary(category))
                .toList();
        incomeTypeCombo.getItems().setAll(types);
    }

    private void showMonthlyIncomes(Integer employeeId) {
        monthlyIncomes.clear();
        if (employeeId == null) {
            return;
        }
        for (var remuneration : remunerationService.getEmployeeRemunerations(employeeId)) {
            RemunerationCategory category = incomeTypeCombo.getItems().stream()
                    .filter(candidate -> candidate.getId().equals(remuneration.getCategoryId()))
                    .findFirst()
                    .orElse(null);
            if (category == null || remuneration.getValue() == null) {
                continue;
            }
            monthlyIncomes.add(new MonthlyIncome(category, remuneration.getValue()));
        }
    }

    private List<MonthlyIncomeLine> currentIncomes() {
        List<MonthlyIncomeLine> lines = new ArrayList<>();
        for (MonthlyIncome income : monthlyIncomes) {
            lines.add(new MonthlyIncomeLine(income.category().getId(), income.amount()));
        }
        return lines;
    }

    private void setupMonthlyIncomeTable() {
        TableColumn<MonthlyIncome, String> typeColumn = new TableColumn<>(messages.text("employee.incomeType"));
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().category().getName()));
        typeColumn.setPrefWidth(320);
        TableColumn<MonthlyIncome, String> unitColumn = new TableColumn<>(messages.text("employee.incomeUnit"));
        unitColumn.setCellValueFactory(data -> new SimpleStringProperty(amountUnit(data.getValue().category())));
        unitColumn.setPrefWidth(110);
        TableColumn<MonthlyIncome, String> amountColumn = new TableColumn<>(messages.text("employee.incomeAmount"));
        amountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().amount().toPlainString()));
        amountColumn.setPrefWidth(140);
        amountColumn.setCellFactory(column -> incomeAmountCell());
        TableColumn<MonthlyIncome, Void> removeColumn = new TableColumn<>();
        removeColumn.setPrefWidth(110);
        removeColumn.setCellFactory(column -> new TableCell<>() {
            private final Button removeButton = new Button(messages.text("action.remove"));

            {
                removeButton.setOnAction(event -> {
                    MonthlyIncome row = getTableRow().getItem();
                    if (row != null) {
                        monthlyIncomes.remove(row);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeButton);
            }
        });
        monthlyIncomeTable.getColumns().setAll(typeColumn, unitColumn, amountColumn, removeColumn);
        monthlyIncomeTable.setItems(monthlyIncomes);
    }

    private TableCell<MonthlyIncome, String> incomeAmountCell() {
        return new TableCell<>() {
            private final TextField amountField = new TextField();
            private boolean refreshing;

            {
                amountField.setMaxWidth(Double.MAX_VALUE);
                amountField.setOnAction(event -> applyAmount());
                amountField.focusedProperty().addListener((observable, wasFocused, focused) -> {
                    if (!focused) {
                        applyAmount();
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                MonthlyIncome row = empty ? null : getTableRow().getItem();
                if (row == null) {
                    setGraphic(null);
                    return;
                }
                if (!amountField.isFocused()) {
                    refreshing = true;
                    amountField.setText(row.amount().toPlainString());
                    refreshing = false;
                }
                setText(null);
                setGraphic(amountField);
            }

            private void applyAmount() {
                if (refreshing) {
                    return;
                }
                MonthlyIncome row = getTableRow() == null ? null : getTableRow().getItem();
                if (row == null) {
                    return;
                }
                BigDecimal amount = parseSalary(amountField.getText());
                if (amount == null) {
                    refreshing = true;
                    amountField.setText(row.amount().toPlainString());
                    refreshing = false;
                    showError(messages.text(MessageKeys.VALIDATION_TITLE), messages.text(MessageKeys.INVALID_AMOUNT));
                    return;
                }
                row.setAmount(amount);
            }
        };
    }

    private void updateIncomeAmountLabel() {
        RemunerationCategory category = incomeTypeCombo.getValue();
        String key = "employee.incomeAmount";
        if (category != null) {
            key = dailyRate(category) ? "employee.amountPerDay" : "employee.amountPerMonth";
        }
        incomeAmountLabel.setText(messages.text(key));
    }

    private String amountUnit(RemunerationCategory category) {
        return messages.text(dailyRate(category) ? "employee.perDay" : "employee.perMonth");
    }

    private static boolean dailyRate(RemunerationCategory category) {
        return category.getType() == RemunerationCategory.RemunerationType.DAILY
                || category.getContributionRule() == RemunerationCategory.ContributionRule.MEAL_DAILY;
    }

    private void prepareBirthDate() {
        dateOfBirthPicker.setEditable(true);
        dateOfBirthPicker.setConverter(birthDateConverter());
        dateOfBirthPicker.setPromptText(messages.text("employee.dateFormat"));
        dateOfBirthPicker.showingProperty().addListener((observable, wasShowing, showing) -> {
            if (showing) {
                Platform.runLater(this::makeBirthYearEditable);
            }
        });
    }

    private void makeBirthYearEditable() {
        if (!(dateOfBirthPicker.getSkin() instanceof DatePickerSkin skin)) {
            return;
        }
        for (Node node : skin.getPopupContent().lookupAll(".spinner")) {
            if (node instanceof Spinner<?> spinner && spinner.getValue() instanceof Integer year && year >= 1000) {
                spinner.setEditable(true);
            }
        }
    }

    private boolean birthDateAccepted() {
        if (dateOfBirthPicker.getEditor() == null) {
            return true;
        }
        String typed = dateOfBirthPicker.getEditor().getText();
        if (typed == null || typed.isBlank()) {
            dateOfBirthPicker.setValue(null);
            return true;
        }
        try {
            dateOfBirthPicker.setValue(LocalDate.parse(typed.trim(), BIRTH_DATE));
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    private StringConverter<LocalDate> birthDateConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : BIRTH_DATE_DISPLAY.format(date);
            }

            @Override
            public LocalDate fromString(String value) {
                if (value == null || value.isBlank()) {
                    return null;
                }
                return LocalDate.parse(value.trim(), BIRTH_DATE);
            }
        };
    }

    private static TextFormatter<String> digitFormatter(int maxDigits) {
        return new TextFormatter<>(change -> {
            String next = change.getControlNewText();
            if (next.length() > maxDigits || !next.chars().allMatch(Character::isDigit)) {
                return null;
            }
            return change;
        });
    }

    private void showNationality(String stored) {
        CountryDirectory.find(stored).ifPresentOrElse(country -> {
            nationalityCombo.setValue(country);
            unmatchedNationality = null;
        }, () -> {
            nationalityCombo.setValue(null);
            unmatchedNationality = blankToNull(stored);
        });
    }

    private String selectedNationality() {
        return nationalityCombo.getValue() == null ? unmatchedNationality : nationalityCombo.getValue().code();
    }

    private StringConverter<Country> countryConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Country country) {
                return country == null ? "" : country.name(messages.locale());
            }

            @Override
            public Country fromString(String value) {
                return null;
            }
        };
    }

    private StringConverter<RemunerationCategory> categoryConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(RemunerationCategory category) {
                return category == null ? "" : category.getName();
            }

            @Override
            public RemunerationCategory fromString(String value) {
                return null;
            }
        };
    }

    private static final class MonthlyIncome {
        private final RemunerationCategory category;
        private BigDecimal amount;

        private MonthlyIncome(RemunerationCategory category, BigDecimal amount) {
            this.category = category;
            this.amount = amount;
        }

        private RemunerationCategory category() {
            return category;
        }

        private BigDecimal amount() {
            return amount;
        }

        private void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    private <E extends Enum<E>> void fill(ComboBox<E> combo, Class<E> type) {
        combo.getItems().setAll(type.getEnumConstants());
        combo.setConverter(messages.enumConverter(type));
    }

    private BigDecimal parseSalary(String raw) {
        String normalized = raw.trim().replace(" ", "").replace("€", "");
        if (normalized.contains(",") && !normalized.contains(".")) {
            normalized = normalized.replace(',', '.');
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String normalizeIban(String value) {
        String iban = blankToNull(value);
        return iban == null ? null : iban.replace(" ", "").toUpperCase();
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private void close() {
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
