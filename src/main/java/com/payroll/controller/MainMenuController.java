package com.payroll.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MainMenuController {

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private VBox menuContainer;
    
    @FXML
    private Text companyNameText;
    
    @FXML
    private Button backButton;

    private String companyName;
    private Integer companyId;

    @FXML
    public void initialize() {
        createMenuCards();
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
        if (companyNameText != null) {
            companyNameText.setText(companyName);
        }
    }
    
    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    private void createMenuCards() {
        menuContainer.getChildren().clear();
        
        // Define menu items
        List<MenuCard> menuCards = new ArrayList<>();
        menuCards.add(new MenuCard("👥", "Employees", "Manage employees", this::openEmployeesMenu));
        menuCards.add(new MenuCard("📊", "Salary Preparation", "Prepare monthly salaries", this::openSalaryPreparation));
        menuCards.add(new MenuCard("💰", "Salary Processing", "Process and generate receipts", this::openSalaryProcessing));
        menuCards.add(new MenuCard("📋", "Salary Consulting", "View salary reports", this::openSalaryConsulting));
        menuCards.add(new MenuCard("🏛️", "Government Maps", "Generate government reports", this::openGovernmentMaps));
        
        // Create horizontal rows with max 4 cards per row
        int maxCardsPerRow = 4;
        for (int i = 0; i < menuCards.size(); i += maxCardsPerRow) {
            HBox row = new HBox(20);
            row.setAlignment(javafx.geometry.Pos.CENTER);
            
            int endIndex = Math.min(i + maxCardsPerRow, menuCards.size());
            for (int j = i; j < endIndex; j++) {
                MenuCard menuCard = menuCards.get(j);
                VBox card = createMenuCard(menuCard.icon, menuCard.title, menuCard.description, menuCard.action);
                row.getChildren().add(card);
            }
            
            menuContainer.getChildren().add(row);
        }
    }

    private VBox createMenuCard(String icon, String title, String description, Runnable action) {
        VBox card = new VBox();
        card.getStyleClass().addAll("menu-card");
        card.setMaxWidth(250);
        card.setPrefWidth(250);
        card.setMaxHeight(180);
        card.setPrefHeight(180);
        
        VBox content = new VBox(10);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        
        Text iconText = new Text(icon);
        iconText.getStyleClass().add("menu-icon");
        
        Text titleText = new Text(title);
        titleText.getStyleClass().add("menu-title");
        
        Text descText = new Text(description);
        descText.getStyleClass().add("menu-description");
        descText.setWrappingWidth(200);
        
        content.getChildren().addAll(iconText, titleText, descText);
        card.getChildren().add(content);
        
        card.setOnMouseClicked(e -> action.run());
        
        return card;
    }
    
    @FXML
    private void goBackToCompanySelection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CompanySelection.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/ios-style.css").toExternalForm());
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Salary Calculator - Company Selection");
            
        } catch (Exception e) {
            showError("Error", "Failed to return to company selection: " + e.getMessage());
        }
    }

    private void openEmployeesMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EmployeesMenu.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            EmployeesMenuController controller = loader.getController();
            controller.setCompanyName(companyName);
            controller.setCompanyId(companyId);
            
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/ios-style.css").toExternalForm());
            
            Stage stage = (Stage) menuContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Employees - " + companyName);
            
        } catch (Exception e) {
            showError("Error", "Failed to open employees menu: " + e.getMessage());
        }
    }

    private void openSalaryPreparation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SalaryPreparation.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            SalaryPreparationController controller = loader.getController();
            controller.setCompanyName(companyName);
            
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/ios-style.css").toExternalForm());
            
            Stage stage = (Stage) menuContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Salary Preparation - " + companyName);
            
        } catch (Exception e) {
            showError("Error", "Failed to open salary preparation: " + e.getMessage());
        }
    }

    private void openSalaryProcessing() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SalaryProcessing.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            SalaryProcessingController controller = loader.getController();
            controller.setCompanyName(companyName);
            
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/ios-style.css").toExternalForm());
            
            Stage stage = (Stage) menuContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Salary Processing - " + companyName);
            
        } catch (Exception e) {
            showError("Error", "Failed to open salary processing: " + e.getMessage());
        }
    }

    private void openSalaryConsulting() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SalaryConsulting.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            SalaryConsultingController controller = loader.getController();
            controller.setCompanyName(companyName);
            
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/ios-style.css").toExternalForm());
            
            Stage stage = (Stage) menuContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Salary Consulting - " + companyName);
            
        } catch (Exception e) {
            showError("Error", "Failed to open salary consulting: " + e.getMessage());
        }
    }

    private void openGovernmentMaps() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GovernmentMaps.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            GovernmentMapsController controller = loader.getController();
            controller.setCompanyName(companyName);
            
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/ios-style.css").toExternalForm());
            
            Stage stage = (Stage) menuContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Government Maps - " + companyName);
            
        } catch (Exception e) {
            showError("Error", "Failed to open government maps: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Inner class to represent menu cards
    private static class MenuCard {
        final String icon;
        final String title;
        final String description;
        final Runnable action;
        
        MenuCard(String icon, String title, String description, Runnable action) {
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.action = action;
        }
    }
}
