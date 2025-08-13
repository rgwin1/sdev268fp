package com.mycompany.payrollsystem.models;

public class PayrollRecord {
    private final String employeeid;
    private final String payPeriodStart;
    private final String payPeriodEnd;
    private final String payDate;
    private final double hoursWorked;
    private final double overtimeHours;
    private final double wageAtTime;
    private final double grossPay;
    private final double taxWithheld;
    private final double netPay;
    private final boolean isSignedOff;

    public PayrollRecord(String employeeid, String payPeriodStart, String payPeriodEnd, String payDate,
                         double hoursWorked, double overtimeHours,
                         double wageAtTime, double grossPay,
                         double taxWithheld, double netPay, boolean isSignedOff) {
        this.employeeid = employeeid;
        this.payPeriodStart = payPeriodStart;
        this.payPeriodEnd = payPeriodEnd;
        this.payDate = payDate;
        this.hoursWorked = hoursWorked;
        this.overtimeHours = overtimeHours;
        this.wageAtTime = wageAtTime;
        this.grossPay = grossPay;
        this.taxWithheld = taxWithheld;
        this.netPay = netPay;
        this.isSignedOff = isSignedOff;
    }

    public String getEmployeeId() {
        return employeeid;
    }

    public String getPayPeriodStart() {
        return payPeriodStart;
    }
    public String getPayPeriodEnd() {
        return payPeriodEnd;
    }

    public String getPayDate() {
        return payDate;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public double getWageAtTime() {
        return wageAtTime;
    }

    public double getGrossPay() {
        return grossPay;
    }

    public double getTaxWithheld() {
        return taxWithheld;
    }

    public double getNetPay() {
        return netPay;
    }
    public boolean isSignedOff() {
        return isSignedOff;
    }
}
