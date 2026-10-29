package com.payroll.service;

import com.payroll.dto.PayrollInput;
import com.payroll.dto.PayrollResult;
import com.payroll.model.Employee;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.CompanyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final PayrollCalculator payrollCalculator;

    @Transactional
    public Employee createEmployee(Employee employee) {
        validateNewEmployee(employee);
        
        // Ensure company ID is properly set
        if (employee.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID must be set for employee");
        }
        
        // Verify company exists in database
        companyRepository.findById(employee.getCompanyId())
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
        employee.setDateOfBirth(employeeDetails.getDateOfBirth());
        employee.setIrsTable(employeeDetails.getIrsTable());
        employee.setStatus(employeeDetails.getStatus());
        employee.setUsefulDaysOfWork(employeeDetails.getUsefulDaysOfWork());

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

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
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
        PayrollResult result = payrollCalculator.calculate(PayrollInput.forEmployee(employee, List.of()));

        employee.setSocialSecurityContribution(result.getEmployeeSocialSecurity());
        employee.setEmployerSocialSecurityContribution(result.getEmployerSocialSecurity());
        employee.setTaxIdContribution(result.getIncomeTax());
        employee.setTotalContributions(result.getEmployeeSocialSecurity().add(result.getIncomeTax()));
        employee.setNetSalary(result.getNet());
    }
} 