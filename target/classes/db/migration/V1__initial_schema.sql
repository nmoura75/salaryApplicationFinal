-- =====================================================
-- COMPLETE INITIAL SCHEMA FOR SALARY CALCULATOR
-- =====================================================

-- Companies table
CREATE TABLE companies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    tax_id TEXT NOT NULL UNIQUE,
    tax_id_password TEXT,
    social_security_id TEXT NOT NULL UNIQUE,
    social_security_id_password TEXT,
    address TEXT,
    phone TEXT,
    email TEXT
);

-- Employees table
CREATE TABLE employees (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    company_id INTEGER NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    tax_id TEXT NOT NULL UNIQUE,
    social_security_id TEXT NOT NULL UNIQUE,
    hire_date TEXT NOT NULL,
    base_salary REAL NOT NULL,
    social_security_contribution REAL NOT NULL,
    employer_social_security_contribution REAL NOT NULL,
    tax_id_contribution REAL NOT NULL,
    total_contributions REAL NOT NULL,
    net_salary REAL NOT NULL,
    dependents INTEGER DEFAULT 0,
    familiar_situation INTEGER NOT NULL,
    useful_days_of_work INTEGER NOT NULL,
    position TEXT,
    department TEXT,
    bank_account TEXT,
    email TEXT,
    phone TEXT,
    date_of_birth TEXT,
    irs_table INTEGER DEFAULT 1,
    status TEXT DEFAULT 'ACTIVE',
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Salary receipts table
CREATE TABLE salary_receipts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    payment_date TEXT NOT NULL,
    month TEXT NOT NULL,
    year INTEGER NOT NULL,
    base_salary REAL NOT NULL,
    social_security_contribution REAL NOT NULL,
    employer_social_security_contribution REAL NOT NULL,
    tax_id_contribution REAL NOT NULL,
    total_contributions REAL NOT NULL,
    overtime REAL,
    bonus REAL,
    meal_allowance REAL,
    transport_allowance REAL,
    other_allowances REAL,
    income_tax REAL NOT NULL,
    gross_amount REAL NOT NULL,
    net_amount REAL NOT NULL,
    remuneration_breakdown TEXT,
    total_remuneration REAL,
    notes TEXT,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- =====================================================
-- REMUNERATION SYSTEM TABLES
-- =====================================================

-- Remuneration categories table
CREATE TABLE remuneration_categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    type TEXT NOT NULL,
    contribution_rule TEXT NOT NULL DEFAULT 'SUBJECT',
    default_value REAL,
    is_active INTEGER NOT NULL DEFAULT 1,
    company_id INTEGER NOT NULL,
    sort_order INTEGER,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Employee default remunerations table
CREATE TABLE employee_remunerations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    category_id INTEGER NOT NULL,
    value REAL,
    is_active INTEGER NOT NULL DEFAULT 1,
    notes TEXT,
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (category_id) REFERENCES remuneration_categories(id),
    UNIQUE(employee_id, category_id)
);

-- Monthly business days table
CREATE TABLE monthly_business_days (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    company_id INTEGER NOT NULL,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    business_days INTEGER NOT NULL,
    last_updated TEXT,
    notes TEXT,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    UNIQUE(company_id, year, month)
);

-- Monthly remunerations table (for overrides and additional amounts)
CREATE TABLE monthly_remunerations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    category_id INTEGER NOT NULL,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    value REAL,
    is_override INTEGER NOT NULL DEFAULT 0,
    notes TEXT,
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (category_id) REFERENCES remuneration_categories(id),
    UNIQUE(employee_id, category_id, year, month)
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Employee indexes
CREATE INDEX idx_employees_company_id ON employees(company_id);
CREATE INDEX idx_employees_tax_id ON employees(tax_id);
CREATE INDEX idx_employees_social_security_id ON employees(social_security_id);

-- Salary receipt indexes
CREATE INDEX idx_salary_receipts_employee_id ON salary_receipts(employee_id);
CREATE INDEX idx_salary_receipts_month_year ON salary_receipts(month, year);

-- Remuneration indexes
CREATE INDEX idx_employee_remunerations_employee_id ON employee_remunerations(employee_id);
CREATE INDEX idx_employee_remunerations_category_id ON employee_remunerations(category_id);
CREATE INDEX idx_monthly_business_days_company_year_month ON monthly_business_days(company_id, year, month);
CREATE INDEX idx_monthly_remunerations_employee_year_month ON monthly_remunerations(employee_id, year, month);
CREATE INDEX idx_monthly_remunerations_category_year_month ON monthly_remunerations(category_id, year, month);