# Parking Booking System

**Course:** Principles of Database Management (Semester 1, 2026–2027)  
**Tech Stack:** Java Swing (JDK 21), JDBC (`mysql-connector-j`), MySQL 8.4  

---

## 1. Project Overview
The **Parking Booking System** is a desktop management application integrated with a normalized relational database (BCNF, 12 tables) designed to manage parking lots, parking bays, vehicle registrations, customer reservations, payments, barrier check-in/out logs, and overstay penalties.

### Repository Structure
```text
Parking-Booking-System/
├── backend/
│   └── src/
│       ├── dao/                     # Data Access Objects (12 entities)
│       ├── database/                # JDBC connection manager (DBConnection.java)
│       ├── model/                   # Domain POJO models
│       ├── parkingbookingsystem/    # Java Swing UI Frames & Main entry point
│       ├── service/                 # Business logic & bay status synchronization
│       └── db.properties.example    # Sample database configuration template
├── sql/
│   ├── 01_create_database.sql       # DDL schema (12 BCNF tables, PKs, FKs, CHECKs)
│   ├── 02_sample_data.sql           # Realistic sample dataset
│   ├── 03_queries.sql               # 12 analytical queries & summary view
│   ├── 04_transactions.sql          # ACID COMMIT & ROLLBACK test scripts
│   ├── 05_indexes.sql               # 8 composite BTREE performance indexes
│   └── README.md                    # Database setup & execution guide
├── Screenshots/
│   └── Testing_Screenshots_Evidence.docx  # 35 verified test screenshots (Phase 2 & 3)
├── Test_Cases.xlsx                  # Complete test plan (33 Test Cases)
├── Test_Results.xlsx                # Execution report, Dashboard & Bug Tracker
└── README.md                        # Project overview & setup instructions
```

---

## 2. Setup & Execution Guide

### Step 1: Initialize the MySQL Database
1. Open **MySQL Workbench** or **DBeaver** and connect to your MySQL 8.0+ server.
2. Execute the scripts in `/sql` strictly in numerical order (`01` $\rightarrow$ `05`). See `sql/README.md` for detailed notes.

### Step 2: Configure JDBC Connection
1. Navigate to `backend/src/`.
2. Copy `db.properties.example` to `db.properties` in the project root directory and update your MySQL credentials:
   ```properties
   db.url=jdbc:mysql://localhost:3306/ParkingBookingSystem
   db.username=root
   db.password=your_password
   ```

### Step 3: Build & Run the Java Application
1. Open the project in **Apache NetBeans IDE** (or IntelliJ IDEA / Eclipse).
2. Add `mysql-connector-j-8.4.0.jar` to Project **Libraries**.
3. Run `parkingbookingsystem.ParkingBookingSystem` (`Shift + F6`) to launch the **Bookings Management** interface.

---

## 3. Quality Assurance & Testing Summary
* **Tester:** Le Hai Dang (`ITITWE24037`)
* **Total Test Cases Executed:** **33 / 33 PASSED (100%)**
  * **Phase 2 (Database Integrity, ACID Transactions, Indexes & Queries):** 27/27 Passed (`TC-01` – `TC-15b`, `TC-Q01` – `TC-Q12`).
  * **Phase 3 (Java Swing UI & MySQL End-to-End Integration):** 6/6 Passed (`TC-16` – `TC-21c`).
* **Defects & Observations Tracked:** All 3 integration defects/observations (`OBS-03` bay status sync in `BookingService.java`, `OBS-04` main entry point, and `OBS-05` script naming consistency) have been resolved and verified.
* Full test specifications, execution logs, and screenshot evidence are documented in `Test_Cases.xlsx`, `Test_Results.xlsx`, and `Screenshots/Testing_Screenshots_Evidence.docx`.