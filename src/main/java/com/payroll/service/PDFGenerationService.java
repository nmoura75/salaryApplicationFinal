package com.payroll.service;

import com.payroll.dto.SalaryReceiptReportDTO;
import com.payroll.model.Company;
import com.payroll.model.Employee;
import com.payroll.model.SalaryReceipt;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class PDFGenerationService {

    private final SalaryReceiptService salaryReceiptService;
    private final EmployeeService employeeService;
    private final CompanyService companyService;

    public byte[] generateSalaryReceiptPDF(Integer receiptId) throws JRException, IOException {
        // Get the salary receipt
        SalaryReceipt receipt = salaryReceiptService.getSalaryReceiptById(receiptId);
        
        // Get the employee and company using the employeeId
        Employee employee = employeeService.getEmployeeById(receipt.getEmployeeId());
        Company company = companyService.getCompanyById(employee.getCompanyId());

        // Create the DTO with all the data needed for the report
        SalaryReceiptReportDTO reportData = SalaryReceiptReportDTO.builder()
                .companyName(company.getName())
                .companyTaxId(company.getTaxId())
                .companyAddress(company.getAddress())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .employeeTaxId(employee.getTaxId())
                .month(receipt.getMonth())
                .year(receipt.getYear())
                .baseSalary(receipt.getBaseSalary())
                .overtime(receipt.getOvertime())
                .bonus(receipt.getBonus())
                .mealAllowance(receipt.getMealAllowance())
                .transportAllowance(receipt.getTransportAllowance())
                .otherAllowances(receipt.getOtherAllowances())
                .grossAmount(receipt.getGrossAmount())
                .socialSecurityContribution(receipt.getSocialSecurityContribution())
                .incomeTax(receipt.getIncomeTax())
                .netAmount(receipt.getNetAmount())
                .build();

        return generatePDF(reportData);
    }

    private byte[] generatePDF(SalaryReceiptReportDTO data) throws JRException, IOException {
        // Load the JasperReport template
        InputStream templateStream = new ClassPathResource("reports/salary_receipt.jrxml").getInputStream();
        JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);

        // Create the data source
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(data));

        // Fill the report
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                new HashMap<>(),  // Parameters if needed
                dataSource
        );

        // Export to PDF
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    public byte[] generateBulkSalaryReceiptsPDF(Integer companyId, String month, Integer year)
            throws JRException, IOException {
        // Get all salary receipts for the company and month
        var receipts = salaryReceiptService.getSalaryReceiptsByCompanyAndMonth(companyId, month, year);
        
        // Convert each receipt to DTO
        var reportDataList = receipts.stream()
                .map(receipt -> {
                    // Get the employee using the employeeId
                    Employee employee = employeeService.getEmployeeById(receipt.getEmployeeId());
                    
                    return SalaryReceiptReportDTO.builder()
                            .companyName(companyService.getCompanyById(employee.getCompanyId()).getName())
                            .companyTaxId(companyService.getCompanyById(employee.getCompanyId()).getTaxId())
                            .companyAddress(companyService.getCompanyById(employee.getCompanyId()).getAddress())
                            .employeeName(employee.getFirstName() + " " + employee.getLastName())
                            .employeeTaxId(employee.getTaxId())
                            .month(receipt.getMonth())
                            .year(receipt.getYear())
                            .baseSalary(receipt.getBaseSalary())
                            .overtime(receipt.getOvertime())
                            .bonus(receipt.getBonus())
                            .mealAllowance(receipt.getMealAllowance())
                            .transportAllowance(receipt.getTransportAllowance())
                            .otherAllowances(receipt.getOtherAllowances())
                            .grossAmount(receipt.getGrossAmount())
                            .socialSecurityContribution(receipt.getSocialSecurityContribution())
                            .incomeTax(receipt.getIncomeTax())
                            .netAmount(receipt.getNetAmount())
                            .build();
                })
                .toList();

        // Load the JasperReport template
        InputStream templateStream = new ClassPathResource("reports/salary_receipt.jrxml").getInputStream();
        JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);

        // Create the data source with all receipts
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportDataList);

        // Fill the report
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                new HashMap<>(),  // Parameters if needed
                dataSource
        );

        // Export to PDF
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
} 