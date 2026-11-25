ALTER TABLE employees ADD COLUMN address TEXT;
ALTER TABLE employees ADD COLUMN postal_code TEXT;
ALTER TABLE employees ADD COLUMN locality TEXT;
ALTER TABLE employees ADD COLUMN nationality TEXT;
ALTER TABLE employees ADD COLUMN gender TEXT;

ALTER TABLE employees ADD COLUMN contract_type TEXT NOT NULL DEFAULT 'PERMANENT';
ALTER TABLE employees ADD COLUMN working_time TEXT NOT NULL DEFAULT 'FULL_TIME';
ALTER TABLE employees ADD COLUMN contract_end_date TEXT;
ALTER TABLE employees ADD COLUMN termination_date TEXT;
ALTER TABLE employees ADD COLUMN termination_reason TEXT;

ALTER TABLE employees ADD COLUMN income_holders TEXT NOT NULL DEFAULT 'SINGLE_HOLDER';
ALTER TABLE employees ADD COLUMN disability INTEGER NOT NULL DEFAULT 0;

ALTER TABLE employees ADD COLUMN bank_name TEXT;
ALTER TABLE employees ADD COLUMN swift TEXT;
ALTER TABLE employees ADD COLUMN insurer TEXT;
ALTER TABLE employees ADD COLUMN insurance_policy_number TEXT;
