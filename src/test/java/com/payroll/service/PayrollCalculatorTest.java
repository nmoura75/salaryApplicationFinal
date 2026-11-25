package com.payroll.service;

import com.payroll.dto.PayrollInput;
import com.payroll.dto.PayrollResult;
import com.payroll.dto.RemunerationLine;
import com.payroll.model.Employee;
import com.payroll.model.MaritalStatus;
import com.payroll.model.RemunerationCategory.ContributionRule;
import com.payroll.model.WithholdingTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollCalculatorTest {

    private static final int NO_DEPENDENTS = 0;
    private static final WithholdingTable WITHHOLDING_TABLE = WithholdingTable.TABLE_I;
    private static final MaritalStatus MARITAL_STATUS = MaritalStatus.NOT_MARRIED;

    private final PayrollCalculator calculator = new PayrollCalculator();

    @Test
    void baseSalaryOnlyAppliesSocialSecurityAndNoTaxUpToFirstBracket() {
        PayrollResult result = calculate("1000.00", List.of(), NO_DEPENDENTS);

        assertEquals(money("1000.00"), result.getGross());
        assertEquals(money("1000.00"), result.getSubjectAmount());
        assertEquals(money("110.00"), result.getEmployeeSocialSecurity());
        assertEquals(money("237.50"), result.getEmployerSocialSecurity());
        assertEquals(money("0.00"), result.getIncomeTax());
        assertEquals(money("890.00"), result.getNet());
    }

    @ParameterizedTest(name = "subject {0} -> income tax {1}")
    @CsvSource({
            "1000.00, 0.00",
            "1000.01, 150.00",
            "2500.00, 375.00",
            "2500.01, 625.00",
            "5000.00, 1250.00",
            "5000.01, 1750.00"
    })
    void withholdingRateChangesAtEachBracketBoundary(String subject, String expectedTax) {
        PayrollResult result = calculate(subject, List.of(), NO_DEPENDENTS);

        assertEquals(money(expectedTax), result.getIncomeTax());
    }

    @Test
    void mealAllowanceAboveDailyExemptionIsPartlySubject() {
        RemunerationLine meal = line(ContributionRule.MEAL_DAILY, "160.00", 20);

        PayrollResult result = calculate("1000.00", List.of(meal), NO_DEPENDENTS);

        assertEquals(money("1160.00"), result.getGross());
        assertEquals(money("1040.00"), result.getSubjectAmount());
        assertEquals(money("114.40"), result.getEmployeeSocialSecurity());
        assertEquals(money("156.00"), result.getIncomeTax());
        assertEquals(money("889.60"), result.getNet());
    }

    @Test
    void mealAllowanceWithinDailyExemptionIsNotSubject() {
        RemunerationLine meal = line(ContributionRule.MEAL_DAILY, "100.00", 20);

        PayrollResult result = calculate("1000.00", List.of(meal), NO_DEPENDENTS);

        assertEquals(money("1100.00"), result.getGross());
        assertEquals(money("1000.00"), result.getSubjectAmount());
    }

    @Test
    void mealAllowanceWithoutBusinessDaysIsFullySubject() {
        RemunerationLine meal = line(ContributionRule.MEAL_DAILY, "120.00", null);

        PayrollResult result = calculate("1000.00", List.of(meal), NO_DEPENDENTS);

        assertEquals(money("1120.00"), result.getSubjectAmount());
    }

    @Test
    void exemptLineAddsToGrossButNotToSubject() {
        RemunerationLine exempt = line(ContributionRule.EXEMPT, "200.00", null);

        PayrollResult result = calculate("1000.00", List.of(exempt), NO_DEPENDENTS);

        assertEquals(money("1200.00"), result.getGross());
        assertEquals(money("1000.00"), result.getSubjectAmount());
        assertEquals(money("1090.00"), result.getNet());
    }

    @Test
    void subjectLineAddsToGrossAndSubject() {
        RemunerationLine bonus = line(ContributionRule.SUBJECT, "500.00", null);

        PayrollResult result = calculate("1000.00", List.of(bonus), NO_DEPENDENTS);

        assertEquals(money("1500.00"), result.getGross());
        assertEquals(money("1500.00"), result.getSubjectAmount());
    }

    @Test
    void eachDependentReducesIncomeTax() {
        PayrollResult result = calculate("2000.00", List.of(), 2);

        assertEquals(money("200.00"), result.getIncomeTax());
    }

    @Test
    void dependentDeductionNeverMakesIncomeTaxNegative() {
        PayrollResult result = calculate("1500.00", List.of(), 5);

        assertEquals(money("0.00"), result.getIncomeTax());
    }

    @Test
    void missingSalaryAndLinesProduceZeroAmounts() {
        PayrollInput input = new PayrollInput(null, null, NO_DEPENDENTS, WITHHOLDING_TABLE, MARITAL_STATUS);

        PayrollResult result = calculator.calculate(input);

        assertEquals(money("0.00"), result.getGross());
        assertEquals(money("0.00"), result.getEmployeeSocialSecurity());
        assertEquals(money("0.00"), result.getIncomeTax());
        assertEquals(money("0.00"), result.getNet());
    }

    @Test
    void employeeWithoutStoredDependentsIsCalculatedWithNone() {
        Employee employee = new Employee();
        employee.setBaseSalary(money("2000.00"));
        employee.setDependents(null);

        PayrollResult result = calculator.calculate(PayrollInput.forEmployee(employee, List.of()));

        assertEquals(money("300.00"), result.getIncomeTax());
    }

    private PayrollResult calculate(String baseSalary, List<RemunerationLine> lines, int dependents) {
        PayrollInput input = new PayrollInput(money(baseSalary), lines, dependents, WITHHOLDING_TABLE, MARITAL_STATUS);
        return calculator.calculate(input);
    }

    private static RemunerationLine line(ContributionRule rule, String amount, Integer businessDays) {
        RemunerationLine line = new RemunerationLine();
        line.setContributionRule(rule);
        line.setAmount(money(amount));
        line.setBusinessDays(businessDays);
        return line;
    }

    private static BigDecimal money(String amount) {
        return new BigDecimal(amount);
    }
}
