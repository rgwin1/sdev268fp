package com.mycompany.payrollsystem.models;

import javafx.beans.property.*;

public class PayrollRow {

    private final StringProperty employeeId;
    private final StringProperty payPeriod;
    private final DoubleProperty hoursWorked;
    private final DoubleProperty wageAtTime;
    private final DoubleProperty grossPay;
    private final DoubleProperty medicalDeduction;
    private final DoubleProperty dependentStipend;
    private final DoubleProperty stateTax;
    private final DoubleProperty federalTax;
    private final DoubleProperty socialSecurity;
    private final DoubleProperty medicare;
    private final DoubleProperty netPay;

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
