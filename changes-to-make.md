# Changes to make

Summary of the work identified in the project analysis. This is a desktop payroll app (JavaFX + Spring Boot + one SQLite file). Company selection and employee management work. Monthly payroll, receipts, consulting, and government maps are still shells, and the tax math is a placeholder.

## 1. Missing screens

`MainMenuController` opens these FXML files, and they are not in `src/main/resources/fxml`:

- `SalaryPreparation.fxml`
- `SalaryProcessing.fxml`
- `SalaryConsulting.fxml`
- `GovernmentMaps.fxml`

The four matching controllers only show the company name. Either add the screens or stop the menu from opening files that do not exist.

## 2. One payroll calculator

`EmployeeService` and `SalaryReceiptService` each apply the same homemade rules:

- Social security: flat 11% of the amount
- Income tax: 0% up to 1,000, then 15% / 25% / 35%, minus 50 per dependent

Replace both with one calculator used by the employee preview and by the receipt:

- Social security base (not every allowance is subject)
- Employer social-security rate (the employee contribution field already has a TODO for this)
- IRS withholding from the official tables, using region, marital status, dependents, and the year's retention tables

`irs_table` and `familiar_situation` are stored and never used. Madeira, Açores, and disability tables stay as a later step, but the calculator should be built so those tables can be added.

## 3. Drive receipts from remunerations

`RemunerationService` already supports `DAILY`, `MONTHLY`, and `PERCENTAGE` categories, employee defaults, monthly overrides, and business days (meal allowance = daily value × business days). Receipt creation does not call it. Fixed columns on `salary_receipts` (`meal_allowance`, `bonus`, and so on) can disagree with the category breakdown.

Receipt lines should come from `RemunerationService`.

Fixes inside that service and its screen:

- `PERCENTAGE` currently returns the stored number. It should be applied to a base.
- `getMonthlyRemunerations` always returns an empty list.
- The remuneration screen hardcodes category ids 1, 2, 3, and 5. Use category name or type. Commission (seed id 4) is skipped. Seed categories exist only for company id 1, so a new company has none.
- Set the company id before loading employees. `setCompanyName` loads data while `companyId` is still null, and going back to the employee list does not pass the company id.

`useful_days_of_work` on the employee overlaps with `monthly_business_days`. Business days belong to the month, not to the employee record.

## 4. Passwords and seed data

- The migration inserts "Default Company" with passwords `password123` and `sspassword123`. Remove those seed passwords.
- `tax_id_password` and `social_security_id_password` are stored in plain text. Do not keep portal credentials in clear text.
- `spring.flyway.clean-disabled=false` allows wiping the schema. Turn that off once real payroll data is in the file.

## 5. Docs and tests

- The README still describes PostgreSQL and `mvn clean install`. The app runs on SQLite with `mvn javafx:run`. `V1__initial_schema_postgres.txt` is a leftover.
- There is no `src/test`. Add a test for meal allowance (daily value × business days) and for one month's receipt.
- Do not commit compiled classes under `target/`.

## 6. Desktop startup

The salary calculation is already small. The slow part of launch is the JVM starting Spring with a web port and security that the desktop app does not use. Take that out of the desktop launch:

- Remove `spring-boot-starter-security` and `spring-security-test` from `pom.xml`.
- Remove `server.port`, `spring.security.user.name`, and `spring.security.user.password` from `application.properties`.
- Set `spring.main.web-application-type=none` so Boot does not open a web server. The window still starts from `SalaryCalculatorApplication`.
- Keep Spring for services, repositories, and Flyway only. Do not add Spring MVC or a REST API to the desktop launch. The browser edition, when it exists, is a separate module.
- Ship the app with `jlink` or `jpackage` so it carries its own runtime. `mvn javafx:run` stays the command used during development.
- After the steps above, time a cold start. A GraalVM native image is the next step only if that start is still too slow. Hibernate and JavaFX need extra native-image configuration, so this is not part of the first pass.

## Suggested order

1. Missing screens, or hide the menu entries until the screens exist.
2. Single payroll calculator, and stop duplicating the tax brackets.
3. Receipt lines from `RemunerationService`.
4. Remuneration screen: category name or type, and company id set before load.
5. Seed passwords and plain-text portal passwords.
6. Desktop startup: remove security and the web port, set the application type to none, then package with `jlink` or `jpackage`.
7. README aligned with SQLite, plus the two payroll tests.
