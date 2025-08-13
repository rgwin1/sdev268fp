package com.mycompany.payrollsystem.dao;

import com.mycompany.payrollsystem.utils.DatabaseManager;
import com.mycompany.payrollsystem.utils.HashUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private Connection conn;

    public UserDAO() {
        try {
            this.conn = DatabaseManager.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    public void insertDefaultAdmin() {
        String sqlCheck = "SELECT 1 FROM users WHERE employeeid = 'HR0001'";
        try (PreparedStatement checkStmt = conn.prepareStatement(sqlCheck)) {
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                String sqlInsert = "INSERT INTO users (employeeid, password, role) VALUES (?, ?, ?)";
                String hashedPassword = HashUtil.hashPassword("password123");
                try (PreparedStatement insertStmt = conn.prepareStatement(sqlInsert)) {
                    insertStmt.setString(1, "HR0001");
                    insertStmt.setString(2, hashedPassword);
                    insertStmt.setString(3, "admin");
                    insertStmt.executeUpdate();
                    System.out.println("Default admin inserted.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to insert default admin: " + e.getMessage());
        }
    }

    public boolean addUser(String employeeid, String password, String role) {
        String hashedPassword = HashUtil.hashPassword(password);
        String sql = "INSERT INTO users (employeeid, password, role) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, employeeid);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role.toLowerCase());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("error adding user: " + e.getMessage());
            return false;
        }
    }

    public boolean validateUser(String employeeid, String inputPassword, String role) {
        String sql = "SELECT password FROM users WHERE employeeid = ? AND role = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, employeeid);
            stmt.setString(2, role.toLowerCase());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                return HashUtil.verifyPassword(inputPassword, storedHash);
            }
        } catch (SQLException e) {
            System.err.println("error validating user: " + e.getMessage());
        }

        return false;
    }

    public String getRoleByEmployeeId(String employeeId) {
        String sql = "SELECT role FROM users WHERE employeeid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving role: " + e.getMessage());
        }
        return null;
    }

    public boolean updateRole(String employeeId, String role) {
        String sql = "UPDATE users SET role = ? WHERE employeeid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role);
            stmt.setString(2, employeeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating role: " + e.getMessage());
            return false;
        }
    }
    public void syncEmployeesToUsers() {
    String sql = """
        SELECT e.employeeid, e.email, e.dob
        FROM employees e
        LEFT JOIN users u ON e.employeeid = u.employeeid
        WHERE u.employeeid IS NULL
    """;

    try (PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            String empId = rs.getString("employeeid");
            String email = rs.getString("email");
            String dob = rs.getString("dob"); // stored as YYYY-MM-DD
            String role;
            String rawPassword;

            if (empId.equals("HR0001")) {
                role = "admin";
                rawPassword = "password123";
            } else {
                role = "employee";

                // Extract part before '@'
                String emailPrefix = email.contains("@") ? email.split("@")[0] : email;

                // Remove dashes from DOB
                String dobFormatted = dob.replace("-", "");

                rawPassword = emailPrefix + dobFormatted;
            }

            String hashedPassword = HashUtil.hashPassword(rawPassword);

            try (PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO users (employeeid, password, role) VALUES (?, ?, ?)")) {
                insertStmt.setString(1, empId);
                insertStmt.setString(2, hashedPassword);
                insertStmt.setString(3, role);
                insertStmt.executeUpdate();
                System.out.println("User account created for employee: " + empId);
            }
        }

    } catch (SQLException e) {
        System.err.println("Error syncing employees to users: " + e.getMessage());
    }
}

}
