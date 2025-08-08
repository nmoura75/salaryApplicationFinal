package com.payroll.controller;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalaryConsultingController {

    @FXML
    private Text companyNameText;

    private String companyName;

    @FXML
    public void initialize() {
        // Initialize the salary consulting screen
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
        if (companyNameText != null) {
            companyNameText.setText(companyName);
        }
    }
}
