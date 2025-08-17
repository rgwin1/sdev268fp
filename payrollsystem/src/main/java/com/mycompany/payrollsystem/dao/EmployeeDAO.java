package com.mycompany.payrollsystem.dao;

import com.mycompany.payrollsystem.models.Employee;
import com.mycompany.payrollsystem.utils.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * dao for employee records.
 * provides basic crud operations and lookups against the employees table.
 */
public class EmployeeDAO {
    //shared connection created at construction
    private Connection conn;

    /**
     * builds a dao and opens a connection using DatabaseManager
     */
    public EmployeeDAO() {
        try {
            this.conn = DatabaseManager.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * gets all employees from the database
     *
     * @return list of employees, empty if none
     * @throws SQLException if statement or query fails
     */
    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> employeeList = new ArrayList<>();

        //select explicit columns to match the employee constructor
        String sql = "SELECT employeeid, firstName, lastName, middleName, dob, phone, email, status, gender, payType, addressLine1, addressLine2, city, state, zip FROM employees";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                Employee emp = new Employee(
                        rs.getString("employeeid"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("middleName"),
                        rs.getString("dob"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("status"),
                        rs.getString("gender"),
                        rs.getString("payType"),
                        rs.getString("addressLine1"),
                        rs.getString("addressLine2"),
                        rs.getString("city"),
                        rs.getString("state"),
                        rs.getString("zip")
                );
                employeeList.add(emp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employeeList;
    }

    /**
     * inserts a new employee row
     *
     * @param emp employee to add
     * @return true if at least one row inserted
     */
    public boolean addEmployee(Employee emp) {
        String sql = "INSERT INTO employees (employeeid, firstName, lastName, middleName, dob, phone, email, status, gender, payType, addressLine1, addressLine2, city, state, zip) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            //bind parameters in the same order as the column list
            stmt.setString(1, emp.getEmployeeId());
            stmt.setString(2, emp.getFirstName());
            stmt.setString(3, emp.getLastName());
            stmt.setString(4, emp.getMiddleName());
            stmt.setString(5, emp.getDob());
            stmt.setString(6, emp.getPhone());
            stmt.setString(7, emp.getEmail());
            stmt.setString(8, emp.getStatus());
            stmt.setString(9, emp.getGender());
            stmt.setString(10, emp.getPayType());
            stmt.setString(11, emp.getAddressLine1());
            stmt.setString(12, emp.getAddressLine2());
            stmt.setString(13, emp.getCity());
            stmt.setString(14, emp.getState());
            stmt.setString(15, emp.getZip());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            System.err.println("Error adding employee: " + e.getMessage());
            return false;
        }
    }

    /**
     * updates an existing employee by employeeid
     *
     * @param emp employee containing new values
     * @return true if at least one row updated
     */
    public boolean updateEmployee(Employee emp) {
        String sql = "UPDATE employees SET firstName = ?, middleName = ?,  lastName = ?, dob = ?, phone = ?, email = ?, status = ?, gender = ?, payType = ?, addressLine1 = ?, addressLine2 = ?, city = ?, state = ?, zip = ?" +
                     " WHERE employeeid = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            //bind values in the same order as the set clause
            stmt.setString(1, emp.getFirstName());
            stmt.setString(2, emp.getMiddleName());
            stmt.setString(3, emp.getLastName());
            stmt.setString(4, emp.getDob());
            stmt.setString(5, emp.getPhone());
            stmt.setString(6, emp.getEmail());
            stmt.setString(7, emp.getStatus());
            stmt.setString(8, emp.getGender());
            stmt.setString(9, emp.getPayType());
            stmt.setString(10, emp.getAddressLine1());
            stmt.setString(11, emp.getAddressLine2());
            stmt.setString(12, emp.getCity());
            stmt.setString(13, emp.getState());
            stmt.setString(14, emp.getZip());
            stmt.setString(15, emp.getEmployeeId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.err.println("Error updating employee: " + e.getMessage());
            return false;
        }
    }

    /**
     * deletes a single employee by id
     *
     * @param employeeid id to delete
     * @return true if a row was deleted
     */
    public boolean deleteEmployeeById(String employeeid) {
        String sql = "DELETE FROM employees WHERE employeeid = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, employeeid);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            return false;
        }
    }

    /**
     * generates the next employee id by reading the current max and incrementing
     * assumes ids are like E001, E002, ...
     *
     * @return next id or E001 on error/empty table
     */
    public String getNextEmployeeId() {
        String query = "SELECT employeeid FROM employees ORDER BY employeeid DESC LIMIT 1";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                String lastId = rs.getString("employeeid"); //e.g., E005
                int num = Integer.parseInt(lastId.substring(1)); //→ 5
                //pad to 3 digits
                return String.format("E%03d", num + 1); //→ E006
            }
        } catch (SQLException | NumberFormatException e) {
            System.out.println("error generating next employee id: " + e.getMessage());
        }

        return "E001"; //default if empty
    }

    /**
     * deletes an employee across related tables within a single transaction
     * deletion order assumes child tables reference employees by employeeid
     *
     * @param employeeId id to delete everywhere
     * @return true if commit succeeds
     */
    public boolean deleteEmployeeEverywhere(String employeeId) {
        String[] tables = { "time_entries", "payroll", "users", "employees" };
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            //delete from each table referencing employeeid
            for (String table : tables) {

                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + table + " WHERE employeeid = ?")) {
                    stmt.setString(1, employeeId);
                    stmt.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting employee from all tables: " + e.getMessage());
            return false;
        }
    }

    /**
     * gets a single employee by id using a parameterized query
     *
     * @param employeeId id to look up
     * @return employee or null if not found
     */
    public Employee getEmployeeById(String employeeId) {
        String sql = "SELECT * FROM employees WHERE employeeid = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                return new Employee(
                        rs.getString("employeeid"),
                        rs.getString("firstName"),
                        rs.getString("middleName"),
                        rs.getString("lastName"),
                        rs.getString("dob"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("status"),
                        rs.getString("gender"),
                        rs.getString("payType"),
                        rs.getString("addressLine1"),
                        rs.getString("addressLine2"),
                        rs.getString("city"),
                        rs.getString("state"),
                        rs.getString("zip")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error fetching employee by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * gets a single employee by case-insensitive first and last name
     *
     * @param firstName first name, case-insensitive
     * @param lastName last name, case-insensitive
     * @return matching employee or null if none
     */
    public Employee getEmployeeByName(String firstName, String lastName) {
        String sql = "SELECT * FROM employees WHERE UPPER(firstName)=UPPER(?) AND UPPER(lastName)=UPPER(?) LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            //normalize inputs to avoid nulls and stray spaces
            ps.setString(1, firstName == null ? "" : firstName.trim());
            ps.setString(2, lastName == null ? "" : lastName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    return new Employee(
                            rs.getString("employeeid"),
                            rs.getString("firstName"),
                            rs.getString("middleName"),
                            rs.getString("lastName"),
                            rs.getString("dob"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("status"),
                            rs.getString("gender"),
                            rs.getString("payType"),
                            rs.getString("addressLine1"),
                            rs.getString("addressLine2"),
                            rs.getString("city"),
                            rs.getString("state"),
                            rs.getString("zip")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching employee by name: " + e.getMessage());
        }
        return null;
    }
}
