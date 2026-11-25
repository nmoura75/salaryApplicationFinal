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
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayrollCalculatorTest {

    private static final int NO_DEPENDENTS = 0;
    private static final WithholdingTable WITHHOLDING_TABLE = WithholdingTable.TABLE_I;
    private static final MaritalStatus MARITAL_STATUS = MaritalStatus.NOT_MARRIED;

    private final PayrollCalculator calculator = new PayrollCalculator();

    @Test
    void baseSalaryKeepsSocialSecuritySeparateFromWithholding() {
        PayrollResult result = calculate("1000.00", List.of(), NO_DEPENDENTS);

        assertEquals(money("1000.00"), result.getGross());
        assertEquals(money("1000.00"), result.getSubjectAmount());
        assertEquals(money("110.00"), result.getEmployeeSocialSecurity());
        assertEquals(money("237.50"), result.getEmployerSocialSecurity());
        assertEquals(money("36.00"), result.getIncomeTax());
        assertEquals(money("854.00"), result.getNet());
    }

    @ParameterizedTest(name = "table {0} at {1} publishes {2}%")
    @CsvSource({
            "TABLE_I, 920.00, 0.0",
            "TABLE_I, 1042.00, 5.3",
            "TABLE_I, 1108.00, 7.2",
            "TABLE_I, 1819.00, 13.5",
            "TABLE_III, 1042.00, 2.2",
            "TABLE_III, 1962.00, 8.5",
            "TABLE_IV, 2063.00, 3.8"
    })
    void publishedEffectiveRateMatchesTheLimitWithNoDependents(WithholdingTable table, String remuneration,
                                                               String publishedPercent) {
        PayrollResult result = calculate(remuneration, List.of(), NO_DEPENDENTS, table);
        BigDecimal percent = result.getIncomeTax()
                .multiply(new BigDecimal("100"))
                .divide(money(remuneration), 1, RoundingMode.HALF_UP);

        assertEquals(new BigDecimal(publishedPercent).setScale(1, RoundingMode.HALF_UP), percent);
    }

    @Test
    void twoDependentsReduceTaxByTwiceTheTableParcel() {
        PayrollResult withoutDependents = calculate("1819.00", List.of(), NO_DEPENDENTS, WithholdingTable.TABLE_II);
        PayrollResult withDependents = calculate("1819.00", List.of(), 2, WithholdingTable.TABLE_II);

        assertEquals(money("245.05"), withoutDependents.getIncomeTax());
        assertEquals(money("176.47"), withDependents.getIncomeTax());
    }

    @Test
    void dependentsDoNotChangeTableIvBelowThree() {
        PayrollResult withoutDependents = calculate("2063.00", List.of(), NO_DEPENDENTS, WithholdingTable.TABLE_IV);
        PayrollResult withDependents = calculate("2063.00", List.of(), 2, WithholdingTable.TABLE_IV);

        assertEquals(money("78.23"), withoutDependents.getIncomeTax());
        assertEquals(withoutDependents.getIncomeTax(), withDependents.getIncomeTax());
    }

    @Test
    void thirdDependentLowersOnlyTheMarginalRate() {
        PayrollResult twoDependents = calculate("2000.00", List.of(), 2);
        PayrollResult threeDependents = calculate("2000.00", List.of(), 3);
        PayrollResult formulaBracket = calculate("1108.00", List.of(), 3);

        assertEquals(money("258.48"), twoDependents.getIncomeTax());
        assertEquals(money("217.05"), threeDependents.getIncomeTax());
        assertEquals(money("3.88"), formulaBracket.getIncomeTax());
    }

    @Test
    void withholdingNeverGoesBelowZero() {
        PayrollResult result = calculate("1042.00", List.of(), 3);

        assertEquals(money("0.00"), result.getIncomeTax());
    }

    @Test
    void mealAllowanceAboveDailyExemptionIsPartlySubject() {
        RemunerationLine meal = line(ContributionRule.MEAL_DAILY, "160.00", 20);

        PayrollResult result = calculate("1000.00", List.of(meal), NO_DEPENDENTS);

        assertEquals(money("1160.00"), result.getGross());
        assertEquals(money("1040.00"), result.getSubjectAmount());
        assertEquals(money("114.40"), result.getEmployeeSocialSecurity());
        assertEquals(money("54.00"), result.getIncomeTax());
        assertEquals(money("991.60"), result.getNet());
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
        assertEquals(money("1054.00"), result.getNet());
    }

    @Test
    void subjectLineAddsToGrossAndSubject() {
        RemunerationLine bonus = line(ContributionRule.SUBJECT, "500.00", null);

        PayrollResult result = calculate("1000.00", List.of(bonus), NO_DEPENDENTS);

        assertEquals(money("1500.00"), result.getGross());
        assertEquals(money("1500.00"), result.getSubjectAmount());
        assertEquals(money("168.17"), result.getIncomeTax());
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
    void missingWithholdingTableIsRejected() {
        PayrollInput input = new PayrollInput(money("1000.00"), List.of(), NO_DEPENDENTS, null, MARITAL_STATUS);

        assertThrows(NullPointerException.class, () -> calculator.calculate(input));
    }

    @Test
    void employeeWithoutStoredDependentsIsCalculatedWithNone() {
        Employee employee = new Employee();
        employee.setBaseSalary(money("2000.00"));
        employee.setDependents(null);
        employee.setWithholdingTable(WITHHOLDING_TABLE);

        PayrollResult result = calculator.calculate(PayrollInput.forEmployee(employee, List.of()));

        assertEquals(money("301.34"), result.getIncomeTax());
    }

    private PayrollResult calculate(String baseSalary, List<RemunerationLine> lines, int dependents) {
        return calculate(baseSalary, lines, dependents, WITHHOLDING_TABLE);
    }

    private PayrollResult calculate(String baseSalary, List<RemunerationLine> lines, int dependents,
                                    WithholdingTable table) {
        PayrollInput input = new PayrollInput(money(baseSalary), lines, dependents, table, MARITAL_STATUS);
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
