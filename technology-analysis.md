# Technology analysis

Date: 14 October 2026

The current stack is the right one for a standalone payroll app now and a browser UI later. Keep Java, Spring, and JavaFX. Do not replace the desktop UI with a website packaged as an app.

What needs care is the version line, how the desktop process is started, and keeping the payroll logic free of JavaFX so a second UI can call it.

## Verdict

| Piece | For this project |
|---|---|
| Java | Keep it. Move off 17 when the build is next touched. |
| Spring Boot | Keep it. It is what lets a browser edition reuse the same services. |
| JavaFX | Keep it for the standalone app. |
| SQLite | Right for the desktop edition. Wrong as the only database once several people use a browser. |
| JasperReports | Fine for receipts, and it can run on the server later. The 6.21.0 copy in the project is old. |
| Spring Security and port 8080 | Not useful in the desktop app. |

A browser UI later means a second front end, not a second copy of the payroll rules. `EmployeeService`, `RemunerationService`, and `SalaryReceiptService` already have no JavaFX types. That split is the part worth protecting.

## Desktop now

JavaFX is still the normal way to ship a Java desktop app in 2026. Gluon maintains it, and the long-term lines are 21 and 25. The project pins JavaFX 21.0.2 on Java 17. That still runs, but both are behind the lines worth shipping:

- Java 17 public updates from Adoptium run at least until October 2027. Java 21 runs until at least December 2029. Java 25, released September 2025, runs until at least September 2031.
- JavaFX 21 still gets patches (21.0.12 as of July 2026) and is the newest long-term JavaFX that runs on Java 17. JavaFX 25 needs Java 23 or newer.

A practical pair for this app is Java 21 with JavaFX 21.0.12. Java 25 with JavaFX 25 is the newer long-term pair, and it is a bigger jump than this project needs right now. Either way, the JDK installed on the user's PC should not matter: a real standalone build uses `jlink` or `jpackage` and ships its own runtime. `mvn javafx:run` is only the development launch.

Spring Boot 3.2.3 is from early 2024. As of this date, current Spring Boot is the 4.1 line, and 3.5 is the last 3.x line still updated. Staying on 3.2 is safe for a while because the code is small, but it is not a line to stay on through the browser edition. A move to a current 3.5 is the small upgrade. Boot 4 can wait until the web UI is added.

Spring inside a desktop app is heavy, and startup is slower than a plain JavaFX program. That cost is justified here. The services are already Spring beans, so a later web module can call them without rewriting payroll. Two things in the current setup fight the desktop shape: `spring-boot-starter-security` with `admin` / `admin`, and `server.port=8080`. The desktop process should not open a web port. Set the application type to none until there is a real web module.

SQLite is the right embedded database for one user on one machine. The pool size of 1 and a Flyway-owned schema match that. Hibernate's SQLite dialect is a community dialect and stays awkward on schema changes, so `ddl-auto=none` and hand-written migrations are the correct choice. The migration SQL is SQLite-specific (`AUTOINCREMENT`, integer flags). That is fine for the desktop file. It will not run unchanged on PostgreSQL.

JasperReports still fits a recibo. Generation already lives in `PDFGenerationService`, so the same PDF can be saved from the desktop app or downloaded from the browser. Version 6.21.0 is behind 7.0.8 (August 2026), and the 6.21 line has published vulnerabilities. Upgrade when reports are next touched. It is not a reason to change libraries.

## Browser later, both kept

Keep both by adding a web module beside the JavaFX module, not by rewriting the screens in HTML now.

```
payroll-core      entities, repositories, services, PDF, Flyway
payroll-desktop   JavaFX, FXML, controllers
payroll-web       Spring MVC or a small REST API plus a browser UI
```

One Maven module is enough until the web UI starts. The rule until then: controllers may know JavaFX, services may not.

The database should split with the edition:

- The desktop stays on its local SQLite file. One accountant, one machine, no server to install.
- The browser edition, if more than one person uses it, wants PostgreSQL. SQLite allows one writer and is a poor shared server database.

JPA makes that split possible. Flyway scripts need a desktop set and a server set, or SQL that both accept. One `salary_calculator.db` should not be planned as the database for the website.

The browser UI should be a thin layer over the same services: list employees, prepare a month, download the PDF the desktop app already builds. Spring MVC with server-rendered pages is enough for an internal payroll tool. A separate JavaScript application is extra surface the first web version does not need.

Do not wrap the app in Electron or Tauri to share the UI, and do not turn the desktop app into a local website opened in Chrome. That would drop the JavaFX work and still leave a UI to maintain, with a worse offline install. Two thin UIs over one core is the smaller long-term cost.
