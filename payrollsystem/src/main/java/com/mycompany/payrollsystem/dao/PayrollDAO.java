package com.mycompany.payrollsystem.dao;

import com.mycompany.payrollsystem.models.PayrollRecord;
import com.mycompany.payrollsystem.utils.DatabaseManager;
import com.mycompany.payrollsystem.models.PayrollRow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PayrollDAO {
    private Connection conn;

    public PayrollDAO() {
        try {
            this.conn = DatabaseManager.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

public boolean insertPayrollRecord(PayrollRecord record) {
    String sql = """
        INSERT INTO payroll (
            employeeid, payPeriodStart, payPeriodEnd, payDate, hoursWorked,
            overtimeHours, wageAtTime, grossPay, taxWithheld, netPay, isSignedOff
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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


    public List<PayrollRecord> fetchAllPayrollRecords() {
        List<PayrollRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM payroll ORDER BY employeeid, payPeriodStart";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
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
                 rs.getInt("isSignedOff") == 1));
            }
        } catch (SQLException e) {
            System.err.println("Error loading payroll records: " + e.getMessage());
        }

        return records;
    }
    public List<PayrollRow> fetchAllPayrollRows() {
    List<PayrollRow> rows = new ArrayList<>();
    String sql = "SELECT p.employeeid, p.payPeriodStart, p.payPeriodEnd, p.hoursWorked, p.wageAtTime, p.grossPay, p.taxWithheld, p.netPay, p.isSignedOff, " +
                 "s.medicalCoverage, s.numDependents " +
                 "FROM payroll p " +
                 "JOIN salary_info s ON p.employeeid = s.employeeid " +
                 "ORDER BY p.employeeid, p.payPeriod";

    try (PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            String employeeId = rs.getString("employeeid");
            String payPeriodStart = rs.getString("payPeriodStart");
            double hoursWorked = rs.getDouble("hoursWorked");
            double wage = rs.getDouble("wageAtTime");
            double grossPay = rs.getDouble("grossPay");
            double netPay = rs.getDouble("netPay");
   

            // From salary_info
            String medicalCoverage = rs.getString("medicalCoverage");
            int numDependents = rs.getInt("numDependents");

            // Recalculate deductions
            double medicalDeduction = medicalCoverage.equalsIgnoreCase("Family") ? 100 : 50;
            double dependentStipend = 45 * numDependents;

            double taxableIncome = grossPay - medicalDeduction - dependentStipend;

            double stateTax = taxableIncome * 0.0315;
            double federalTax = taxableIncome * 0.0765;
            double socialSecurity = taxableIncome * 0.062;
            double medicare = taxableIncome * 0.0145;

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
    public void signOffPayroll(String employeeId, String startDate, String endDate) {
    String sql = """
        UPDATE payroll
        SET isSignedOff = 1
        WHERE employeeId = ? AND payPeriodStart = ? AND payPeriodEnd = ?
    """;
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, employeeId);
        stmt.setString(2, startDate);
        stmt.setString(3, endDate);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


}
