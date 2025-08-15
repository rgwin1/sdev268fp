package com.mycompany.payrollsystem.dao;

import com.mycompany.payrollsystem.models.Employee;
import com.mycompany.payrollsystem.utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class EmployeeDAO {
    private Connection conn;
    
    public EmployeeDAO() {
        try {
            this.conn = DatabaseManager.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> employeeList = new ArrayList<>();
        
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
    public boolean addEmployee(Employee emp) {
    String sql = "INSERT INTO employees (employeeid, firstName, lastName, middleName, dob, phone, email, status, gender, payType, addressLine1, addressLine2, city, state, zip) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
   public boolean updateEmployee(Employee emp) {
    String sql = "UPDATE employees SET firstName = ?, middleName = ?,  lastName = ?, dob = ?, phone = ?, email = ?, status = ?, gender = ?, payType = ?, addressLine1 = ?, addressLine2 = ?, city = ?, state = ?, zip = ?" +
                 "WHERE employeeid = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
public String getNextEmployeeId() {
    String query = "SELECT employeeid FROM employees ORDER BY employeeid DESC LIMIT 1";

    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        if (rs.next()) {
            String lastId = rs.getString("employeeid"); // e.g., "E005"
            int num = Integer.parseInt(lastId.substring(1)); // → 5
            return String.format("E%03d", num + 1); // → E006
        }
    } catch (SQLException | NumberFormatException e) {
        System.out.println("error generating next employee id: " + e.getMessage());
    }

    return "E001"; // default if empty
}

public boolean deleteEmployeeEverywhere(String employeeId) {
    String[] tables = { "time_entries", "payroll", "users", "employees" };
    try (Connection conn = DatabaseManager.getConnection()) {
        conn.setAutoCommit(false);

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

public Employee getEmployeeByName(String firstName, String lastName) {
    String sql = "SELECT * FROM employees WHERE UPPER(firstName)=UPPER(?) AND UPPER(lastName)=UPPER(?) LIMIT 1";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
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




 

    