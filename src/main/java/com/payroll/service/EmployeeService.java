package com.payroll.service;

import com.payroll.model.Employee;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.CompanyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public Employee createEmployee(Employee employee) {
        validateNewEmployee(employee);
        
        // Ensure company exists
        companyRepository.findById(employee.getCompany().getId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        
        // Calculate initial contributions and net salary
        calculateEmployeeContributions(employee);
        
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Integer id, Employee employeeDetails) {
        Employee employee = getEmployeeById(id);
        
        if (!employee.getTaxId().equals(employeeDetails.getTaxId()) && 
            employeeRepository.existsByTaxId(employeeDetails.getTaxId())) {
            throw new IllegalArgumentException("Tax ID already exists");
        }
        
        if (!employee.getSocialSecurityId().equals(employeeDetails.getSocialSecurityId()) && 
            employeeRepository.existsBySocialSecurityId(employeeDetails.getSocialSecurityId())) {
            throw new IllegalArgumentException("Social Security ID already exists");
        }

        employee.setFirstName(employeeDetails.getFirstName());
        employee.setLastName(employeeDetails.getLastName());
        employee.setTaxId(employeeDetails.getTaxId());
        employee.setSocialSecurityId(employeeDetails.getSocialSecurityId());
        employee.setBaseSalary(employeeDetails.getBaseSalary());
        employee.setPosition(employeeDetails.getPosition());
        employee.setDepartment(employeeDetails.getDepartment());
        employee.setBankAccount(employeeDetails.getBankAccount());
        employee.setEmail(employeeDetails.getEmail());
        employee.setPhone(employeeDetails.getPhone());
        employee.setDependents(employeeDetails.getDependents());
        employee.setFamiliarSituation(employeeDetails.getFamiliarSituation());

        // Recalculate contributions and net salary
        calculateEmployeeContributions(employee);

        return employeeRepository.save(employee);
    }

    public Employee getEmployeeById(Integer id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));
    }

    public List<Employee> getEmployeesByCompany(Integer companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new EntityNotFoundException("Company not found");
        }
        return employeeRepository.findByCompanyId(companyId);
    }

    public Page<Employee> searchEmployees(Integer companyId, String searchTerm, Pageable pageable) {
        if (!companyRepository.existsById(companyId)) {
            throw new EntityNotFoundException("Company not found");
        }
        return employeeRepository.searchByCompany(companyId, searchTerm, pageable);
    }

    @Transactional
    public void deleteEmployee(Integer id) {
        if (!employeeRepository.existsById(id)) {
            throw new EntityNotFoundException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    private void validateNewEmployee(Employee employee) {
        if (employeeRepository.existsByTaxId(employee.getTaxId())) {
            throw new IllegalArgumentException("Tax ID already exists");
        }
        
        if (employeeRepository.existsBySocialSecurityId(employee.getSocialSecurityId())) {
            throw new IllegalArgumentException("Social Security ID already exists");
        }
    }

    private void calculateEmployeeContributions(Employee employee) {
        // Calculate Social Security contribution (11% of base salary)
        BigDecimal socialSecurityRate = new BigDecimal("0.11");
        employee.setSocialSecurityContribution(
            employee.getBaseSalary().multiply(socialSecurityRate)
        );

        // Calculate Tax ID contribution (based on base salary and dependents)
        BigDecimal taxRate = calculateTaxRate(employee.getBaseSalary());
        BigDecimal dependentDeduction = BigDecimal.valueOf(employee.getDependents())
                .multiply(new BigDecimal("50.00"));
        
        employee.setTaxIdContribution(
            employee.getBaseSalary()
                .multiply(taxRate)
                .subtract(dependentDeduction)
                .max(BigDecimal.ZERO)
        );

        // Calculate total contributions
        employee.setTotalContributions(
            employee.getSocialSecurityContribution().add(employee.getTaxIdContribution())
        );

        // Calculate net salary
        employee.setNetSalary(
            employee.getBaseSalary().subtract(employee.getTotalContributions())
        );
    }

    //TODO: adapt for the existence rates in Portugal and make this an independente method in a new class because this should be validated executed every time we proces a monthly payroll/receipt
    //TODO: insert field for company contribution
    private BigDecimal calculateTaxRate(BigDecimal baseSalary) { //to do: adapt for the existence rates in Portugal
        // Progressive tax rate based on salary ranges
        if (baseSalary.compareTo(new BigDecimal("1000.00")) <= 0) {
            return new BigDecimal("0.00");
        } else if (baseSalary.compareTo(new BigDecimal("2500.00")) <= 0) {
            return new BigDecimal("0.15");
        } else if (baseSalary.compareTo(new BigDecimal("5000.00")) <= 0) {
            return new BigDecimal("0.25");
        } else {
            return new BigDecimal("0.35");
        }
    }
} 