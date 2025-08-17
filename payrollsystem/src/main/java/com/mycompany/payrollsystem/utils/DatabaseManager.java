package com.mycompany.payrollsystem.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.mycompany.payrollsystem.dao.UserDAO;

/**
 * utility class for managing the SQLite database.
 * handles connections and initializes required tables.
 */
public class DatabaseManager {

    //path to SQLite database file
    private static final String dbPath = "jdbc:sqlite:payroll.db";

    /**
     * opens and returns a connection to the database.
     *
     * @return connection object
     * @throws SQLException if database connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbPath);
    }

    /**
     * creates the employees table if it does not exist.
     */
    public static void initEmployeeTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS employees (
                employeeid TEXT PRIMARY KEY,
                firstName TEXT NOT NULL,
                lastName TEXT NOT NULL,
                middleName TEXT,
                dob TEXT NOT NULL,
                phone TEXT NOT NULL,
                email TEXT NOT NULL,
                status TEXT NOT NULL,
                gender TEXT NOT NULL,
                payType TEXT NOT NULL,
                addressLine1 TEXT NOT NULL,
                addressLine2 TEXT,
                city TEXT NOT NULL,
                state TEXT NOT NULL,
                zip TEXT NOT NULL
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("employee table ready.");
        } catch (SQLException e) {
            System.out.println("error creating employee table: " + e.getMessage());
        }
    }

    /**
     * creates the users table if it does not exist.
     */
    public static void initUsersTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                employeeid TEXT PRIMARY KEY,
                password TEXT NOT NULL,
                role TEXT NOT NULL
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("users table ready.");
        } catch (SQLException e) {
            System.out.println("error creating users table: " + e.getMessage());
        }
    }

    /**
     * creates the payroll table if it does not exist.
     */
    public static void initPayrollTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS payroll (
                employeeid TEXT NOT NULL,
                payPeriodStart TEXT NOT NULL,
                payPeriodEnd TEXT NOT NULL,
                payDate TEXT NOT NULL,
                hoursWorked REAL NOT NULL,
                overtimeHours REAL NOT NULL,
                wageAtTime REAL NOT NULL,
                grossPay REAL NOT NULL,
                taxWithheld REAL NOT NULL,
                netPay REAL NOT NULL,
                isSignedOff INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (employeeid, payPeriodStart, payPeriodEnd)
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("payroll table ready.");
        } catch (SQLException e) {
            System.err.println("error creating payroll table: " + e.getMessage());
        }
    }

    /**
     * creates the salary_info table if it does not exist.
     */
    public static void initSalaryInfoTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS salary_info (
                employeeid TEXT PRIMARY KEY,
                department TEXT NOT NULL,
                jobTitle TEXT NOT NULL,
                hireDate TEXT NOT NULL,
                payType TEXT NOT NULL,
                wage REAL NOT NULL,
                medicalCoverage TEXT NOT NULL,
                numDependents INTEGER NOT NULL,
                FOREIGN KEY (employeeid) REFERENCES employees(employeeid)
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("salary_info table ready.");
        } catch (SQLException e) {
            System.err.println("error creating salary_info table: " + e.getMessage());
        }
    }

    /**
     * creates the time_entries table if it does not exist.
     */
    public static void initTimeEntriesTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS time_entries (
                employeeid TEXT NOT NULL,
                date TEXT NOT NULL,
                hoursWorked REAL NOT NULL,
                isPTO INTEGER NOT NULL,
                isLocked INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (employeeid, date)
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("time_entries table ready.");
        } catch (SQLException e) {
            System.err.println("error creating time_entries table: " + e.getMessage());
        }
    }

    /**
     * initializes all required tables and syncs default users.
     * ensures admin and employee accounts exist.
     */
    public static void initAllTables() {
        initUsersTable();
        initEmployeeTable();
        initPayrollTable();
        initTimeEntriesTable();
        initSalaryInfoTable();

        //sync with users table
        UserDAO userDAO = new UserDAO();
        userDAO.insertDefaultAdmin();
        userDAO.syncEmployeesToUsers();
    }
}
