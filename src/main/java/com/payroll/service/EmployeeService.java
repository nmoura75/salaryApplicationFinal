package com.payroll.service;

import com.payroll.dto.PayrollInput;
import com.payroll.dto.PayrollResult;
import com.payroll.i18n.MessageKeys;
import com.payroll.model.BankDetails;
import com.payroll.model.ContractType;
import com.payroll.model.Employee;
import com.payroll.model.EmploymentStatus;
import com.payroll.model.IncomeHolders;
import com.payroll.model.InsurancePolicy;
import com.payroll.model.WorkingTime;
import com.payroll.repository.CompanyRepository;
import com.payroll.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final PayrollCalculator payrollCalculator;
    private final WithholdingTableResolver withholdingTableResolver;

    @Transactional
    public Employee createEmployee(Employee employee) {
        validateNewEmployee(employee);

        if (employee.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID must be set for employee");
        }

        companyRepository.findById(employee.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));

        applyDefaults(employee);
        validateContract(employee);
        applyWithholdingTable(employee);
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

        copyEditableFields(employee, employeeDetails);
        applyDefaults(employee);
        validateContract(employee);
        applyWithholdingTable(employee);
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

    private void applyDefaults(Employee employee) {
        if (employee.getStatus() == null) {
            employee.setStatus(EmploymentStatus.ACTIVE);
        }
        if (employee.getContractType() == null) {
            employee.setContractType(ContractType.PERMANENT);
        }
        if (employee.getWorkingTime() == null) {
            employee.setWorkingTime(WorkingTime.FULL_TIME);
        }
        if (employee.getIncomeHolders() == null) {
            employee.setIncomeHolders(IncomeHolders.SINGLE_HOLDER);
        }
        if (employee.getDependents() == null) {
            employee.setDependents(0);
        }
        if (employee.getUsefulDaysOfWork() == null) {
            employee.setUsefulDaysOfWork(PayrollConstants.DEFAULT_WORKING_DAYS);
        }
        if (employee.getBankDetails() == null) {
            employee.setBankDetails(new BankDetails());
        }
        if (employee.getInsurancePolicy() == null) {
            employee.setInsurancePolicy(new InsurancePolicy());
        }
    }

    private void validateContract(Employee employee) {
        if (employee.getContractType() == ContractType.FIXED_TERM && isBlank(employee.getContractEndDate())) {
            throw new IllegalArgumentException(MessageKeys.FIXED_TERM_NEEDS_END_DATE);
        }
        if (employee.getStatus() == EmploymentStatus.INACTIVE
                && (isBlank(employee.getTerminationDate()) || employee.getTerminationReason() == null)) {
            throw new IllegalArgumentException(MessageKeys.INACTIVE_NEEDS_TERMINATION);
        }
        LocalDate hireDate = StoredDates.parse(employee.getHireDate());
        LocalDate terminationDate = StoredDates.parse(employee.getTerminationDate());
        if (hireDate != null && terminationDate != null && terminationDate.isBefore(hireDate)) {
            throw new IllegalArgumentException(MessageKeys.TERMINATION_BEFORE_HIRE);
        }
    }

    private void applyWithholdingTable(Employee employee) {
        if (employee.getMaritalStatus() == null) {
            throw new IllegalArgumentException(MessageKeys.MARITAL_STATUS_REQUIRED);
        }
        int dependents = employee.getDependents() == null ? 0 : employee.getDependents();
        employee.setWithholdingTable(withholdingTableResolver.resolve(
                employee.getMaritalStatus(),
                employee.getIncomeHolders(),
                dependents,
                employee.isDisability()).table());
    }

    private void copyEditableFields(Employee employee, Employee employeeDetails) {
        employee.setFirstName(employeeDetails.getFirstName());
        employee.setLastName(employeeDetails.getLastName());
        employee.setTaxId(employeeDetails.getTaxId());
        employee.setSocialSecurityId(employeeDetails.getSocialSecurityId());
        employee.setDateOfBirth(employeeDetails.getDateOfBirth());
        employee.setGender(employeeDetails.getGender());
        employee.setNationality(employeeDetails.getNationality());
        employee.setAddress(employeeDetails.getAddress());
        employee.setPostalCode(employeeDetails.getPostalCode());
        employee.setLocality(employeeDetails.getLocality());
        employee.setEmail(employeeDetails.getEmail());
        employee.setPhone(employeeDetails.getPhone());
        employee.setStatus(employeeDetails.getStatus());
        employee.setContractType(employeeDetails.getContractType());
        employee.setWorkingTime(employeeDetails.getWorkingTime());
        employee.setPosition(employeeDetails.getPosition());
        employee.setDepartment(employeeDetails.getDepartment());
        employee.setHireDate(employeeDetails.getHireDate());
        employee.setContractEndDate(employeeDetails.getContractEndDate());
        employee.setTerminationDate(employeeDetails.getTerminationDate());
        employee.setTerminationReason(employeeDetails.getTerminationReason());
        employee.setMaritalStatus(employeeDetails.getMaritalStatus());
        employee.setIncomeHolders(employeeDetails.getIncomeHolders());
        employee.setDependents(employeeDetails.getDependents());
        employee.setDisability(employeeDetails.isDisability());
        employee.setBaseSalary(employeeDetails.getBaseSalary());
        employee.setUsefulDaysOfWork(employeeDetails.getUsefulDaysOfWork());
        copyBank(employee, employeeDetails.getBankDetails());
        copyInsurance(employee, employeeDetails.getInsurancePolicy());
    }

    private void copyBank(Employee employee, BankDetails incoming) {
        BankDetails bank = employee.getBankDetails() == null ? new BankDetails() : employee.getBankDetails();
        bank.setBankName(incoming == null ? null : incoming.getBankName());
        bank.setIban(incoming == null ? null : incoming.getIban());
        bank.setSwift(incoming == null ? null : incoming.getSwift());
        employee.setBankDetails(bank);
    }

    private void copyInsurance(Employee employee, InsurancePolicy incoming) {
        InsurancePolicy policy = employee.getInsurancePolicy() == null
                ? new InsurancePolicy()
                : employee.getInsurancePolicy();
        policy.setInsurer(incoming == null ? null : incoming.getInsurer());
        policy.setPolicyNumber(incoming == null ? null : incoming.getPolicyNumber());
        employee.setInsurancePolicy(policy);
    }

    private void calculateEmployeeContributions(Employee employee) {
        PayrollResult result = payrollCalculator.calculate(PayrollInput.forEmployee(employee, List.of()));

        employee.setSocialSecurityContribution(result.getEmployeeSocialSecurity());
        employee.setEmployerSocialSecurityContribution(result.getEmployerSocialSecurity());
        employee.setTaxIdContribution(result.getIncomeTax());
        employee.setTotalContributions(result.getEmployeeSocialSecurity().add(result.getIncomeTax()));
        employee.setNetSalary(result.getNet());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
