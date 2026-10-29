package com.payroll.controller;

import com.payroll.model.Company;
import com.payroll.controller.ScreenNavigator.WindowSize;
import com.payroll.service.CompanyService;
import jakarta.persistence.EntityNotFoundException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CompanySelectionController {

    private static final WindowSize CREATE_COMPANY_DIALOG = new WindowSize(600, 700);

    private final CompanyService companyService;
    private final ScreenNavigator screenNavigator;

    @FXML
    private ComboBox<String> companyComboBox;

    @FXML
    public void initialize() {
        loadCompanies();
    }

    private void loadCompanies() {
        companyComboBox.getItems().clear();
        try {
            List<Company> companies = companyService.getAllCompanies();
            for (Company company : companies) {
                companyComboBox.getItems().add(company.getName());
            }
        } catch (IllegalStateException exception) {
            showError("Error", "Failed to load companies: " + exception.getMessage());
        }
    }

    @FXML
    private void openSelectedCompany() {
        String selectedCompanyName = companyComboBox.getValue();
        if (selectedCompanyName == null || selectedCompanyName.isEmpty()) {
            showError("Selection Error", "Please select a company first.");
            return;
        }

        Company selectedCompany;
        try {
            selectedCompany = companyService.openCompany(selectedCompanyName);
        } catch (IllegalArgumentException | IllegalStateException | EntityNotFoundException exception) {
            showError("Error", "Failed to open company: " + exception.getMessage());
            return;
        }
        screenNavigator.showMainMenu(companyComboBox, selectedCompany.getId(), selectedCompanyName);
    }

    @FXML
    private void showCreateCompanyDialog() {
        screenNavigator.<CreateCompanyDialogController>showModalDialog(companyComboBox,
                "/fxml/CreateCompanyDialog.fxml", "Create New Company", CREATE_COMPANY_DIALOG,
                controller -> controller.setParentController(this));
    }

    @FXML
    private void deleteSelectedCompany() {
        String selectedCompanyName = companyComboBox.getValue();
        if (selectedCompanyName == null || selectedCompanyName.isEmpty()) {
            showError("Selection Error", "Please select a company first.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Company");
        alert.setHeaderText(null);
        alert.setContentText("Delete " + selectedCompanyName + " and its database file?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    companyService.deleteCompanyByName(selectedCompanyName);
                    refreshCompanies();
                } catch (IllegalArgumentException | IllegalStateException exception) {
                    showError("Error", "Failed to delete company: " + exception.getMessage());
                }
            }
        });
    }

    public void refreshCompanies() {
        loadCompanies();
    }

    public void selectNewlyCreatedCompany(String companyName) {
        System.out.println("Attempting to select newly created company: " + companyName);
        
        // Find and select the newly created company in the dropdown
        for (String item : companyComboBox.getItems()) {
            if (item.equals(companyName)) {
                companyComboBox.setValue(item);
                System.out.println("Successfully selected company: " + companyName);
                return;
            }
        }
        
        System.out.println("Company not found in dropdown: " + companyName);
        System.out.println("Available companies: " + companyComboBox.getItems());
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
