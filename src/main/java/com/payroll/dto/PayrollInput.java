package com.payroll.dto;

import com.payroll.model.Employee;
import com.payroll.model.MaritalStatus;
import com.payroll.model.WithholdingTable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Everything {@link com.payroll.service.PayrollCalculator} needs for one employee and month.
 * {@code withholdingTable} and {@code maritalStatus} are carried for the official withholding tables.
 */
public record PayrollInput(BigDecimal baseSalary,
                           List<RemunerationLine> remunerationLines,
                           int dependents,
                           WithholdingTable withholdingTable,
                           MaritalStatus maritalStatus) {

    public PayrollInput {
        remunerationLines = remunerationLines == null ? List.of() : List.copyOf(remunerationLines);
    }

    public static PayrollInput forEmployee(Employee employee, List<RemunerationLine> remunerationLines) {
        int dependents = employee.getDependents() == null ? 0 : employee.getDependents();
        return new PayrollInput(employee.getBaseSalary(), remunerationLines, dependents,
                employee.getWithholdingTable(), employee.getMaritalStatus());
    }
}
