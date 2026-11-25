package com.payroll.service;

import com.payroll.dto.PayrollInput;
import com.payroll.dto.PayrollResult;
import com.payroll.dto.RemunerationLine;
import com.payroll.model.RemunerationCategory.ContributionRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Single payroll calculation used by the employee preview and by monthly receipts.
 * Income tax follows the 2026 Continente withholding tables. Madeira, Açores,
 * and pension tables are not included.
 */
@Component
public class PayrollCalculator {

    private final WithholdingTables withholdingTables = WithholdingTables.continente2026();

    public PayrollResult calculate(PayrollInput input) {
        BigDecimal baseSalary = money(input.baseSalary());
        List<RemunerationLine> lines = input.remunerationLines();

        BigDecimal gross = money(baseSalary.add(totalAmount(lines)));
        BigDecimal subject = money(baseSalary.add(totalSubjectPortion(lines)));
        BigDecimal employeeSocialSecurity = money(subject.multiply(PayrollConstants.EMPLOYEE_SOCIAL_SECURITY_RATE));
        BigDecimal employerSocialSecurity = money(subject.multiply(PayrollConstants.EMPLOYER_SOCIAL_SECURITY_RATE));
        BigDecimal tax = incomeTax(subject, input);
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

    private BigDecimal totalAmount(List<RemunerationLine> lines) {
        return lines.stream()
                .map(line -> money(line.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalSubjectPortion(List<RemunerationLine> lines) {
        return lines.stream()
                .map(this::subjectPortion)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal subjectPortion(RemunerationLine line) {
        BigDecimal amount = money(line.getAmount());
        ContributionRule rule = line.getContributionRule();
        if (rule == ContributionRule.EXEMPT) {
            return BigDecimal.ZERO;
        }
        if (rule == ContributionRule.MEAL_DAILY) {
            int days = line.getBusinessDays() == null ? 0 : line.getBusinessDays();
            BigDecimal exempt = PayrollConstants.MEAL_ALLOWANCE_EXEMPT_PER_DAY.multiply(BigDecimal.valueOf(days));
            return money(amount.subtract(exempt).max(BigDecimal.ZERO));
        }
        return amount;
    }

    private BigDecimal incomeTax(BigDecimal subject, PayrollInput input) {
        return withholdingTables.withhold(input.withholdingTable(), subject, input.dependents());
    }

    private static BigDecimal money(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
