package com.mycompany.payrollsystem.models;

/**
 * model for a single employee time entry.
 * tracks work date, hours worked, pto, and lock status.
 */
public class TimeEntry {
    //employee id this entry belongs to
    private final String employeeId;
    //date of the entry (format: yyyy-mm-dd)
    private final String date;
    //number of hours worked for that day
    private final double hoursWorked;
    //true if entry is paid time off instead of worked hours
    private final boolean pto;
    //true if entry is locked (cannot be edited after payroll processing)
    private final boolean locked;

    /**
     * constructs a TimeEntry with all fields.
     *
     * @param employeeId employee id
     * @param date date in yyyy-mm-dd format
     * @param hoursWorked hours worked that day
     * @param pto true if pto day
     * @param locked true if entry locked
     */
    public TimeEntry(String employeeId, String date, double hoursWorked, boolean pto, boolean locked) {
        this.employeeId = employeeId;
        this.date = date;
        this.hoursWorked = hoursWorked;
        this.pto = pto;
        this.locked = locked;
    }

    //getters only since entries are immutable once created
    public String getEmployeeId() {
        return employeeId;
    }

    public String getDate() {
        return date;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }

    public boolean isPto() {
        return pto;
    }

    public boolean isLocked() {
        return locked;
    }
}
