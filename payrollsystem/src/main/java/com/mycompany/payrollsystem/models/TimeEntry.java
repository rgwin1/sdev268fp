package com.mycompany.payrollsystem.models;

/**
 * model for a single employee time entry.
 * tracks work date, hours worked, pto, and lock status.
 */
public class TimeEntry {
    //employee id this entry belongs to
    private String employeeid;
    //date of the entry (format: yyyy-mm-dd)
    private  String date;
    //number of hours worked for that day
    private double hoursWorked;
    //true if entry is paid time off instead of worked hours
    private boolean pto;
    //true if entry is locked (cannot be edited after payroll processing)
    private boolean locked;
    
    /**
     * no arg constructor for TimeEntry
     */
    public TimeEntry() {}

    /**
     * constructs a TimeEntry with all fields.
     *
     * @param employeeid employee id
     * @param date date in yyyy-mm-dd format
     * @param hoursWorked hours worked that day
     * @param pto true if pto day
     * @param locked true if entry locked
     */
    public TimeEntry(String employeeid, String date, double hoursWorked, boolean pto, boolean locked) {
        this.employeeid = employeeid;
        this.date = date;
        this.hoursWorked = hoursWorked;
        this.pto = pto;
        this.locked = locked;
    }

    //getters and setters
    public String getEmployeeId() {
        return employeeid;
    }
    public void setEmployeeId(String employeeid) {
        this.employeeid = employeeid;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }
    public void setHoursWorked(Double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public boolean isPto() {
        return pto;
    }
    public void setPto(boolean pto) {
        this.pto = pto;
    }

    public boolean isLocked() {
        return locked;
    }
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
