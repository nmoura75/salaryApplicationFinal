package com.payroll.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Index of company name, tax id, and database path. Payroll data stays in each company file.
 */
@Component
public class CompanyRegistry {

    private final ObjectMapper objectMapper;
    private final Path registryFile;
    private final Path companiesDirectory;

    public CompanyRegistry(ObjectMapper objectMapper, @Value("${payroll.data-directory}") String dataDirectory) {
        Path root = Path.of(dataDirectory);
        this.objectMapper = objectMapper;
        this.registryFile = root.resolve("registry.json");
        this.companiesDirectory = root.resolve("companies");
    }

    public List<Entry> list() {
        return new ArrayList<>(read().getCompanies());
    }

    public Optional<Entry> findByName(String name) {
        return list().stream().filter(entry -> entry.getName().equals(name)).findFirst();
    }

    public Entry requireByName(String name) {
        return findByName(name).orElseThrow(() -> new IllegalArgumentException("Company not found: " + name));
    }

    public Path pathForTaxId(String taxId) {
        String safeTaxId = taxId.replaceAll("[^A-Za-z0-9]", "");
        if (safeTaxId.isEmpty()) {
            throw new IllegalArgumentException("Tax ID cannot be used as a database file name");
        }
        return companiesDirectory.resolve(safeTaxId + ".db");
    }

    public void assertUnique(String name, String taxId, String socialSecurityId) {
        for (Entry entry : list()) {
            if (entry.getName().equals(name)) {
                throw new IllegalArgumentException("Company name already exists");
            }
            if (entry.getTaxId().equals(taxId)) {
                throw new IllegalArgumentException("Tax ID already exists");
            }
            if (entry.getSocialSecurityId().equals(socialSecurityId)) {
                throw new IllegalArgumentException("Social Security ID already exists");
            }
        }
    }

    /**
     * Checks uniqueness while ignoring the company that is being edited.
     */
    public void assertUniqueForUpdate(String currentTaxId, String name, String taxId, String socialSecurityId) {
        for (Entry entry : list()) {
            if (entry.getTaxId().equals(currentTaxId)) {
                continue;
            }
            if (entry.getName().equals(name)) {
                throw new IllegalArgumentException("Company name already exists");
            }
            if (entry.getTaxId().equals(taxId)) {
                throw new IllegalArgumentException("Tax ID already exists");
            }
            if (entry.getSocialSecurityId().equals(socialSecurityId)) {
                throw new IllegalArgumentException("Social Security ID already exists");
            }
        }
    }

    public void add(Entry entry) {
        Document document = read();
        document.getCompanies().add(entry);
        write(document);
    }

    public void replace(String currentTaxId, Entry updated) {
        Document document = read();
        List<Entry> companies = document.getCompanies();
        boolean replaced = false;
        for (int index = 0; index < companies.size(); index++) {
            if (companies.get(index).getTaxId().equals(currentTaxId)) {
                companies.set(index, updated);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            throw new IllegalArgumentException("Company not found in the registry");
        }
        write(document);
    }

    public void removeByTaxId(String taxId) {
        Document document = read();
        document.getCompanies().removeIf(entry -> entry.getTaxId().equals(taxId));
        write(document);
    }

    private Document read() {
        if (!Files.exists(registryFile)) {
            return new Document();
        }
        try {
            return objectMapper.readValue(registryFile.toFile(), Document.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read the company registry", exception);
        }
    }

    private void write(Document document) {
        try {
            Files.createDirectories(registryFile.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(registryFile.toFile(), document);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write the company registry", exception);
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {
        private List<Entry> companies = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
        private String name;
        private String taxId;
        private String socialSecurityId;
        private String dbPath;
    }
}
