package com.payroll.service;

import com.payroll.dto.PayrollResult;
import com.payroll.dto.RemunerationLine;
import com.payroll.model.RemunerationCategory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Single payroll calculation used by the employee preview and by monthly receipts.
 * Income tax is a temporary withholding method. Official retention tables replace
 * {@link #incomeTax} later. Madeira, Açores, and disability tables are not included.
 */
@Component
public class PayrollCalculator {

    public static final BigDecimal EMPLOYEE_SOCIAL_SECURITY_RATE = new BigDecimal("0.11");
    public static final BigDecimal EMPLOYER_SOCIAL_SECURITY_RATE = new BigDecimal("0.2375");
    public static final BigDecimal MEAL_ALLOWANCE_EXEMPT_PER_DAY = new BigDecimal("6.00");
    public static final BigDecimal DEPENDENT_DEDUCTION = new BigDecimal("50.00");

    /**
     * Calculates gross, social security, income tax, and net for one employee.
     * {@code irsTable} and {@code familiarSituation} are accepted so both screens pass the stored values.
     */
    public PayrollResult calculate(BigDecimal baseSalary, List<RemunerationLine> lines, int dependents,
                                   Integer irsTable, Integer familiarSituation) {
        BigDecimal safeBase = money(baseSalary);
        BigDecimal remunerationTotal = BigDecimal.ZERO;
        BigDecimal subject = safeBase;

        if (lines != null) {
            for (RemunerationLine line : lines) {
                BigDecimal amount = money(line.getAmount());
                remunerationTotal = remunerationTotal.add(amount);
                subject = subject.add(subjectPortion(line, amount));
            }
        }

        subject = money(subject);
        BigDecimal gross = money(safeBase.add(remunerationTotal));
        BigDecimal employeeSocialSecurity = money(subject.multiply(EMPLOYEE_SOCIAL_SECURITY_RATE));
        BigDecimal employerSocialSecurity = money(subject.multiply(EMPLOYER_SOCIAL_SECURITY_RATE));
        BigDecimal tax = incomeTax(subject, dependents, irsTable, familiarSituation);
        BigDecimal net = money(gross.subtract(employeeSocialSecurity).subtract(tax));

        PayrollResult result = new PayrollResult();
        result.setGross(gross);
        result.setSubjectAmount(subject);
        result.setEmployeeSocialSecurity(employeeSocialSecurity);
        result.setEmployerSocialSecurity(employerSocialSecurity);
        result.setIncomeTax(tax);
        result.setNet(net.max(BigDecimal.ZERO));
        return result;
    }

    private BigDecimal subjectPortion(RemunerationLine line, BigDecimal amount) {
        RemunerationCategory.ContributionRule rule = line.getContributionRule();
        if (rule == RemunerationCategory.ContributionRule.EXEMPT) {
            return BigDecimal.ZERO;
        }
        if (rule == RemunerationCategory.ContributionRule.MEAL_DAILY) {
            int days = line.getBusinessDays() == null ? 0 : line.getBusinessDays();
            BigDecimal exempt = MEAL_ALLOWANCE_EXEMPT_PER_DAY.multiply(BigDecimal.valueOf(days));
            return money(amount.subtract(exempt).max(BigDecimal.ZERO));
        }
        return amount;
    }

    /**
     * Temporary withholding. {@code irsTable} and {@code familiarSituation} are unused until official tables are supplied.
     */
    private BigDecimal incomeTax(BigDecimal taxable, int dependents, Integer irsTable, Integer familiarSituation) {
        BigDecimal rate = withholdingRate(taxable);
        BigDecimal deduction = BigDecimal.valueOf(Math.max(dependents, 0)).multiply(DEPENDENT_DEDUCTION);
        return money(taxable.multiply(rate).subtract(deduction).max(BigDecimal.ZERO));
    }

    private BigDecimal withholdingRate(BigDecimal taxable) {
        if (taxable.compareTo(new BigDecimal("1000.00")) <= 0) {
            return BigDecimal.ZERO;
        }
        if (taxable.compareTo(new BigDecimal("2500.00")) <= 0) {
            return new BigDecimal("0.15");
        }
        if (taxable.compareTo(new BigDecimal("5000.00")) <= 0) {
            return new BigDecimal("0.25");
        }
        return new BigDecimal("0.35");
    }

    private static BigDecimal money(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
