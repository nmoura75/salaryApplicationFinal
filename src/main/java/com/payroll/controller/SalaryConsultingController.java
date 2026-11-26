package com.payroll.controller;

import com.payroll.model.Employee;
import com.payroll.model.SalaryReceipt;
import com.payroll.service.EmployeeService;
import com.payroll.service.PDFGenerationService;
import com.payroll.service.SalaryReceiptService;
import jakarta.persistence.EntityNotFoundException;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SalaryConsultingController {

    private final SalaryReceiptService salaryReceiptService;
    private final EmployeeService employeeService;
    private final PDFGenerationService pdfGenerationService;
    private final ScreenNavigator screenNavigator;

    @FXML
    private Button backButton;
    @FXML
    private Label companyNameText;
    @FXML
    private TextField yearField;
    @FXML
    private ComboBox<Integer> monthComboBox;
    @FXML
    private TableView<ReceiptRow> receiptsTable;
    @FXML
    private TableColumn<ReceiptRow, String> employeeColumn;
    @FXML
    private TableColumn<ReceiptRow, String> grossColumn;
    @FXML
    private TableColumn<ReceiptRow, String> netColumn;
    @FXML
    private TableColumn<ReceiptRow, String> taxColumn;
    @FXML
    private Text statusText;

    private String companyName;
    private Integer companyId;

    @FXML
    public void initialize() {
        YearMonth currentMonth = YearMonth.now();
        yearField.setText(String.valueOf(currentMonth.getYear()));
        monthComboBox.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        monthComboBox.setValue(currentMonth.getMonthValue());
        employeeColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getEmployeeName()));
        grossColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getGross()));
        netColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getNet()));
        taxColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getTax()));
        styleMoneyColumn(grossColumn);
        styleMoneyColumn(netColumn);
        styleMoneyColumn(taxColumn);
    }

    private void styleMoneyColumn(TableColumn<ReceiptRow, String> column) {
        column.setCellFactory(col -> new TableCell<>() {
            {
                getStyleClass().add("money-cell");
                setAlignment(Pos.CENTER_RIGHT);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
            }
        });
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
        if (companyNameText != null) {
            companyNameText.setText(companyName);
        }
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
        loadReceipts();
    }

    @FXML
    private void loadReceipts() {
        if (companyId == null) {
            return;
        }
        try {
            List<SalaryReceipt> receipts = salaryReceiptService.getSalaryReceiptsByCompanyAndMonth(
                    companyId, String.valueOf(monthComboBox.getValue()), Integer.parseInt(yearField.getText().trim()));
            List<ReceiptRow> rows = new ArrayList<>();
            for (SalaryReceipt receipt : receipts) {
                Employee employee = employeeService.getEmployeeById(receipt.getEmployeeId());
                rows.add(new ReceiptRow(receipt, employee.getFirstName() + " " + employee.getLastName()));
            }
            receiptsTable.getItems().setAll(rows);
            statusText.setText(rows.size() + " receipt(s).");
        } catch (NumberFormatException exception) {
            showError("Error", "Enter a valid year.");
        } catch (EntityNotFoundException exception) {
            showError("Error", "Could not load receipts: " + exception.getMessage());
        }
    }

    @FXML
    private void saveSelectedPdf() {
        ReceiptRow row = receiptsTable.getSelectionModel().getSelectedItem();
        if (row == null) {
            showError("Error", "Select a receipt first.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("receipt-" + row.getReceiptId() + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = chooser.showSaveDialog(backButton.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            Files.write(file.toPath(), pdfGenerationService.generateSalaryReceiptPDF(row.getReceiptId()));
            statusText.setText("Saved " + file.getName());
        } catch (JRException | IOException | EntityNotFoundException exception) {
            showError("Error", "Could not write the PDF: " + exception.getMessage());
        }
    }

    @FXML
    private void saveMonthPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("receipts-" + yearField.getText().trim() + "-" + monthComboBox.getValue() + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = chooser.showSaveDialog(backButton.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            byte[] pdf = pdfGenerationService.generateBulkSalaryReceiptsPDF(
                    companyId, String.valueOf(monthComboBox.getValue()), Integer.parseInt(yearField.getText().trim()));
            Files.write(file.toPath(), pdf);
            statusText.setText("Saved " + file.getName());
        } catch (NumberFormatException exception) {
            showError("Error", "Enter a valid year.");
        } catch (JRException | IOException | EntityNotFoundException exception) {
            showError("Error", "Could not write the PDF: " + exception.getMessage());
        }
    }

    @FXML
    private void goBackToMainMenu() {
        screenNavigator.showMainMenu(backButton, companyId, companyName);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Getter
    public static class ReceiptRow {
        private final Integer receiptId;
        private final String employeeName;
        private final String gross;
        private final String net;
        private final String tax;

        public ReceiptRow(SalaryReceipt receipt, String employeeName) {
            this.receiptId = receipt.getId();
            this.employeeName = employeeName;
            this.gross = receipt.getGrossAmount().toPlainString();
            this.net = receipt.getNetAmount().toPlainString();
            this.tax = receipt.getIncomeTax().toPlainString();
        }
    }
}
