package com.mycompany.payrollsystem.controllers;

import com.mycompany.payrollsystem.models.Employee;
import com.mycompany.payrollsystem.dao.EmployeeDAO;
import com.mycompany.payrollsystem.dao.UserDAO;
import com.mycompany.payrollsystem.dao.SalaryInfoDAO;
import com.mycompany.payrollsystem.models.SalaryInfo;
import com.mycompany.payrollsystem.utils.InputValidator;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;

public class EditEmployeeController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField middleNameField;
    @FXML private ComboBox<String> statusField;
    @FXML private ComboBox<String>genderField;
    @FXML private ComboBox<String> payTypeField;
    @FXML ComboBox<String> roleField;
    @FXML private TextField addressLine1Field;
    @FXML private TextField addressLine2Field;
    @FXML private TextField cityField;
    @FXML private TextField stateField;
    @FXML private TextField zipField;
    @FXML private TextField dobField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField wageField;
    @FXML private Button saveButton;
    @FXML private Button backButton;
    @FXML private Label statusLabel;
    @FXML private Button updateEmployeeButton;
    @FXML private TextField departmentField;
    @FXML private TextField jobTitleField;
    @FXML private TextField hireDateField;
    @FXML private ComboBox<String> coverageField;
    @FXML private TextField dependentsField;



    private Employee selectedEmployee;

public void setEmployeeToEdit(Employee employee) {
    this.selectedEmployee = employee;

    firstNameField.setText(employee.getFirstName());
    lastNameField.setText(employee.getLastName());
    middleNameField.setText(employee.getMiddleName());
    dobField.setText(employee.getDob());
    phoneField.setText(employee.getPhone());
    emailField.setText(employee.getEmail());
    addressLine1Field.setText(employee.getAddressLine1());
    addressLine2Field.setText(employee.getAddressLine2());
    cityField.setText(employee.getCity());
    stateField.setText(employee.getState());
    zipField.setText(employee.getZip());
    statusField.setValue(employee.getStatus());
    genderField.setValue(employee.getGender());
    payTypeField.setValue(employee.getPayType());

    // fetch and set user role
    UserDAO userDAO = new UserDAO();
    String role = userDAO.getRoleByEmployeeId(employee.getEmployeeId());
    roleField.setValue(role);
    
    SalaryInfoDAO salaryDAO = new SalaryInfoDAO();
SalaryInfo salary = salaryDAO.fetchSalaryInfoByEmployeeId(employee.getEmployeeId());

if (salary != null) {
    departmentField.setText(salary.getDepartment());
    jobTitleField.setText(salary.getJobTitle());
    hireDateField.setText(salary.getHireDate());
    payTypeField.setValue(salary.getPayType());
    wageField.setText(String.valueOf(salary.getWage()));
    coverageField.setValue(salary.getMedicalCoverage());
    dependentsField.setText(String.valueOf(salary.getNumDependents()));
}
    
}


 @FXML
private void onUpdateEmployeeClick() {
    if (selectedEmployee == null) {
        statusLabel.setText("No employee loaded.");
        return;
    }

    try {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String middleName = middleNameField.getText().trim();
        String dob = dobField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address1 = addressLine1Field.getText().trim();
        String address2 = addressLine2Field.getText().trim();
        String city = cityField.getText().trim();
        String state = stateField.getText().trim();
        String zip = zipField.getText().trim();
        String status = statusField.getValue();
        String gender = genderField.getValue();
        String payType = payTypeField.getValue();
        String department = departmentField.getText().trim();
        String jobTitle = jobTitleField.getText().trim();
        String hireDate = hireDateField.getText().trim();
        String wageText = wageField.getText().trim();
        String medicalCoverage = coverageField.getValue();
        String dependentsText = dependentsField.getText().trim();

        // validations
        if (!InputValidator.isNonEmpty(firstName) || !InputValidator.isAlphabetic(firstName)) {
            statusLabel.setText("First name is required and must be alphabetic.");
            return;
        }
        if (!InputValidator.isNonEmpty(lastName) || !InputValidator.isAlphabetic(lastName)) {
            statusLabel.setText("Last name is required and must be alphabetic.");
            return;
        }
        if (!InputValidator.isValidDateOfBirth(dob)) {
            statusLabel.setText("Invalid DOB or under 18.");
            return;
        }
        if (!InputValidator.isValidEmail(email)) {
            statusLabel.setText("Invalid email format.");
            return;
        }
        if (!InputValidator.isValidGender(gender)) {
            statusLabel.setText("Gender must be Male or Female.");
            return;
        }
        if (!InputValidator.isValidPayType(payType)) {
            statusLabel.setText("Pay type must be Hourly or Salary.");
            return;
        }
        if (!InputValidator.isValidState(state)) {
            statusLabel.setText("State must be 2-letter code.");
            return;
        }
        if (!InputValidator.isValidZip(zip)) {
            statusLabel.setText("Zip must be 5 digits.");
            return;
        }
        if (!InputValidator.isValidDate(hireDate)) {
            statusLabel.setText("Invalid hire date.");
            return;
        }
        if (!InputValidator.isValidMedicalCoverage(medicalCoverage)) {
            statusLabel.setText("Coverage must be Single or Family.");
            return;
        }

        double wage;
        int dependents;

        try {
            wage = Double.parseDouble(wageText);
            if (!InputValidator.isValidWage(wage)) {
                statusLabel.setText("Wage must be at least $7.25.");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid wage format.");
            return;
        }

        try {
            dependents = Integer.parseInt(dependentsText);
            if (!InputValidator.isValidDependents(dependents)) {
                statusLabel.setText("Dependents must be 0 or more.");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid dependents format.");
            return;
        }

        selectedEmployee.setFirstName(firstName);
        selectedEmployee.setLastName(lastName);
        selectedEmployee.setMiddleName(middleName);
        selectedEmployee.setDob(dob);
        selectedEmployee.setPhone(phone);
        selectedEmployee.setEmail(email);
        selectedEmployee.setAddressLine1(address1);
        selectedEmployee.setAddressLine2(address2);
        selectedEmployee.setCity(city);
        selectedEmployee.setState(state);
        selectedEmployee.setZip(zip);
        selectedEmployee.setStatus(status);
        selectedEmployee.setGender(gender);
        selectedEmployee.setPayType(payType);

        EmployeeDAO dao = new EmployeeDAO();
        boolean success = dao.updateEmployee(selectedEmployee);

        String selectedRole = roleField.getValue();
        UserDAO userDAO = new UserDAO();
        boolean roleUpdated = userDAO.updateRole(selectedEmployee.getEmployeeId(), selectedRole);

        SalaryInfoDAO salaryDAO = new SalaryInfoDAO();
        SalaryInfo salary = new SalaryInfo(
            selectedEmployee.getEmployeeId(),
            department,
            jobTitle,
            hireDate,
            payType,
            wage,
            medicalCoverage,
            dependents
        );
        boolean salaryUpdated = salaryDAO.updateSalaryInfo(salary);

        if (!salaryUpdated) {
            statusLabel.setText("Employee updated, but salary info failed.");
        } else if (!roleUpdated) {
            statusLabel.setText("Employee and salary updated, but role change failed.");
        } else {
            statusLabel.setText(success ? "Employee, role, and salary updated." : "Update failed.");
        }

    } catch (Exception e) {
        statusLabel.setText("Error updating: " + e.getMessage());
        e.printStackTrace();
    }
}


    @FXML
    private void onBackClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_dashboard.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
