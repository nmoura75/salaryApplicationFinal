package com.payroll.dto;

import com.payroll.model.Employee;

import java.math.BigDecimal;
import java.util.List;

/**
 * Everything {@link com.payroll.service.PayrollCalculator} needs for one employee and month.
 * {@code irsTable} and {@code familiarSituation} are carried for the official withholding tables.
 */
public record PayrollInput(BigDecimal baseSalary,
                           List<RemunerationLine> remunerationLines,
                           int dependents,
                           Integer irsTable,
                           Integer familiarSituation) {

    public PayrollInput {
        remunerationLines = remunerationLines == null ? List.of() : List.copyOf(remunerationLines);
    }

    public static PayrollInput forEmployee(Employee employee, List<RemunerationLine> remunerationLines) {
        int dependents = employee.getDependents() == null ? 0 : employee.getDependents();
        return new PayrollInput(employee.getBaseSalary(), remunerationLines, dependents,
                employee.getIrsTable(), employee.getFamiliarSituation());
    }
}
