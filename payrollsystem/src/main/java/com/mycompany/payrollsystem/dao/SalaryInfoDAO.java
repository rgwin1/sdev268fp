package com.mycompany.payrollsystem.dao;

import com.mycompany.payrollsystem.models.SalaryInfo;
import com.mycompany.payrollsystem.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * dao for salary_info table.
 * provides read and write operations tied to an employee id.
 */
public class SalaryInfoDAO {

    /**
     * fetches salary info for a single employee
     *
     * @param employeeid employee id to look up
     * @return SalaryInfo or null if not found
     */
    public SalaryInfo fetchSalaryInfoByEmployeeId(String employeeid) {
        String sql = "SELECT department, jobTitle, hireDate, payType, wage, medicalCoverage, numDependents " +
                     "FROM salary_info WHERE employeeid = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            //bind id
            stmt.setString(1, employeeid);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                //map row to domain model
                return new SalaryInfo(
                    employeeid,
                    rs.getString("department"),
                    rs.getString("jobTitle"),
                    rs.getString("hireDate"),
                    rs.getString("payType"),
                    rs.getDouble("wage"),
                    rs.getString("medicalCoverage"),
                    rs.getInt("numDependents")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error fetching salary info: " + e.getMessage());
        }

        return null;
    }

    /**
     * inserts a new salary_info row
     *
     * @param salary SalaryInfo to insert
     * @return true if insert succeeded
     */
    public boolean addSalaryInfo(SalaryInfo salary) {
        String sql = "INSERT INTO salary_info (employeeid, department, jobTitle, hireDate, payType, wage, medicalCoverage, numDependents) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            //bind parameters in column order
            stmt.setString(1, salary.getEmployeeId());
            stmt.setString(2, salary.getDepartment());
            stmt.setString(3, salary.getJobTitle());
            stmt.setString(4, salary.getHireDate());
            stmt.setString(5, salary.getPayType());
            stmt.setDouble(6, salary.getWage());
            stmt.setString(7, salary.getMedicalCoverage());
            stmt.setInt(8, salary.getNumDependents());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error inserting salary info: " + e.getMessage());
            return false;
        }
    }

    /**
     * updates existing salary_info row by employee id
     *
     * @param salary SalaryInfo containing new values
     * @return true if at least one row updated
     */
    public boolean updateSalaryInfo(SalaryInfo salary) {
        String sql = "UPDATE salary_info SET " +
                     "department = ?, " +
                     "jobTitle = ?, " +
                     "hireDate = ?, " +
                     "payType = ?, " +
                     "wage = ?, " +
                     "medicalCoverage = ?, " +
                     "numDependents = ? " +
                     "WHERE employeeid = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            //bind values matching the set clause
            stmt.setString(1, salary.getDepartment());
            stmt.setString(2, salary.getJobTitle());
            stmt.setString(3, salary.getHireDate());
            stmt.setString(4, salary.getPayType());
            stmt.setDouble(5, salary.getWage());
            stmt.setString(6, salary.getMedicalCoverage());
            stmt.setInt(7, salary.getNumDependents());
            stmt.setString(8, salary.getEmployeeId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating salary info: " + e.getMessage());
            return false;
        }
    }
}
