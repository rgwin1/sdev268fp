package com.mycompany.payrollsystem.dao;

import com.mycompany.payrollsystem.models.PayrollRecord;
import com.mycompany.payrollsystem.utils.DatabaseManager;
import com.mycompany.payrollsystem.models.PayrollRow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * dao for payroll records.
 * provides insert, per-employee history fetch, full-table fetches,
 * and convenience rows for reporting/sign-off views.
 */
public class PayrollDAO {
    //shared connection created at construction
    private Connection conn;

    /**
     * builds a dao and opens a connection using DatabaseManager
     */
    public PayrollDAO() {
        try {
            this.conn = DatabaseManager.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * inserts a single payroll record
     *
     * @param record payroll record to insert
     * @return true if insert succeeded
     */
    public boolean insertPayrollRecord(PayrollRecord record) {
        String sql = """
            INSERT INTO payroll (
                employeeid, payPeriodStart, payPeriodEnd, payDate, hoursWorked,
                overtimeHours, wageAtTime, grossPay, taxWithheld, netPay, isSignedOff
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            //bind parameters in column order
            stmt.setString(1, record.getEmployeeId());
            stmt.setString(2, record.getPayPeriodStart());
            stmt.setString(3, record.getPayPeriodEnd());
            stmt.setString(4, record.getPayDate());
            stmt.setDouble(5, record.getHoursWorked());
            stmt.setDouble(6, record.getOvertimeHours());
            stmt.setDouble(7, record.getWageAtTime());
            stmt.setDouble(8, record.getGrossPay());
            stmt.setDouble(9, record.getTaxWithheld());
            stmt.setDouble(10, record.getNetPay());
            stmt.setInt(11, record.isSignedOff() ? 1 : 0);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting payroll record: " + e.getMessage());
            return false;
        }
    }

    /**
     * fetches payroll history for one employee ordered by most recent period start
     *
     * @param employeeid employee id to filter by
     * @return list of payroll records, empty if none
     */
    public List<PayrollRecord> fetchPayrollByEmployeeId(String employeeid) {
        List<PayrollRecord> history = new ArrayList<>();
        String sql = """
            SELECT * FROM payroll
            WHERE employeeid = ?
            ORDER BY payPeriodStart DESC
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, employeeid);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                //map row to domain model
                PayrollRecord record = new PayrollRecord(
                    rs.getString("employeeid"),
                    rs.getString("payPeriodStart"),
                    rs.getString("payPeriodEnd"),
                    rs.getString("payDate"),
                    rs.getDouble("hoursWorked"),
                    rs.getDouble("overtimeHours"),
                    rs.getDouble("wageAtTime"),
                    rs.getDouble("grossPay"),
                    rs.getDouble("taxWithheld"),
                    rs.getDouble("netPay"),
                    rs.getInt("isSignedOff") == 1
                );
                history.add(record);
            }
        } catch (SQLException e) {
            System.out.println("Error loading payroll history: " + e.getMessage());
        }

        return history;
    }

    /**
     * fetches all payroll records for all employees
     *
     * @return list of payroll records ordered by employee then period start
     */
    public List<PayrollRecord> fetchAllPayrollRecords() {
        List<PayrollRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM payroll ORDER BY employeeid, payPeriodStart";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                //collect each row
                records.add(new PayrollRecord(
                    rs.getString("employeeid"),
                    rs.getString("payPeriodStart"),
                    rs.getString("payPeriodEnd"),
                    rs.getString("payDate"),
                    rs.getDouble("hoursWorked"),
                    rs.getDouble("overtimeHours"),
                    rs.getDouble("wageAtTime"),
                    rs.getDouble("grossPay"),
                    rs.getDouble("taxWithheld"),
                    rs.getDouble("netPay"),
                    rs.getInt("isSignedOff") == 1
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading payroll records: " + e.getMessage());
        }

        return records;
    }

    /**
     * builds report-friendly rows by joining payroll with salary_info
     * and calculating per-period deductions and taxes
     *
     * @return list of payroll rows
     */
    public List<PayrollRow> fetchAllPayrollRows() {
        List<PayrollRow> rows = new ArrayList<>();
        String sql = "SELECT p.employeeid, p.payPeriodStart, p.payPeriodEnd, p.hoursWorked, p.wageAtTime, p.grossPay, p.taxWithheld, p.netPay, p.isSignedOff, " +
                     "s.medicalCoverage, s.numDependents " +
                     "FROM payroll p " +
                     "JOIN salary_info s ON p.employeeid = s.employeeid " +
                     "ORDER BY p.employeeid, p.payPeriodStart";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                //pull core payroll fields
                String employeeId = rs.getString("employeeid");
                String payPeriodStart = rs.getString("payPeriodStart");
                double hoursWorked = rs.getDouble("hoursWorked");
                double wage = rs.getDouble("wageAtTime");
                double grossPay = rs.getDouble("grossPay");
                double netPay = rs.getDouble("netPay");

                //from salary_info
                String medicalCoverage = rs.getString("medicalCoverage");
                int numDependents = rs.getInt("numDependents");

                //recalculate deductions per project spec
                double medicalDeduction = medicalCoverage.equalsIgnoreCase("Family") ? 100 : 50; //single=50, family=100
                double dependentStipend = 45 * numDependents; //45 per dependent

                //pretax basis per spec
                double taxableIncome = grossPay - medicalDeduction - dependentStipend;

                //tax breakdowns
                double stateTax = taxableIncome * 0.0315;       //IN 3.15%
                double federalTax = taxableIncome * 0.0765;     //employee 7.65%
                double socialSecurity = taxableIncome * 0.062;  //6.2%
                double medicare = taxableIncome * 0.0145;       //1.45%

                //compose a row for reports/preview screens
                PayrollRow row = new PayrollRow(
                    employeeId,
                    payPeriodStart,
                    hoursWorked,
                    wage,
                    grossPay,
                    medicalDeduction,
                    dependentStipend,
                    stateTax,
                    federalTax,
                    socialSecurity,
                    medicare,
                    netPay
                );

                rows.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error loading payroll rows: " + e.getMessage());
        }

        return rows;
    }

    /**
     * sets isSignedOff=1 for a specific employee and pay period
     *
     * @param employeeId id to update
     * @param startDate pay period start (yyyy-mm-dd)
     * @param endDate pay period end (yyyy-mm-dd)
     */
    public void signOffPayroll(String employeeid, String startDate, String endDate) {
        String sql = """
            UPDATE payroll
            SET isSignedOff = 1
            WHERE employeeid = ? AND payPeriodStart = ? AND payPeriodEnd = ?
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employeeid);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
