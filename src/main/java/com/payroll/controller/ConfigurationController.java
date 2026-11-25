package com.payroll.controller;

import com.payroll.config.Messages;
import com.payroll.i18n.MessageKeys;
import com.payroll.model.RemunerationCategory;
import com.payroll.service.RemunerationService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ConfigurationController {

    private final RemunerationService remunerationService;
    private final ScreenNavigator screenNavigator;
    private final Messages messages;

    @FXML
    private Button backButton;
    @FXML
    private TableView<RemunerationCategory> typesTable;
    @FXML
    private TableColumn<RemunerationCategory, String> socialSecurityColumn;
    @FXML
    private TableColumn<RemunerationCategory, String> taxAuthorityColumn;
    @FXML
    private TableColumn<RemunerationCategory, String> descriptionColumn;
    @FXML
    private TableColumn<RemunerationCategory, String> defaultValueColumn;
    @FXML
    private TextField socialSecurityField;
    @FXML
    private TextField taxAuthorityField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField defaultValueField;
    @FXML
    private ComboBox<RemunerationCategory.RemunerationType> calculationCombo;
    @FXML
    private Button saveChangesButton;

    private Integer companyId;
    private String companyName;

    @FXML
    public void initialize() {
        socialSecurityColumn.setCellValueFactory(new PropertyValueFactory<>("socialSecurityCode"));
        taxAuthorityColumn.setCellValueFactory(new PropertyValueFactory<>("taxAuthorityCode"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        defaultValueColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDefaultValue() == null ? "" : data.getValue().getDefaultValue().toPlainString()));
        calculationCombo.getItems().setAll(RemunerationCategory.RemunerationType.values());
        calculationCombo.setConverter(messages.enumConverter(RemunerationCategory.RemunerationType.class));
        calculationCombo.getSelectionModel().select(RemunerationCategory.RemunerationType.MONTHLY);
        typesTable.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) -> showSelected(selected));
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
        loadTypes();
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @FXML
    private void addType() {
        if (companyId == null) {
            return;
        }
        BigDecimal defaultValue = parseAmount(defaultValueField.getText());
        if (defaultValue == null) {
            showError(messages.text(MessageKeys.INVALID_AMOUNT));
            return;
        }
        try {
            remunerationService.addCategory(companyId, socialSecurityField.getText(), taxAuthorityField.getText(),
                    descriptionField.getText(), calculationCombo.getValue(), defaultValue);
            clearForm();
            loadTypes();
        } catch (IllegalArgumentException exception) {
            showError(messages.text(exception.getMessage()));
        }
    }

    @FXML
    private void saveChanges() {
        RemunerationCategory selected = typesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        BigDecimal defaultValue = parseAmount(defaultValueField.getText());
        if (defaultValue == null) {
            showError(messages.text(MessageKeys.INVALID_AMOUNT));
            return;
        }
        try {
            remunerationService.updateCategory(selected.getId(), socialSecurityField.getText(), taxAuthorityField.getText(),
                    descriptionField.getText(), calculationCombo.getValue(), defaultValue);
            loadTypes();
        } catch (IllegalArgumentException exception) {
            showError(messages.text(exception.getMessage()));
        }
    }

    @FXML
    private void goBack() {
        screenNavigator.showMainMenu(backButton, companyId, companyName);
    }

    private void loadTypes() {
        if (companyId == null) {
            return;
        }
        remunerationService.ensureStandardTypes(companyId);
        typesTable.getItems().setAll(remunerationService.getCategoriesForCompany(companyId));
    }

    private void showSelected(RemunerationCategory category) {
        saveChangesButton.setDisable(category == null);
        if (category == null) {
            return;
        }
        socialSecurityField.setText(text(category.getSocialSecurityCode()));
        taxAuthorityField.setText(text(category.getTaxAuthorityCode()));
        descriptionField.setText(text(category.getName()));
        defaultValueField.setText(category.getDefaultValue() == null ? "" : category.getDefaultValue().toPlainString());
        calculationCombo.setValue(category.getType());
    }

    private void clearForm() {
        typesTable.getSelectionModel().clearSelection();
        socialSecurityField.clear();
        taxAuthorityField.clear();
        descriptionField.clear();
        defaultValueField.clear();
        calculationCombo.getSelectionModel().select(RemunerationCategory.RemunerationType.MONTHLY);
    }

    private BigDecimal parseAmount(String raw) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO;
        }
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

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(messages.text(MessageKeys.ERROR_TITLE));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
