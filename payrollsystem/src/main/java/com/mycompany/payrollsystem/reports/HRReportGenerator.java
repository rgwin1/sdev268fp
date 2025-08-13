package com.mycompany.payrollsystem.reports;

import com.mycompany.payrollsystem.models.PayrollRecord;
import com.mycompany.payrollsystem.utils.DatabaseManager;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class HRReportGenerator {

    public Map<String, Map<String, List<PayrollRecord>>> getPayrollData(LocalDate start, LocalDate end) {
        Map<String, Map<String, List<PayrollRecord>>> data = new LinkedHashMap<>();

        String sql = """
      SELECT s.department,
                   e.firstName || ' ' || e.lastName AS fullName,
                   p.employeeid,
                   p.grossPay,
                   p.netPay,
                   p.payPeriodStart,
                   p.payPeriodEnd,
                   p.payDate,
                   p.hoursWorked,
                   p.overtimeHours,
                   p.wageAtTime,
                   p.taxWithheld,
                   p.isSignedOff
            FROM employees e
            JOIN salary_info s ON e.employeeid = s.employeeid
            JOIN payroll p ON e.employeeid = p.employeeid
            WHERE p.payPeriodStart >= ? 
              AND p.payPeriodEnd <= ?
            ORDER BY s.department, fullName;
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, start.toString());
            stmt.setString(2, end.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String dept = rs.getString("department");
                String empName = rs.getString("fullName");

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
                        rs.getBoolean("isSignedOff")
                );

                data.putIfAbsent(dept, new LinkedHashMap<>());
                data.get(dept).putIfAbsent(empName, new ArrayList<>());
                data.get(dept).get(empName).add(record);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }

public void generateReport(Map<String, Map<String, List<PayrollRecord>>> data, String filePath, String payPeriod) {
    try (FileWriter writer = new FileWriter(filePath)) {
        writer.write("HR Payroll Report\n");
        writer.write("=================\n\n");
        
        // Add pay period header
        writer.write("Pay Period: " + payPeriod + "\n\n");
        
        recurseDepartments(data, writer);
    } catch (IOException e) {
        e.printStackTrace();
    }
}


    //recursive department traversal
    private void recurseDepartments(Map<String, Map<String, List<PayrollRecord>>> departments, FileWriter writer) throws IOException {
        for (String dept : departments.keySet()) {
            writer.write("Department: " + dept + "\n");
            recurseEmployees(departments.get(dept), writer);
            writer.write("\n");
        }
    }

    //recursive employee traversal
    private void recurseEmployees(Map<String, List<PayrollRecord>> employees, FileWriter writer) throws IOException {
        for (String emp : employees.keySet()) {
            writer.write("  Employee: " + emp + "\n");
            recursePayrollRecords(employees.get(emp), writer);
        }
    }

    //base case: list of payroll records
   private void recursePayrollRecords(List<PayrollRecord> records, FileWriter writer) throws IOException {
    for (PayrollRecord rec : records) {
        writer.write(String.format(
            "    Period: %s to %s | Pay Date: %s | Hours: %.2f | OT Hours: %.2f | Wage: %.2f | Gross: %.2f | Net: %.2f | Tax: %.2f\n",
            rec.getPayPeriodStart(), rec.getPayPeriodEnd(),
            rec.getPayDate(),
            rec.getHoursWorked(), rec.getOvertimeHours(),
            rec.getWageAtTime(),
            rec.getGrossPay(), rec.getNetPay(),
            rec.getTaxWithheld()
        ));
    }
}
}
