package com.mycompany.payrollsystem.models;

import javafx.beans.property.*;

public class SalaryInfo {

    private final StringProperty employeeId;
    private final StringProperty department;
    private final StringProperty jobTitle;
    private final StringProperty hireDate;
    private final StringProperty payType;
    private final DoubleProperty wage;
    private final StringProperty medicalCoverage;
    private final IntegerProperty numDependents;

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
