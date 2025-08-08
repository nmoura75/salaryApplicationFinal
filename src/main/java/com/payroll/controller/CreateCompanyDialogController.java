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

import java.io.File;

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

            // Create company database file
            File dataDir = new File("./data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            String dbFileName = name.replaceAll("[^a-zA-Z0-9]", "_") + ".db";
            File dbFile = new File(dataDir, dbFileName);

            // Set the database URL for this new company
            System.setProperty("spring.datasource.url", "jdbc:sqlite:" + dbFile.getAbsolutePath());

            // Create the company entity
            Company company = new Company();
            company.setName(name);
            company.setTaxId(taxId);
            company.setTaxIdPassword(""); // Will be set later
            company.setSocialSecurityId(socialSecurityId);
            company.setSocialSecurityIdPassword(""); // Will be set later
            company.setAddress(address);
            company.setPhone(phone);
            company.setEmail(email);

            // Save the company - this will create the database
            Company savedCompany = companyService.createCompany(company);
            System.out.println("Company created successfully: " + savedCompany.getName());

            // Close the dialog
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();

            // Refresh the parent controller - ensure it happens after dialog closes
            if (parentController != null) {
                // Use Platform.runLater to ensure UI updates happen on the JavaFX thread
                Platform.runLater(() -> {
                    try {
                        // Refresh the companies list
                        parentController.refreshCompanies();
                        
                        // Select the newly created company
                        parentController.selectNewlyCreatedCompany(name);
                        
                        System.out.println("Company selection completed for: " + name);
                    } catch (Exception e) {
                        System.out.println("Error during refresh: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }

        } catch (Exception e) {
            showError("Error", "Failed to create company: " + e.getMessage());
            e.printStackTrace();
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
