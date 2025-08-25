package com.payroll.service;

import com.payroll.model.Employee;
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

    @Transactional
    public SalaryReceipt createSalaryReceipt(Integer employeeId, String month, Integer year,
                                            BigDecimal overtime, BigDecimal bonus,
                                            BigDecimal mealAllowance, BigDecimal transportAllowance,
                                            BigDecimal otherAllowances, String notes) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        if (salaryReceiptRepository.existsByEmployeeIdAndMonthAndYear(employeeId, month, year)) {
            throw new IllegalStateException("Salary receipt already exists for this month");
        }

        SalaryReceipt receipt = new SalaryReceipt();
        receipt.setEmployeeId(employeeId);
        receipt.setPaymentDate(LocalDate.now().toString());
        receipt.setMonth(month);
        receipt.setYear(year);
        receipt.setBaseSalary(employee.getBaseSalary());
        receipt.setOvertime(overtime != null ? overtime : BigDecimal.ZERO);
        receipt.setBonus(bonus != null ? bonus : BigDecimal.ZERO);
        receipt.setMealAllowance(mealAllowance != null ? mealAllowance : BigDecimal.ZERO);
        receipt.setTransportAllowance(transportAllowance != null ? transportAllowance : BigDecimal.ZERO);
        receipt.setOtherAllowances(otherAllowances != null ? otherAllowances : BigDecimal.ZERO);
        receipt.setNotes(notes);

        calculateSalaryReceiptAmounts(receipt, employee);

        return salaryReceiptRepository.save(receipt);
    }

    @Transactional
    public SalaryReceipt updateSalaryReceipt(Integer id, BigDecimal overtime, BigDecimal bonus,
                                            BigDecimal mealAllowance, BigDecimal transportAllowance,
                                            BigDecimal otherAllowances, String notes) {
        SalaryReceipt receipt = salaryReceiptRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Salary receipt not found"));

        // Get the employee to pass to calculateSalaryReceiptAmounts
        Employee employee = employeeRepository.findById(receipt.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        receipt.setOvertime(overtime != null ? overtime : BigDecimal.ZERO);
        receipt.setBonus(bonus != null ? bonus : BigDecimal.ZERO);
        receipt.setMealAllowance(mealAllowance != null ? mealAllowance : BigDecimal.ZERO);
        receipt.setTransportAllowance(transportAllowance != null ? transportAllowance : BigDecimal.ZERO);
        receipt.setOtherAllowances(otherAllowances != null ? otherAllowances : BigDecimal.ZERO);
        receipt.setNotes(notes);

        calculateSalaryReceiptAmounts(receipt, employee);

        return salaryReceiptRepository.save(receipt);
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

    private void calculateSalaryReceiptAmounts(SalaryReceipt receipt, Employee employee) {
        // Calculate gross amount (base salary + extras)
        BigDecimal grossAmount = receipt.getBaseSalary()
                .add(receipt.getOvertime())
                .add(receipt.getBonus())
                .add(receipt.getMealAllowance())
                .add(receipt.getTransportAllowance())
                .add(receipt.getOtherAllowances());
        receipt.setGrossAmount(grossAmount);

        // Calculate social security contribution (11% of gross amount)
        BigDecimal socialSecurityContribution = grossAmount.multiply(new BigDecimal("0.11"));
        receipt.setSocialSecurityContribution(socialSecurityContribution);

        // Calculate income tax (based on gross amount)
        BigDecimal incomeTax = calculateIncomeTax(grossAmount, employee);
        receipt.setIncomeTax(incomeTax);

        // Calculate net amount
        BigDecimal netAmount = grossAmount
                .subtract(socialSecurityContribution)
                .subtract(incomeTax);
        receipt.setNetAmount(netAmount);
    }

    private BigDecimal calculateIncomeTax(BigDecimal grossAmount, Employee employee) {
        // Get base tax rate
        BigDecimal taxRate = getTaxRate(grossAmount);
        
        // Calculate tax amount before deductions
        BigDecimal baseTaxAmount = grossAmount.multiply(taxRate);
        
        // Apply deductions for dependents (50.00 per dependent)
        BigDecimal dependentDeduction = BigDecimal.valueOf(employee.getDependents())
                .multiply(new BigDecimal("50.00"));
        
        // Calculate final tax amount (minimum 0)
        return baseTaxAmount.subtract(dependentDeduction).max(BigDecimal.ZERO);
    }


    //TODO: adapt for the existence rates in Portugal and make this an independente method in a new class because this should be validated executed every time we proces a monthly payroll/receipt  
    
    private BigDecimal getTaxRate(BigDecimal grossAmount) {
        if (grossAmount.compareTo(new BigDecimal("1000.00")) <= 0) {
            return new BigDecimal("0.00");
        } else if (grossAmount.compareTo(new BigDecimal("2500.00")) <= 0) {
            return new BigDecimal("0.15");
        } else if (grossAmount.compareTo(new BigDecimal("5000.00")) <= 0) {
            return new BigDecimal("0.25");
        } else {
            return new BigDecimal("0.35");
        }
    }
} 