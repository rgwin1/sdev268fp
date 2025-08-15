package com.mycompany.payrollsystem.reports;

import com.mycompany.payrollsystem.utils.DatabaseManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HRReportGenerator {

    public static class Row {
        public final String department;
        public final String fullName;
        public final String employeeId;
        public final double grossPay;
        public final double netPay;
        public final String payPeriodStart;
        public final String payPeriodEnd;
        public final String payDate;
        public final double hoursWorked;
        public final double overtimeHours;
        public final double wageAtTime;
        public final double taxWithheld;
        public final boolean isSignedOff;

        public Row(String department, String fullName, String employeeId, double grossPay, double netPay,
                   String payPeriodStart, String payPeriodEnd, String payDate, double hoursWorked,
                   double overtimeHours, double wageAtTime, double taxWithheld, boolean isSignedOff) {
            this.department = department;
            this.fullName = fullName;
            this.employeeId = employeeId;
            this.grossPay = grossPay;
            this.netPay = netPay;
            this.payPeriodStart = payPeriodStart;
            this.payPeriodEnd = payPeriodEnd;
            this.payDate = payDate;
            this.hoursWorked = hoursWorked;
            this.overtimeHours = overtimeHours;
            this.wageAtTime = wageAtTime;
            this.taxWithheld = taxWithheld;
            this.isSignedOff = isSignedOff;
        }
    }

    public List<Row> getPayrollData(LocalDate start, LocalDate end) {
        List<Row> rows = new ArrayList<>();
        String sql = """
            SELECT COALESCE(s.department,'Unassigned') AS department,
                   e.firstName || ' ' || e.lastName AS fullName,
                   e.employeeid,
                   p.grossPay, p.netPay, p.payPeriodStart, p.payPeriodEnd, p.payDate,
                   p.hoursWorked, p.overtimeHours, p.wageAtTime, p.taxWithheld, p.isSignedOff
            FROM payroll p
            JOIN employees e ON e.employeeid = p.employeeid
            LEFT JOIN salary_info s ON s.employeeid = e.employeeid
            WHERE p.payPeriodStart >= ? AND p.payPeriodEnd <= ?
            ORDER BY department, fullName
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, start.toString());
            stmt.setString(2, end.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(new Row(
                    rs.getString("department"),
                    rs.getString("fullName"),
                    rs.getString("employeeid"),
                    rs.getDouble("grossPay"),
                    rs.getDouble("netPay"),
                    rs.getString("payPeriodStart"),
                    rs.getString("payPeriodEnd"),
                    rs.getString("payDate"),
                    rs.getDouble("hoursWorked"),
                    rs.getDouble("overtimeHours"),
                    rs.getDouble("wageAtTime"),
                    rs.getDouble("taxWithheld"),
                    rs.getInt("isSignedOff") == 1
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading payroll data: " + e.getMessage());
        }
        return rows;
    }

    public void generateReport(List<Row> data, String filePath, String payPeriod) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(filePath))) {
            w.write("HR Payroll Report");
            w.newLine();
            w.write("======");
            w.newLine();
            w.write("Pay Period: " + payPeriod);
            w.newLine();
            w.newLine();

            if (data.isEmpty()) {
                w.write("No payroll records found for this period.");
                w.newLine();
                return;
            }

            String currentDept = null;
            double deptGross = 0, deptNet = 0;
            double totalGross = 0, totalNet = 0;

            for (Row r : data) {
                if (currentDept == null || !currentDept.equals(r.department)) {
                    if (currentDept != null) {
                        w.write(String.format("Department Subtotal (Gross: %.2f, Net: %.2f)", deptGross, deptNet));
                        w.newLine();
                        w.newLine();
                    }
                    currentDept = r.department;
                    deptGross = 0;
                    deptNet = 0;
                    w.write("Department: " + currentDept);
                    w.newLine();
                    w.write("Employee ID | Name | Gross | Net | Hours | OT | Wage | Signed");
                    w.newLine();
                }

                w.write(String.format("%s | %s | %.2f | %.2f | %.2f | %.2f | %.2f | %s",
                        r.employeeId, r.fullName, r.grossPay, r.netPay, r.hoursWorked, r.overtimeHours,
                        r.wageAtTime, r.isSignedOff ? "Y" : "N"));
                w.newLine();

                deptGross += r.grossPay;
                deptNet += r.netPay;
                totalGross += r.grossPay;
                totalNet += r.netPay;
            }

            w.write(String.format("Department Subtotal (Gross: %.2f, Net: %.2f)", deptGross, deptNet));
            w.newLine();
            w.newLine();
            w.write(String.format("Grand Total (Gross: %.2f, Net: %.2f)", totalGross, totalNet));
            w.newLine();
        }
    }
}
