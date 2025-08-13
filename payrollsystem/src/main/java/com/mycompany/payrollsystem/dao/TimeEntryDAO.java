package com.mycompany.payrollsystem.dao;

import com.mycompany.payrollsystem.models.TimeEntry;
import com.mycompany.payrollsystem.utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimeEntryDAO {
    private Connection conn;

    public TimeEntryDAO() {
        try {
            this.conn = DatabaseManager.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // create table if it doesn't exist
    public void createTimeEntriesTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS time_entries (
                employeeid TEXT NOT NULL,
                date TEXT NOT NULL,
                hoursWorked REAL NOT NULL,
                isPTO INTEGER NOT NULL,
                isLocked INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (employeeId, date)
            );
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
            System.out.println("time_entries table ready.");
        } catch (SQLException e) {
            System.err.println("error creating time_entries table: " + e.getMessage());
        }
    }

    // add new entry
   public boolean insertTimeEntry(String employeeId, String date, double hoursWorked, boolean isPTO) {
    String sql = "INSERT INTO time_entries (employeeid, date, hoursWorked, isPTO) VALUES (?, ?, ?, ?)";
    
    // check if entry is locked before inserting
    if (isEntryLocked(employeeId, date)) {
    System.err.println("Attempted to modify locked entry for " + employeeId + " on " + date);
    return false;
}
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, employeeId);
        stmt.setString(2, date);
        stmt.setDouble(3, hoursWorked);
        stmt.setInt(4, isPTO ? 1 : 0);
        stmt.executeUpdate();
        return true;
    } catch (SQLException e) {
        System.err.println("Error inserting time entry: " + e.getMessage());
        return false;
    }
}


    // fetch all entries for a given employee
    public List<TimeEntry> fetchTimeEntriesByEmployeeId(String employeeId) {
        List<TimeEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM time_entries WHERE employeeid = ? ORDER BY date DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String date = rs.getString("date");
                double hours = rs.getDouble("hoursWorked");
                boolean isPTO = rs.getInt("isPTO") == 1;
                boolean isLocked = rs.getInt("isLocked") == 1;

                entries.add(new TimeEntry(employeeId, date, hours, isPTO, isLocked));
            }
        } catch (SQLException e) {
            System.err.println("error fetching time entries: " + e.getMessage());
        }

        return entries;
    }

    // lock entries for a specific pay period (to prevent edits after payroll)
    public void lockEntries(String employeeId, String startDate, String endDate) {
        String sql = """
            UPDATE time_entries
            SET isLocked = 1
            WHERE employeeid = ? AND date BETWEEN ? AND ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("error locking time entries: " + e.getMessage());
        }
    }
    public boolean isEntryLocked(String employeeId, String date) {
    String sql = "SELECT isLocked FROM time_entries WHERE employeeid = ? AND date = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, employeeId);
        stmt.setString(2, date);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("isLocked") == 1;
        }
    } catch (SQLException e) {
        System.err.println("error checking lock status: " + e.getMessage());
    }
    return false;
}

}
