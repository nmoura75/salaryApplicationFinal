package com.payroll;

import com.payroll.dto.PayrollInput;
import com.payroll.dto.PayrollResult;
import com.payroll.dto.RemunerationLine;
import com.payroll.model.Company;
import com.payroll.model.Employee;
import com.payroll.model.MaritalStatus;
import com.payroll.model.RemunerationCategory;
import com.payroll.model.SalaryReceipt;
import com.payroll.service.CompanyService;
import com.payroll.service.EmployeeService;
import com.payroll.service.PayrollCalculator;
import com.payroll.service.RemunerationService;
import com.payroll.service.SalaryReceiptService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PayrollFlowTest {

    private static Path dataDirectory;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private RemunerationService remunerationService;

    @Autowired
    private SalaryReceiptService salaryReceiptService;

    @DynamicPropertySource
    static void dataDirectory(DynamicPropertyRegistry registry) throws IOException {
        dataDirectory = Files.createTempDirectory("payroll-test");
        registry.add("payroll.data-directory", dataDirectory::toString);
    }

    @AfterEach
    void closeCompany() {
        companyService.closeCompany();
    }

    @AfterAll
    static void deleteDataDirectory() throws IOException {
        if (dataDirectory == null || !Files.exists(dataDirectory)) {
            return;
        }
        try (var paths = Files.walk(dataDirectory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    // The test result does not depend on deleting the temporary directory.
                }
            });
        }
    }

    @Test
    void mealAllowanceUsesStoredBusinessDays() {
        Company company = openNewCompany("Meal Company", "100000001", "SS100000001");
        Employee employee = employeeService.createEmployee(employee(company.getId(), "Ana", "100000001", "11911111100"));
        RemunerationCategory meal = category(company.getId(), "Subsídio de refeição");
        remunerationService.setEmployeeRemuneration(employee.getId(), meal.getId(), new BigDecimal("6.00"), null);
        remunerationService.setMonthlyBusinessDays(company.getId(), 2026, 3, 20);

        List<RemunerationLine> lines = remunerationService.calculateLines(employee.getId(), company.getId(), 2026, 3);
        RemunerationLine mealLine = lines.stream()
                .filter(line -> "Subsídio de refeição".equals(line.getCategoryName()))
                .findFirst()
                .orElseThrow();

        assertEquals(new BigDecimal("120.00"), mealLine.getAmount());
        assertEquals(20, mealLine.getBusinessDays());
    }

    @Test
    void processedMonthUsesTheSharedCalculator() {
        Company company = openNewCompany("Receipt Company", "100000002", "SS100000002");
        Employee employee = employeeService.createEmployee(employee(company.getId(), "Bruno", "100000002", "11911111200"));
        RemunerationCategory meal = category(company.getId(), "Subsídio de refeição");
        remunerationService.setEmployeeRemuneration(employee.getId(), meal.getId(), new BigDecimal("6.00"), null);
        remunerationService.setMonthlyBusinessDays(company.getId(), 2026, 3, 20);

        List<RemunerationLine> lines = remunerationService.calculateLines(employee.getId(), company.getId(), 2026, 3);
        PayrollResult expected = new PayrollCalculator().calculate(PayrollInput.forEmployee(employee, lines));
        SalaryReceipt receipt = salaryReceiptService.createMonthlyReceipt(employee.getId(), 2026, 3);

        assertEquals(expected.getEmployeeSocialSecurity(), receipt.getSocialSecurityContribution());
        assertEquals(expected.getEmployerSocialSecurity(), receipt.getEmployerSocialSecurityContribution());
        assertEquals(expected.getIncomeTax(), receipt.getIncomeTax());
        assertEquals(expected.getNet(), receipt.getNetAmount());
        assertEquals(new BigDecimal("120.00"), receipt.getMealAllowance());
    }

    @Test
    void employeeInOneCompanyIsNotVisibleInAnother() {
        Company first = openNewCompany("Company A", "100000003", "SS100000003");
        employeeService.createEmployee(employee(first.getId(), "Carla", "100000003", "11911111300"));
        companyService.closeCompany();

        openNewCompany("Company B", "100000004", "SS100000004");

        assertTrue(employeeService.getAllEmployees().isEmpty());
    }

    private Company openNewCompany(String name, String taxId, String socialSecurityId) {
        Company company = new Company();
        company.setName(name);
        company.setTaxId(taxId);
        company.setSocialSecurityId(socialSecurityId);
        companyService.createCompany(company);
        return companyService.openCompany(name);
    }

    private Employee employee(Integer companyId, String firstName, String taxId, String socialSecurityId) {
        Employee employee = new Employee();
        employee.setCompanyId(companyId);
        employee.setFirstName(firstName);
        employee.setLastName("Silva");
        employee.setTaxId(taxId);
        employee.setSocialSecurityId(socialSecurityId);
        employee.setHireDate("2024-01-01");
        employee.setBaseSalary(new BigDecimal("1000.00"));
        employee.setMaritalStatus(MaritalStatus.NOT_MARRIED);
        employee.setUsefulDaysOfWork(22);
        employee.setDependents(0);
        return employee;
    }

    private RemunerationCategory category(Integer companyId, String name) {
        return remunerationService.getCategoriesForCompany(companyId).stream()
                .filter(category -> name.equals(category.getName()))
                .findFirst()
                .orElseThrow();
    }
}
