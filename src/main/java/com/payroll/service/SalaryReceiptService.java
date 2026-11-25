package com.payroll.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payroll.dto.PayrollInput;
import com.payroll.dto.PayrollResult;
import com.payroll.dto.ProcessMonthResult;
import com.payroll.dto.RemunerationLine;
import com.payroll.model.Employee;
import com.payroll.model.EmploymentStatus;
import com.payroll.model.SalaryReceipt;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.SalaryReceiptRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalaryReceiptService {

    private final SalaryReceiptRepository salaryReceiptRepository;
    private final EmployeeRepository employeeRepository;
    private final RemunerationService remunerationService;
    private final PayrollCalculator payrollCalculator;
    private final ObjectMapper objectMapper;

    /**
     * Creates one receipt from the employee's remuneration lines for the month.
     * Month is stored as the calendar month number, from 1 to 12.
     */
    @Transactional
    public SalaryReceipt createMonthlyReceipt(Integer employeeId, int year, int month) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        String monthText = String.valueOf(month);
        if (salaryReceiptRepository.existsByEmployeeIdAndMonthAndYear(employeeId, monthText, year)) {
            throw new IllegalStateException("Salary receipt already exists for this month");
        }
        return salaryReceiptRepository.save(buildReceipt(employee, year, month));
    }

    /**
     * Creates receipts for active employees. Employees who already have a receipt for the month are refused.
     */
    @Transactional
    public ProcessMonthResult processMonth(Integer companyId, int year, int month) {
        ProcessMonthResult result = new ProcessMonthResult();
        String monthText = String.valueOf(month);
        for (Employee employee : employeeRepository.findByCompanyId(companyId)) {
            if (!isActive(employee)) {
                continue;
            }
            if (salaryReceiptRepository.existsByEmployeeIdAndMonthAndYear(employee.getId(), monthText, year)) {
                result.getRefusedEmployeeNames().add(employee.getFirstName() + " " + employee.getLastName());
                continue;
            }
            result.getCreated().add(salaryReceiptRepository.save(buildReceipt(employee, year, month)));
        }
        return result;
    }

    public SalaryReceipt getSalaryReceiptById(Integer id) {
        return salaryReceiptRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Salary receipt not found"));
    }

    public List<SalaryReceipt> getSalaryReceiptsByEmployee(Integer employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new EntityNotFoundException("Employee not found");
        }
        return salaryReceiptRepository.findByEmployeeId(employeeId);
    }

    public List<SalaryReceipt> getSalaryReceiptsByCompanyAndMonth(Integer companyId, String month, Integer year) {
        return salaryReceiptRepository.findByCompanyIdAndMonthAndYear(companyId, month, year);
    }

    public Page<SalaryReceipt> getSalaryReceiptsByCompany(Integer companyId, Pageable pageable) {
        return salaryReceiptRepository.findByCompanyId(companyId, pageable);
    }

    private SalaryReceipt buildReceipt(Employee employee, int year, int month) {
        List<RemunerationLine> lines = remunerationService.calculateLines(
                employee.getId(), employee.getCompanyId(), year, month);
        PayrollResult result = payrollCalculator.calculate(PayrollInput.forEmployee(employee, lines));

        SalaryReceipt receipt = new SalaryReceipt();
        receipt.setEmployeeId(employee.getId());
        receipt.setPaymentDate(LocalDate.now().toString());
        receipt.setMonth(String.valueOf(month));
        receipt.setYear(year);
        receipt.setBaseSalary(employee.getBaseSalary());
        receipt.setOvertime(amountNamed(lines, "Overtime"));
        receipt.setBonus(amountNamed(lines, "Christmas Bonus", StandardRemunerations.CHRISTMAS_BONUS)
                .add(amountNamed(lines, "Vacation Subsidy", StandardRemunerations.VACATION_SUBSIDY)));
        receipt.setMealAllowance(amountNamed(lines, "Meal Allowance", StandardRemunerations.MEAL_ALLOWANCE));
        receipt.setTransportAllowance(amountNamed(lines, "Transport Allowance", StandardRemunerations.TRANSPORT_ALLOWANCE));
        receipt.setOtherAllowances(otherAmounts(lines));
        receipt.setGrossAmount(result.getGross());
        receipt.setSocialSecurityContribution(result.getEmployeeSocialSecurity());
        receipt.setEmployerSocialSecurityContribution(result.getEmployerSocialSecurity());
        receipt.setIncomeTax(result.getIncomeTax());
        receipt.setTaxIdContribution(result.getIncomeTax());
        receipt.setTotalContributions(result.getEmployeeSocialSecurity().add(result.getIncomeTax()));
        receipt.setNetAmount(result.getNet());
        receipt.setTotalRemuneration(lines.stream()
                .map(line -> line.getAmount() == null ? BigDecimal.ZERO : line.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        receipt.setRemunerationBreakdown(writeBreakdown(lines));
        return receipt;
    }

    private BigDecimal amountNamed(List<RemunerationLine> lines, String... names) {
        return lines.stream()
                .filter(line -> matchesAny(line.getCategoryName(), names))
                .map(line -> line.getAmount() == null ? BigDecimal.ZERO : line.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal otherAmounts(List<RemunerationLine> lines) {
        return lines.stream()
                .filter(line -> !isNamedColumn(line.getCategoryName()))
                .map(line -> line.getAmount() == null ? BigDecimal.ZERO : line.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isNamedColumn(String name) {
        return matchesAny(name, "Overtime", "Christmas Bonus", StandardRemunerations.CHRISTMAS_BONUS,
                "Vacation Subsidy", StandardRemunerations.VACATION_SUBSIDY,
                "Meal Allowance", StandardRemunerations.MEAL_ALLOWANCE,
                "Transport Allowance", StandardRemunerations.TRANSPORT_ALLOWANCE);
    }

    private static boolean matchesAny(String name, String... candidates) {
        if (name == null) {
            return false;
        }
        for (String candidate : candidates) {
            if (name.equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    private String writeBreakdown(List<RemunerationLine> lines) {
        try {
            return objectMapper.writeValueAsString(lines);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not store the remuneration breakdown", exception);
        }
    }

    private boolean isActive(Employee employee) {
        return employee.getStatus() == null || employee.getStatus() == EmploymentStatus.ACTIVE;
    }
}
