package com.payroll.service;

import com.payroll.i18n.MessageKeys;
import com.payroll.model.BankDetails;
import com.payroll.model.Company;
import com.payroll.model.ContractType;
import com.payroll.model.Employee;
import com.payroll.model.EmploymentStatus;
import com.payroll.model.IncomeHolders;
import com.payroll.model.MaritalStatus;
import com.payroll.model.RemunerationCategory;
import com.payroll.model.TerminationReason;
import com.payroll.model.WithholdingTable;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class EmployeeServiceTest {

    private static Path dataDirectory;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private RemunerationService remunerationService;

    @DynamicPropertySource
    static void dataDirectory(DynamicPropertyRegistry registry) throws IOException {
        dataDirectory = Files.createTempDirectory("payroll-employee-test");
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
    void fixedTermWithoutEndDateIsRejected() {
        Company company = openNewCompany("Fixed Term Company", "200000001", "SS200000001");
        Employee employee = employee(company.getId(), "200000001", "11911111201");
        employee.setContractType(ContractType.FIXED_TERM);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employeeService.createEmployee(employee));

        assertEquals(MessageKeys.FIXED_TERM_NEEDS_END_DATE, exception.getMessage());
    }

    @Test
    void inactiveEmployeeNeedsTerminationDateAndReason() {
        Company company = openNewCompany("Inactive Company", "200000002", "SS200000002");
        Employee missingDate = employee(company.getId(), "200000002", "11911111202");
        missingDate.setStatus(EmploymentStatus.INACTIVE);
        missingDate.setTerminationReason(TerminationReason.RESIGNATION);

        IllegalArgumentException missingDateError = assertThrows(IllegalArgumentException.class,
                () -> employeeService.createEmployee(missingDate));
        assertEquals(MessageKeys.INACTIVE_NEEDS_TERMINATION, missingDateError.getMessage());

        Employee missingReason = employee(company.getId(), "200000012", "11911111212");
        missingReason.setStatus(EmploymentStatus.INACTIVE);
        missingReason.setTerminationDate("2026-03-01");

        IllegalArgumentException missingReasonError = assertThrows(IllegalArgumentException.class,
                () -> employeeService.createEmployee(missingReason));
        assertEquals(MessageKeys.INACTIVE_NEEDS_TERMINATION, missingReasonError.getMessage());
    }

    @Test
    void terminationDateBeforeHireDateIsRejected() {
        Company company = openNewCompany("Dates Company", "200000003", "SS200000003");
        Employee employee = employee(company.getId(), "200000003", "11911111203");
        employee.setStatus(EmploymentStatus.INACTIVE);
        employee.setTerminationDate("2023-12-31");
        employee.setTerminationReason(TerminationReason.RESIGNATION);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> employeeService.createEmployee(employee));

        assertEquals(MessageKeys.TERMINATION_BEFORE_HIRE, exception.getMessage());
    }

    @Test
    void savedEmployeeKeepsDetailsAndDerivedWithholdingTable() {
        Company company = openNewCompany("Details Company", "200000004", "SS200000004");
        Employee employee = employee(company.getId(), "200000004", "11911111204");
        employee.setMaritalStatus(MaritalStatus.MARRIED);
        employee.setIncomeHolders(IncomeHolders.SINGLE_HOLDER);
        employee.setDependents(2);
        employee.setAddress("Rua das Flores 10");
        employee.setNationality("Portuguesa");
        employee.getBankDetails().setIban("PT50000201231234567890154");
        employee.getBankDetails().setBankName("Banco");

        Employee saved = employeeService.createEmployee(employee);
        Employee reloaded = employeeService.getEmployeeById(saved.getId());

        assertEquals(WithholdingTable.TABLE_III, reloaded.getWithholdingTable());
        assertEquals("Rua das Flores 10", reloaded.getAddress());
        assertEquals("Portuguesa", reloaded.getNationality());
        assertEquals("PT50000201231234567890154", reloaded.getBankDetails().getIban());
        assertEquals(ContractType.PERMANENT, reloaded.getContractType());

        reloaded.setMaritalStatus(MaritalStatus.NOT_MARRIED);
        reloaded.setDependents(1);
        reloaded.setAddress("Rua Nova 2");
        Employee updated = employeeService.updateEmployee(reloaded.getId(), reloaded);

        assertEquals(WithholdingTable.TABLE_II, updated.getWithholdingTable());
        assertEquals("Rua Nova 2", updated.getAddress());
    }

    @Test
    void taxNumberAndSocialSecurityNumberFollowTheDigitRules() {
        Company company = openNewCompany("Identity Company", "200000006", "SS200000006");
        Employee shortNif = employee(company.getId(), "1234567", "11911111206");

        IllegalArgumentException nifError = assertThrows(IllegalArgumentException.class,
                () -> employeeService.createEmployee(shortNif));
        assertEquals(MessageKeys.INVALID_NIF, nifError.getMessage());

        Employee shortNiss = employee(company.getId(), "12345678", "119111112");
        IllegalArgumentException nissError = assertThrows(IllegalArgumentException.class,
                () -> employeeService.createEmployee(shortNiss));
        assertEquals(MessageKeys.INVALID_NISS, nissError.getMessage());

        Employee spaced = employee(company.getId(), "1234 5678", "119 1111 1207");
        Employee saved = employeeService.createEmployee(spaced);

        assertEquals("12345678", saved.getTaxId());
        assertEquals("11911111207", saved.getSocialSecurityId());
    }

    @Test
    void standardIncomeTypesAreSeededOnce() {
        Company company = openNewCompany("Catalogue Company", "200000005", "SS200000005");

        List<RemunerationCategory> categories = remunerationService.getCategoriesForCompany(company.getId());
        RemunerationCategory meal = categories.stream()
                .filter(category -> StandardRemunerations.MEAL_ALLOWANCE.equals(category.getName()))
                .findFirst()
                .orElseThrow();

        assertEquals(StandardRemunerations.types().size(), categories.size());
        assertEquals("R", meal.getSocialSecurityCode());
        assertEquals(RemunerationCategory.ContributionRule.MEAL_DAILY, meal.getContributionRule());

        remunerationService.ensureStandardTypes(company.getId());

        assertEquals(StandardRemunerations.types().size(),
                remunerationService.getCategoriesForCompany(company.getId()).size());
    }

    private Company openNewCompany(String name, String taxId, String socialSecurityId) {
        Company company = new Company();
        company.setName(name);
        company.setTaxId(taxId);
        company.setSocialSecurityId(socialSecurityId);
        companyService.createCompany(company);
        return companyService.openCompany(name);
    }

    private Employee employee(Integer companyId, String taxId, String socialSecurityId) {
        Employee employee = new Employee();
        employee.setCompanyId(companyId);
        employee.setFirstName("Ana");
        employee.setLastName("Silva");
        employee.setTaxId(taxId);
        employee.setSocialSecurityId(socialSecurityId);
        employee.setHireDate("2024-01-01");
        employee.setBaseSalary(new BigDecimal("1000.00"));
        employee.setMaritalStatus(MaritalStatus.NOT_MARRIED);
        employee.setBankDetails(new BankDetails());
        return employee;
    }
}
