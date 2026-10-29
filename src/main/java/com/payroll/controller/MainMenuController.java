package com.payroll.controller;

import com.payroll.service.CompanyService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MainMenuController {

    private final CompanyService companyService;
    private final ScreenNavigator screenNavigator;

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
        companyService.closeCompany();
        screenNavigator.showScreen(backButton, "/fxml/CompanySelection.fxml",
                "Salary Calculator - Company Selection", controller -> { });
    }

    private void openEmployeesMenu() {
        screenNavigator.<EmployeesMenuController>showScreen(menuContainer, "/fxml/EmployeesMenu.fxml",
                "Employees - " + companyName, controller -> {
                    controller.setCompanyId(companyId);
                    controller.setCompanyName(companyName);
                });
    }

    private void openSalaryPreparation() {
        screenNavigator.<SalaryPreparationController>showScreen(menuContainer, "/fxml/SalaryPreparation.fxml",
                "Salary Preparation - " + companyName, controller -> {
                    controller.setCompanyId(companyId);
                    controller.setCompanyName(companyName);
                });
    }

    private void openSalaryProcessing() {
        screenNavigator.<SalaryProcessingController>showScreen(menuContainer, "/fxml/SalaryProcessing.fxml",
                "Salary Processing - " + companyName, controller -> {
                    controller.setCompanyId(companyId);
                    controller.setCompanyName(companyName);
                });
    }

    private void openSalaryConsulting() {
        screenNavigator.<SalaryConsultingController>showScreen(menuContainer, "/fxml/SalaryConsulting.fxml",
                "Salary Consulting - " + companyName, controller -> {
                    controller.setCompanyId(companyId);
                    controller.setCompanyName(companyName);
                });
    }

    private void openGovernmentMaps() {
        screenNavigator.<GovernmentMapsController>showScreen(menuContainer, "/fxml/GovernmentMaps.fxml",
                "Government Maps - " + companyName, controller -> {
                    controller.setCompanyId(companyId);
                    controller.setCompanyName(companyName);
                });
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
