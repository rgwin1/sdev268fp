package com.mycompany.payrollsystem.dao;

import com.mycompany.payrollsystem.utils.DatabaseManager;
import com.mycompany.payrollsystem.utils.HashUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * dao for users table.
 * handles user creation, validation, role lookups and syncing from employees.
 */
public class UserDAO {
    //shared connection created at construction
    private Connection conn;

    /**
     * builds a dao and opens a connection using DatabaseManager
     */
    public UserDAO() {
        try {
            this.conn = DatabaseManager.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * inserts the default admin user HR0001 if missing
     * password is hashed using HashUtil
     */
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

    /**
     * adds a new user with a hashed password
     *
     * @param employeeid employee id
     * @param password plain text password to hash
     * @param role role name like admin or employee
     * @return true if insert succeeded
     */
    public boolean addUser(String employeeid, String password, String role) {
        String hashedPassword = HashUtil.hashPassword(password);
        String sql = "INSERT INTO users (employeeid, password, role) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            //bind parameters
            stmt.setString(1, employeeid);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role.toLowerCase()); //normalize role
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("error adding user: " + e.getMessage());
            return false;
        }
    }

    /**
     * validates a user by checking the role and verifying the password hash
     *
     * @param employeeid employee id
     * @param inputPassword plain text password
     * @param role expected role for the login
     * @return true if credentials are valid
     */
    public boolean validateUser(String employeeid, String inputPassword, String role) {
        String sql = "SELECT password FROM users WHERE employeeid = ? AND role = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            //bind filters
            stmt.setString(1, employeeid);
            stmt.setString(2, role.toLowerCase()); //normalize role
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                //verify hash matches the provided password
                return HashUtil.verifyPassword(inputPassword, storedHash);
            }
        } catch (SQLException e) {
            System.err.println("error validating user: " + e.getMessage());
        }

        return false;
    }

    /**
     * gets the role for a given employee id
     *
     * @param employeeId employee id
     * @return role string or null if missing
     */
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

    /**
     * updates the role for a given employee id
     *
     * @param employeeid employee id
     * @param role new role string
     * @return true if at least one row updated
     */
    public boolean updateRole(String employeeid, String role) {
        String sql = "UPDATE users SET role = ? WHERE employeeid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);
            stmt.setString(2, employeeid);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating role: " + e.getMessage());
            return false;
        }
    }

    /**
     * syncs employees into users table where a user does not yet exist
     * generates a role and initial password derived from email and dob
     * passwords are hashed before insert
     */
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
                String dob = rs.getString("dob"); //stored as yyyy-mm-dd
                String role;
                String rawPassword;

                if (empId.equals("HR0001")) {
                    //bootstrap admin
                    role = "admin";
                    rawPassword = "password123";
                } else {
                    //regular employee
                    role = "employee";

                    //extract part before '@'
                    String emailPrefix = email.contains("@") ? email.split("@")[0] : email;

                    //remove dashes from dob
                    String dobFormatted = dob.replace("-", "");

                    //compose default password rule per spec
                    rawPassword = emailPrefix + dobFormatted;
                }

                //hash the generated password
                String hashedPassword = HashUtil.hashPassword(rawPassword);

                //insert the new user
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
