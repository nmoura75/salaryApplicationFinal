package com.payroll.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GovernmentMapsController {

    private final ScreenNavigator screenNavigator;

    @FXML
    private Button backButton;

    @FXML
    private Label companyNameText;

    private String companyName;
    private Integer companyId;

    @FXML
    public void initialize() {
        // Reports are not available yet.
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

    @FXML
    private void goBackToMainMenu() {
        screenNavigator.showMainMenu(backButton, companyId, companyName);
    }
}
