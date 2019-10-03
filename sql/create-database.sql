DROP DATABASE IF EXISTS four_finance;
CREATE DATABASE four_finance;
\c four_finance;

DROP TABLE IF EXISTS customer;

CREATE TABLE IF NOT EXISTS customer (
   id BIGSERIAL PRIMARY KEY,
   email VARCHAR(255) UNIQUE NOT NULL
);

DROP TABLE IF EXISTS loan;

CREATE TABLE IF NOT EXISTS loan (
  id BIGSERIAL PRIMARY KEY,
  customer_id BIGINT NOT NULL REFERENCES customer(id),
  loan_amount DECIMAL NOT NULL,
  remaining_loan_amount DECIMAL NOT NULL,
  term INT NOT NULL,
  original_term INT NOT NULL,
  weekly_percentage_rate DECIMAL NOT NULL,
  original_weekly_percentage_rate DECIMAL NOT NULL,
  date_created TIMESTAMP
);