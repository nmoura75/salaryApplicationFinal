package com.payroll.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class StartMenu extends VBox {

    public StartMenu() {
        setSpacing(10);
        setAlignment(Pos.CENTER);

        //Button btnManageEmployees = new Button("Manage Employees");
        Button btnManageCompanies = new Button("Manage Companies");
        //Button btnCalculateSalaries = new Button("Calculate Salaries");

        getChildren().addAll(
                //btnManageEmployees,
                btnManageCompanies
                //btnCalculateSalaries
        );
    }
}