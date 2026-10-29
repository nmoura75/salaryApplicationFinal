package com.payroll.controller;

import com.payroll.model.Company;
import com.payroll.service.CompanyService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateCompanyDialogController {

    private final CompanyService companyService;

    @FXML
    private TextField nameField;
    
    @FXML
    private TextField taxIdField;
    
    @FXML
    private TextField socialSecurityIdField;
    
    @FXML
    private TextField addressField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private TextField emailField;

    private CompanySelectionController parentController;

    @FXML
    public void initialize() {
        // Add validation listeners if needed
    }

    public void setParentController(CompanySelectionController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void createCompany() {
        try {
            String name = nameField.getText().trim();
            String taxId = taxIdField.getText().trim();
            String socialSecurityId = socialSecurityIdField.getText().trim();
            String address = addressField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();

            // Validate required fields
            if (name.isEmpty() || taxId.isEmpty() || socialSecurityId.isEmpty()) {
                showError("Validation Error", "Name, Tax ID, and Social Security ID are required.");
                return;
            }

            Company company = new Company();
            company.setName(name);
            company.setTaxId(taxId);
            company.setSocialSecurityId(socialSecurityId);
            company.setAddress(address);
            company.setPhone(phone);
            company.setEmail(email);

            Company savedCompany = companyService.createCompany(company);
            System.out.println("Company created successfully: " + savedCompany.getName());

            // Close the dialog
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();

            // Refresh after the dialog has closed so the parent window updates, not the closing one.
            if (parentController != null) {
                Platform.runLater(() -> {
                    parentController.refreshCompanies();
                    parentController.selectNewlyCreatedCompany(name);
                });
            }

        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError("Error", "Failed to create company: " + exception.getMessage());
        }
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) nameField.getScene().getWindow();
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
