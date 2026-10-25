package com.payroll.controller;

import com.payroll.model.Company;
import com.payroll.service.CompanyService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CompanySelectionController {

    private final CompanyService companyService;
    
    @Autowired
    private ApplicationContext applicationContext;

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
        } catch (Exception exception) {
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

        try {
            Company selectedCompany = companyService.openCompany(selectedCompanyName);
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            MainMenuController controller = loader.getController();
            controller.setCompanyId(selectedCompany.getId());
            controller.setCompanyName(selectedCompanyName);
            
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/ios-style.css").toExternalForm());
            
            Stage stage = (Stage) companyComboBox.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Salary Calculator - " + selectedCompanyName);
            
        } catch (Exception e) {
            showError("Error", "Failed to open company: " + e.getMessage());
        }
    }

    @FXML
    private void showCreateCompanyDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateCompanyDialog.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 600, 700);
            scene.getStylesheets().add(getClass().getResource("/css/ios-style.css").toExternalForm());
            
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(companyComboBox.getScene().getWindow());
            dialog.setTitle("Create New Company");
            dialog.setScene(scene);
            dialog.setMinWidth(600);
            dialog.setMinHeight(700);
            dialog.setResizable(true);
            
            CreateCompanyDialogController controller = loader.getController();
            controller.setParentController(this);
            
            dialog.showAndWait();
            
        } catch (Exception e) {
            showError("Error", "Failed to open create company dialog: " + e.getMessage());
        }
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
                } catch (Exception exception) {
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
