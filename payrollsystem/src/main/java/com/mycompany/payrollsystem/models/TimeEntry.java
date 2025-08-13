package com.mycompany.payrollsystem.models;

public class TimeEntry {
    private final String employeeId;
    private final String date; // format: YYYY-MM-DD
    private final double hoursWorked;
    private final boolean pto;
    private final boolean locked;

    public TimeEntry(String employeeId, String date, double hoursWorked, boolean pto, boolean locked) {
        this.employeeId = employeeId;
        this.date = date;
        this.hoursWorked = hoursWorked;
        this.pto = pto;
        this.locked = locked;
    }

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
