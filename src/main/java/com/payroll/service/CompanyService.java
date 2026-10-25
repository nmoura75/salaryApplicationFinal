package com.payroll.service;

import com.payroll.config.CompanyDatabaseMigrator;
import com.payroll.config.CompanyRegistry;
import com.payroll.config.CompanyRoutingDataSource;
import com.payroll.config.CurrentCompanyHolder;
import com.payroll.model.Company;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Creates, opens, and deletes one SQLite file per company.
 * Registry checks happen before a transaction so the connection targets the right file.
 */
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRegistry registry;
    private final CompanyDatabaseMigrator migrator;
    private final CompanyRoutingDataSource routingDataSource;
    private final CompanyStore companyStore;

    public Company createCompany(Company company) {
        registry.assertUnique(company.getName(), company.getTaxId(), company.getSocialSecurityId());
        Path databaseFile = registry.pathForTaxId(company.getTaxId());
        migrator.migrate(databaseFile);
        String path = databaseFile.toAbsolutePath().toString();
        CurrentCompanyHolder.set(path);
        try {
            Company saved = companyStore.insert(company);
            CompanyRegistry.Entry entry = new CompanyRegistry.Entry();
            entry.setName(saved.getName());
            entry.setTaxId(saved.getTaxId());
            entry.setSocialSecurityId(saved.getSocialSecurityId());
            entry.setDbPath(path);
            registry.add(entry);
            return saved;
        } finally {
            CurrentCompanyHolder.clear();
            routingDataSource.closePool(path);
        }
    }

    public Company updateCompany(Integer id, Company companyDetails) {
        String currentPath = requireOpenPath();
        Company current = companyStore.getById(id);
        registry.assertUniqueForUpdate(current.getTaxId(), companyDetails.getName(),
                companyDetails.getTaxId(), companyDetails.getSocialSecurityId());
        Company saved = companyStore.update(id, companyDetails);

        String newPath = currentPath;
        if (!current.getTaxId().equals(saved.getTaxId())) {
            Path destination = registry.pathForTaxId(saved.getTaxId()).toAbsolutePath();
            CurrentCompanyHolder.clear();
            routingDataSource.closePool(currentPath);
            try {
                Files.createDirectories(destination.getParent());
                Files.move(Path.of(currentPath), destination);
            } catch (IOException exception) {
                CurrentCompanyHolder.set(currentPath);
                throw new IllegalStateException("Could not rename the company database file", exception);
            }
            newPath = destination.toString();
            CurrentCompanyHolder.set(newPath);
        }

        CompanyRegistry.Entry entry = new CompanyRegistry.Entry();
        entry.setName(saved.getName());
        entry.setTaxId(saved.getTaxId());
        entry.setSocialSecurityId(saved.getSocialSecurityId());
        entry.setDbPath(newPath);
        registry.replace(current.getTaxId(), entry);
        return saved;
    }

    public Company openCompany(String name) {
        CompanyRegistry.Entry entry = registry.requireByName(name);
        String newPath = entry.getDbPath();
        String currentPath = CurrentCompanyHolder.get();
        if (currentPath != null && !currentPath.equals(newPath)) {
            CurrentCompanyHolder.clear();
            routingDataSource.closePool(currentPath);
        }
        migrator.migrate(Path.of(newPath));
        CurrentCompanyHolder.set(newPath);
        try {
            return companyStore.requireSingle();
        } catch (RuntimeException exception) {
            CurrentCompanyHolder.clear();
            routingDataSource.closePool(newPath);
            throw exception;
        }
    }

    public void closeCompany() {
        String currentPath = CurrentCompanyHolder.get();
        CurrentCompanyHolder.clear();
        if (currentPath != null) {
            routingDataSource.closePool(currentPath);
        }
    }

    public void deleteCompany(Integer id) {
        Company company = companyStore.getById(id);
        deleteCompanyByName(company.getName());
    }

    public void deleteCompanyByName(String name) {
        CompanyRegistry.Entry entry = registry.requireByName(name);
        String path = entry.getDbPath();
        if (path.equals(CurrentCompanyHolder.get())) {
            CurrentCompanyHolder.clear();
        }
        routingDataSource.closePool(path);
        try {
            Files.deleteIfExists(Path.of(path));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not delete the company database file", exception);
        }
        registry.removeByTaxId(entry.getTaxId());
    }

    public Company getCompanyById(Integer id) {
        return companyStore.getById(id);
    }

    public Company getCompanyByTaxId(String taxId) {
        return companyStore.getByTaxId(taxId);
    }

    public Company getCompanyByName(String name) {
        if (CurrentCompanyHolder.get() != null) {
            try {
                Company open = companyStore.getByName(name);
                if (open != null) {
                    return open;
                }
            } catch (EntityNotFoundException exception) {
                // Fall through to the registry error below.
            }
        }
        throw new EntityNotFoundException("Company not found with name: " + name);
    }

    public List<Company> getAllCompanies() {
        return registry.list().stream().map(this::toSummary).toList();
    }

    private Company toSummary(CompanyRegistry.Entry entry) {
        Company company = new Company();
        company.setName(entry.getName());
        company.setTaxId(entry.getTaxId());
        company.setSocialSecurityId(entry.getSocialSecurityId());
        return company;
    }

    private String requireOpenPath() {
        String path = CurrentCompanyHolder.get();
        if (path == null) {
            throw new IllegalStateException("No company database is open");
        }
        return path;
    }
}
