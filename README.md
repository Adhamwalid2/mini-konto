# Mini-Konto – Banking Backend with Test Automation

A small banking backend (accounts, deposits, withdrawals, transfers) built with **Java 17 and Spring Boot**,
focused on **systematic software testing**.

![Tests](../../actions/workflows/tests.yml/badge.svg)

## What it does

**Web interface** at `http://localhost:8080`: open accounts, deposit, withdraw, transfer, view transaction history.

**REST API:**

| Endpoint | Description |
|---|---|
| `GET /api/accounts` | List all accounts |
| `POST /api/accounts` | Open an account (`{"owner": "Anna", "initialDeposit": 100}`) |
| `GET /api/accounts/{id}` | Show an account and its balance |
| `POST /api/accounts/{id}/deposit` | Deposit money (`{"amount": 50}`) |
| `POST /api/accounts/{id}/withdraw` | Withdraw money |
| `POST /api/transfers` | Transfer money (`{"fromAccountId": 1, "toAccountId": 2, "amount": 25.50}`) |
| `GET /api/accounts/{id}/transfers` | Transfer history of an account |

**Business rules**
- No overdraft: an account can never go below 0.00
- Amounts must be positive and have at most 2 decimal places
- No transfers to the same account
- A transfer is **atomic**: either both balances change, or neither does (database transaction)
- Parallel changes to the same account are detected (optimistic locking)

Errors are returned as standard *Problem Details* (RFC 9457): `400` invalid input, `404` unknown account,
`422` insufficient funds.

## Tests

31 automated tests on three levels:

| Level | Class | What is checked |
|---|---|---|
| Unit | `AccountTest` | Business rules in isolation, incl. boundary values (exact balance, 1 cent too much, 3 decimals) |
| Service | `AccountServiceTest` | Transfers against the real SQL database (H2), rollback when a transfer fails |
| API | `ApiIntegrationTest` | REST endpoints like a real client: status codes and JSON responses |

- **Coverage:** ~91 % of instructions and branches (JaCoCo)
- **CI:** all tests run automatically on GitHub Actions on every push

## Run it

Requirements: Java 17+ (Maven is downloaded automatically by the wrapper).

```bash
# run all tests (coverage report: target/site/jacoco/index.html)
./mvnw test            # Windows: mvnw.cmd test

# start the app on http://localhost:8080
./mvnw spring-boot:run # Windows: mvnw.cmd spring-boot:run
```

Database console while the app runs: http://localhost:8080/h2-console
(JDBC URL `jdbc:h2:mem:minikonto`, user `sa`, no password).

## Tech stack

Java 17 · Spring Boot 4 · Spring Data JPA / Hibernate · H2 (SQL) · JUnit 5 · AssertJ · MockMvc · JaCoCo · GitHub Actions

## Roadmap

- [x] Banking backend with business rules
- [x] Unit, service and API tests, coverage, CI
- [ ] AI-assisted test generation: generate test cases from written requirements with an LLM and compare them with the hand-written tests
