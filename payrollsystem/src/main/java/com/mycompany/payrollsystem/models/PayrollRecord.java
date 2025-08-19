package com.mycompany.payrollsystem.models;

/**
 * model for a payroll record.
 * represents one pay period's financial details for an employee.
 */
public class PayrollRecord {
    //employee id foreign key
    private final String employeeid;
    //start date of pay period yyyy-mm-dd
    private final String payPeriodStart;
    //end date of pay period yyyy-mm-dd
    private final String payPeriodEnd;
    //actual pay date yyyy-mm-dd
    private final String payDate;
    //total regular hours worked
    private final double hoursWorked;
    //overtime hours
    private final double overtimeHours;
    //hourly wage or salary rate at the time of payroll
    private final double wageAtTime;
    //gross pay before deductions
    private final double grossPay;
    //tax withheld from paycheck
    private final double taxWithheld;
    //net pay after deductions
    private final double netPay;
    //flag if employee signed off payroll
    private final boolean isSignedOff;

    /**
     * constructs a payroll record with all fields.
     *
     * @param employeeid employee id
     * @param payPeriodStart start date of pay period
     * @param payPeriodEnd end date of pay period
     * @param payDate actual pay date
     * @param hoursWorked regular hours worked
     * @param overtimeHours overtime hours worked
     * @param wageAtTime wage or salary rate at time
     * @param grossPay gross pay
     * @param taxWithheld taxes withheld
     * @param netPay net pay after deductions
     * @param isSignedOff true if record signed off by employee
     */
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

    //getters for payroll record fields
    public String getEmployeeId() { return employeeid; }

    public String getPayPeriodStart() { return payPeriodStart; }

    public String getPayPeriodEnd() { return payPeriodEnd; }
     
    public String getPayPeriod() { return payPeriodStart + " to " + payPeriodEnd;}

    public String getPayDate() { return payDate; }

    public double getHoursWorked() { return hoursWorked; }

    public double getOvertimeHours() { return overtimeHours; }

    public double getWageAtTime() { return wageAtTime; }

    public double getGrossPay() { return grossPay; }

    public double getTaxWithheld() { return taxWithheld; }

    public double getNetPay() { return netPay; }
    
    public double getDeductions() { return grossPay - netPay;}

    public boolean isSignedOff() { return isSignedOff; }
}
