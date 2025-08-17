package com.mycompany.payrollsystem.dao;

import com.mycompany.payrollsystem.models.TimeEntry;
import com.mycompany.payrollsystem.utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * dao for time_entries.
 * supports creation, insert, fetch, locking, and lock checks.
 */
public class TimeEntryDAO {
    //shared connection created at construction
    private Connection conn;

    /**
     * builds a dao and opens a connection using DatabaseManager
     */
    public TimeEntryDAO() {
        try {
            this.conn = DatabaseManager.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * creates the time_entries table if missing
     * columns:
     * - employeeid text
     * - date text (yyyy-mm-dd recommended)
     * - hoursWorked real
     * - isPTO integer 0 or 1
     * - isLocked integer 0 or 1
     * primary key on (employeeid, date)
     */
    public void createTimeEntriesTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS time_entries (
                employeeid TEXT NOT NULL,
                date TEXT NOT NULL,
                hoursWorked REAL NOT NULL,
                isPTO INTEGER NOT NULL,
                isLocked INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (employeeid, date)
            );
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
            System.out.println("time_entries table ready.");
        } catch (SQLException e) {
            System.err.println("error creating time_entries table: " + e.getMessage());
        }
    }

    /**
     * inserts a new time entry for an employee on a date
     *
     * @param employeeid employee id
     * @param date yyyy-mm-dd
     * @param hoursWorked number of hours
     * @param isPTO true if pto day
     * @return true if insert succeeded
     */
    public boolean insertTimeEntry(String employeeid, String date, double hoursWorked, boolean isPTO) {
        String sql = "INSERT INTO time_entries (employeeid, date, hoursWorked, isPTO) VALUES (?, ?, ?, ?)";

        //check if entry is locked before inserting
        if (isEntryLocked(employeeid, date)) {
            System.err.println("Attempted to modify locked entry for " + employeeid + " on " + date);
            return false;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            //bind parameters in column order
            stmt.setString(1, employeeid);
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

    /**
     * fetches all time entries for an employee ordered by most recent date
     *
     * @param employeeid employee id
     * @return list of time entries, empty if none
     */
    public List<TimeEntry> fetchTimeEntriesByEmployeeId(String employeeid) {
        List<TimeEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM time_entries WHERE employeeid = ? ORDER BY date DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, employeeid);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                //map row to domain model
                String date = rs.getString("date");
                double hours = rs.getDouble("hoursWorked");
                boolean isPTO = rs.getInt("isPTO") == 1;
                boolean isLocked = rs.getInt("isLocked") == 1;

                entries.add(new TimeEntry(employeeid, date, hours, isPTO, isLocked));
            }
        } catch (SQLException e) {
            System.err.println("error fetching time entries: " + e.getMessage());
        }

        return entries;
    }

    /**
     * locks entries within a date range so they cannot be edited post payroll
     *
     * @param employeeid employee id
     * @param startDate inclusive start yyyy-mm-dd
     * @param endDate inclusive end yyyy-mm-dd
     */
    public void lockEntries(String employeeid, String startDate, String endDate) {
        String sql = """
            UPDATE time_entries
            SET isLocked = 1
            WHERE employeeid = ? AND date BETWEEN ? AND ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            //bind values
            stmt.setString(1, employeeid);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("error locking time entries: " + e.getMessage());
        }
    }

    /**
     * checks if a specific entry is locked
     *
     * @param employeeid employee id
     * @param date yyyy-mm-dd
     * @return true if locked
     */
    public boolean isEntryLocked(String employeeid, String date) {
        String sql = "SELECT isLocked FROM time_entries WHERE employeeid = ? AND date = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            //bind values
            stmt.setString(1, employeeid);
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
