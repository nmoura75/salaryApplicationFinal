# Salary Calculator

A desktop payroll application for several companies. Each company has its own SQLite file. The window is JavaFX. Spring Boot provides the services and the database access.

## Features

- One SQLite file per company, indexed in `data/registry.json`
- Employee management
- Monthly salary preparation from remuneration categories
- Salary receipt creation and PDF export
- Company files stay on this computer

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Run during development

```bash
mvn javafx:run
```

Company databases are created under `./data/companies/` when a company is added in the application. The registry file is `./data/registry.json`.

## Standalone build

`mvn javafx:run` is the development launch. A self-contained app image, with its own runtime, is built with:

```bash
mvn -Ppackage-app verify
```

The image is written to `target/dist/SalaryCalculator`.

## Project structure

- `src/main/java/com/payroll/model` - Entity classes
- `src/main/java/com/payroll/repository` - Spring Data JPA repositories
- `src/main/java/com/payroll/service` - Business logic services
- `src/main/java/com/payroll/controller` - JavaFX controllers
- `src/main/resources/fxml` - JavaFX view files
- `src/main/resources/db/migration` - Flyway database migrations, applied to each company file

## Technologies

- Spring Boot 3.2.3
- JavaFX 21.0.2
- SQLite
- Flyway
- JasperReports
- Lombok
- Maven

## License

This project is licensed under the MIT License - see the LICENSE file for details.
