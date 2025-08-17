package com.mycompany.payrollsystem.models;

import javafx.beans.property.*;

/**
 * model for salary-related information tied to an employee.
 * includes department, job, hire date, wage, and benefits.
 */
public class SalaryInfo {

    //unique id for the employee this record belongs to
    private final StringProperty employeeId;
    //department name (e.g., HR, IT, Sales)
    private final StringProperty department;
    //job title (e.g., Manager, Developer)
    private final StringProperty jobTitle;
    //hire date stored as yyyy-mm-dd
    private final StringProperty hireDate;
    //pay type (salary or hourly)
    private final StringProperty payType;
    //wage amount (per hour if hourly, per period if salary)
    private final DoubleProperty wage;
    //medical coverage plan (e.g., Family, Single)
    private final StringProperty medicalCoverage;
    //number of dependents
    private final IntegerProperty numDependents;

    /**
     * constructs a SalaryInfo object with all fields.
     *
     * @param employeeId employee id
     * @param department department name
     * @param jobTitle job title
     * @param hireDate hire date in yyyy-mm-dd format
     * @param payType pay type (hourly/salary)
     * @param wage wage amount
     * @param medicalCoverage medical coverage type
     * @param numDependents number of dependents
     */
    public SalaryInfo(String employeeId, String department, String jobTitle, String hireDate,
                      String payType, double wage, String medicalCoverage, int numDependents) {
        this.employeeId = new SimpleStringProperty(employeeId);
        this.department = new SimpleStringProperty(department);
        this.jobTitle = new SimpleStringProperty(jobTitle);
        this.hireDate = new SimpleStringProperty(hireDate);
        this.payType = new SimpleStringProperty(payType);
        this.wage = new SimpleDoubleProperty(wage);
        this.medicalCoverage = new SimpleStringProperty(medicalCoverage);
        this.numDependents = new SimpleIntegerProperty(numDependents);
    }

    //getters and JavaFX property accessors
    public String getEmployeeId() { return employeeId.get(); }
    public StringProperty employeeIdProperty() { return employeeId; }

    public String getDepartment() { return department.get(); }
    public StringProperty departmentProperty() { return department; }

    public String getJobTitle() { return jobTitle.get(); }
    public StringProperty jobTitleProperty() { return jobTitle; }

    public String getHireDate() { return hireDate.get(); }
    public StringProperty hireDateProperty() { return hireDate; }

    public String getPayType() { return payType.get(); }
    public StringProperty payTypeProperty() { return payType; }

    public double getWage() { return wage.get(); }
    public DoubleProperty wageProperty() { return wage; }

    public String getMedicalCoverage() { return medicalCoverage.get(); }
    public StringProperty medicalCoverageProperty() { return medicalCoverage; }

    public int getNumDependents() { return numDependents.get(); }
    public IntegerProperty numDependentsProperty() { return numDependents; }
}
