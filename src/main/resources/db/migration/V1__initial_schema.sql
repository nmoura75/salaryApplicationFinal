CREATE TABLE companies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    tax_id TEXT NOT NULL UNIQUE,
    tax_id_password TEXT NOT NULL,
    social_security_id TEXT NOT NULL UNIQUE,
    social_security_id_password TEXT NOT NULL,
    address TEXT,
    phone TEXT,
    email TEXT
);

CREATE TABLE employees (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    company_id INTEGER NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    tax_id TEXT NOT NULL UNIQUE,
    social_security_id TEXT NOT NULL UNIQUE,
    hire_date DATE NOT NULL,
    base_salary NUMERIC(38,2) NOT NULL,
    social_security_contribution NUMERIC(38,2) NOT NULL,
    tax_id_contribution NUMERIC(38,2) NOT NULL,
    total_contributions NUMERIC(38,2) NOT NULL,
    net_salary NUMERIC(38,2) NOT NULL,
    dependents INTEGER,
    familiar_situation INTEGER NOT NULL,
    useful_days_of_work INTEGER NOT NULL,
    position TEXT,
    department TEXT,
    bank_account TEXT,
    email TEXT,
    phone TEXT,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE salary_receipts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    payment_date DATE NOT NULL,
    month TEXT NOT NULL,
    year INTEGER NOT NULL,
    base_salary NUMERIC(38,2) NOT NULL,
    social_security_contribution NUMERIC(38,2) NOT NULL,
    tax_id_contribution NUMERIC(38,2) NOT NULL,
    total_contributions NUMERIC(38,2) NOT NULL,
    overtime NUMERIC(38,2),
    bonus NUMERIC(38,2),
    meal_allowance NUMERIC(38,2),
    transport_allowance NUMERIC(38,2),
    other_allowances NUMERIC(38,2),
    income_tax NUMERIC(38,2) NOT NULL,
    gross_amount NUMERIC(38,2) NOT NULL,
    net_amount NUMERIC(38,2) NOT NULL,
    notes TEXT,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);