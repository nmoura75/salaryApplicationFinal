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

import java.io.File;
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
            // Query companies from the database
            List<Company> companies = companyService.getAllCompanies();
            for (Company company : companies) {
                companyComboBox.getItems().add(company.getName());
            }
            
            System.out.println("Found " + companies.size() + " companies in database");
        } catch (Exception e) {
            System.out.println("Error loading companies from database: " + e.getMessage());
            // Fallback to file-based approach if database query fails
            loadCompaniesFromFiles();
        }
        
        // Set default selection if companies exist
       /* if (!companyComboBox.getItems().isEmpty()) {
            companyComboBox.getSelectionModel().selectFirst();
        }*/
    }

    private void loadCompaniesFromFiles() {
        // Fallback method to load companies from database files
        File dataDir = new File("./data");
        if (dataDir.exists()) {
            File[] dbFiles = dataDir.listFiles((dir, name) -> name.endsWith(".db"));
            if (dbFiles != null) {
                for (File dbFile : dbFiles) {
                    String companyName = dbFile.getName().replace(".db", "");
                    // Only add if not already in the list
                    if (!companyComboBox.getItems().contains(companyName)) {
                        companyComboBox.getItems().add(companyName);
                    }
                }
                System.out.println("Loaded " + dbFiles.length + " companies from files");
            }
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
            // Find the company in the database to get the correct database file name
            Company selectedCompany = null;
            try {
                // Try to find by name first
                selectedCompany = companyService.getAllCompanies().stream()
                    .filter(c -> c.getName().equals(selectedCompanyName))
                    .findFirst()
                    .orElse(null);
            } catch (Exception e) {
                System.out.println("Error finding company by name: " + e.getMessage());
            }

            // If not found in database, try file-based approach
            if (selectedCompany == null) {
                // Find the database file for the selected company
                File dataDir = new File("./data");
                String dbFileName = selectedCompanyName + ".db";
                File dbFile = new File(dataDir, dbFileName);

                if (!dbFile.exists()) {
                    showError("Error", "Company database not found.");
                    return;
                }

                // Set the database URL for this company
                System.setProperty("spring.datasource.url", "jdbc:sqlite:" + dbFile.getAbsolutePath());
            } else {
                // Use the company's database file
                String dbFileName = selectedCompany.getName().replaceAll("[^a-zA-Z0-9]", "_") + ".db";
                File dataDir = new File("./data");
                File dbFile = new File(dataDir, dbFileName);
                System.setProperty("spring.datasource.url", "jdbc:sqlite:" + dbFile.getAbsolutePath());
            }
            
            // Load the main menu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            MainMenuController controller = loader.getController();
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

    public void refreshCompanies() {
        System.out.println("Refreshing companies...");
        
        // Clear the current items
        companyComboBox.getItems().clear();
        
        // First, try to load from database files to ensure we get all companies
        loadCompaniesFromFiles();
        
        // Then try to load from the current database to get additional info
        try {
            List<Company> companies = companyService.getAllCompanies();
            for (Company company : companies) {
                // Only add if not already in the list
                if (!companyComboBox.getItems().contains(company.getName())) {
                    companyComboBox.getItems().add(company.getName());
                }
            }
            
            System.out.println("Found " + companies.size() + " companies in current database");
            
        } catch (Exception e) {
            System.out.println("Error loading companies from current database: " + e.getMessage());
            // The file-based approach already loaded companies, so we're good
        }
        
        // Set default selection if companies exist
        if (!companyComboBox.getItems().isEmpty()) {
            companyComboBox.getSelectionModel().selectFirst();
        }
        
        System.out.println("Total companies loaded: " + companyComboBox.getItems().size());
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
