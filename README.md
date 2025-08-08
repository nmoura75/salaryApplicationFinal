# Salary Calculator

A multi-company salary processing application built with Spring Boot and JavaFX.

## Features

- Multi-company support
- Employee management
- Monthly salary processing
- Salary receipt generation
- Database persistence with PostgreSQL
- Modern JavaFX user interface

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher

## Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE salary_calculator;
```

2. Configure the database connection in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/salary_calculator
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. Build the application:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn javafx:run
```

## Project Structure

- `src/main/java/com/payroll/model` - Entity classes
- `src/main/java/com/payroll/repository` - Spring Data JPA repositories
- `src/main/java/com/payroll/service` - Business logic services
- `src/main/java/com/payroll/controller` - JavaFX controllers
- `src/main/resources/fxml` - JavaFX view files
- `src/main/resources/db/migration` - Flyway database migrations

## Technologies Used

- Spring Boot 3.2.3
- JavaFX 21.0.2
- PostgreSQL
- Flyway
- JasperReports
- Lombok
- Maven

## License

This project is licensed under the MIT License - see the LICENSE file for details. 