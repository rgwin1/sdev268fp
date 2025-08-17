package com.mycompany.payrollsystem.models;

import javafx.beans.property.*;

/**
 * model for a payroll row in payroll reports.
 * combines payroll amounts with deductions and stipends
 * for display in JavaFX tables.
 */
public class PayrollRow {

    //employee id this row belongs to
    private final StringProperty employeeId;
    //pay period label (start-end combined)
    private final StringProperty payPeriod;
    //total hours worked
    private final DoubleProperty hoursWorked;
    //hourly wage or salary rate at the time
    private final DoubleProperty wageAtTime;
    //gross pay before deductions
    private final DoubleProperty grossPay;
    //medical coverage deduction (based on plan type)
    private final DoubleProperty medicalDeduction;
    //stipend for dependents
    private final DoubleProperty dependentStipend;
    //state income tax deduction
    private final DoubleProperty stateTax;
    //federal income tax deduction
    private final DoubleProperty federalTax;
    //social security deduction
    private final DoubleProperty socialSecurity;
    //medicare deduction
    private final DoubleProperty medicare;
    //final net pay after deductions
    private final DoubleProperty netPay;

    /**
     * constructs a payroll row with all fields.
     *
     * @param employeeId employee id
     * @param payPeriod pay period label
     * @param hoursWorked total hours worked
     * @param wageAtTime wage at the time of payroll
     * @param grossPay gross pay
     * @param medicalDeduction medical deduction amount
     * @param dependentStipend stipend for dependents
     * @param stateTax state tax
     * @param federalTax federal tax
     * @param socialSecurity social security tax
     * @param medicare medicare tax
     * @param netPay net pay after deductions
     */
    public PayrollRow(String employeeId, String payPeriod, double hoursWorked, double wageAtTime,
                      double grossPay, double medicalDeduction, double dependentStipend,
                      double stateTax, double federalTax, double socialSecurity,
                      double medicare, double netPay) {

        this.employeeId = new SimpleStringProperty(employeeId);
        this.payPeriod = new SimpleStringProperty(payPeriod);
        this.hoursWorked = new SimpleDoubleProperty(hoursWorked);
        this.wageAtTime = new SimpleDoubleProperty(wageAtTime);
        this.grossPay = new SimpleDoubleProperty(grossPay);
        this.medicalDeduction = new SimpleDoubleProperty(medicalDeduction);
        this.dependentStipend = new SimpleDoubleProperty(dependentStipend);
        this.stateTax = new SimpleDoubleProperty(stateTax);
        this.federalTax = new SimpleDoubleProperty(federalTax);
        this.socialSecurity = new SimpleDoubleProperty(socialSecurity);
        this.medicare = new SimpleDoubleProperty(medicare);
        this.netPay = new SimpleDoubleProperty(netPay);
    }

    //getters and JavaFX property accessors
    public String getEmployeeId() { return employeeId.get(); }
    public StringProperty employeeIdProperty() { return employeeId; }

    public String getPayPeriod() { return payPeriod.get(); }
    public StringProperty payPeriodProperty() { return payPeriod; }

    public double getHoursWorked() { return hoursWorked.get(); }
    public DoubleProperty hoursWorkedProperty() { return hoursWorked; }

    public double getWageAtTime() { return wageAtTime.get(); }
    public DoubleProperty wageAtTimeProperty() { return wageAtTime; }

    public double getGrossPay() { return grossPay.get(); }
    public DoubleProperty grossPayProperty() { return grossPay; }

    public double getMedicalDeduction() { return medicalDeduction.get(); }
    public DoubleProperty medicalDeductionProperty() { return medicalDeduction; }

    public double getDependentStipend() { return dependentStipend.get(); }
    public DoubleProperty dependentStipendProperty() { return dependentStipend; }

    public double getStateTax() { return stateTax.get(); }
    public DoubleProperty stateTaxProperty() { return stateTax; }

    public double getFederalTax() { return federalTax.get(); }
    public DoubleProperty federalTaxProperty() { return federalTax; }

    public double getSocialSecurity() { return socialSecurity.get(); }
    public DoubleProperty socialSecurityProperty() { return socialSecurity; }

    public double getMedicare() { return medicare.get(); }
    public DoubleProperty medicareProperty() { return medicare; }

    public double getNetPay() { return netPay.get(); }
    public DoubleProperty netPayProperty() { return netPay; }
}
