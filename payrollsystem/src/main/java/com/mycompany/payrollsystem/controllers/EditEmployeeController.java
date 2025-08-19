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

/**
 * controller for editing an existing employee record
 * loads the employee data into the form, allows the user to update values,
 * validates input, and pushes changes to the database (employee, user role, salary info)
 */

public class EditEmployeeController {
        
    //fields mapped to the fxml form
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


    //employee currently being edited
    private Employee selectedEmployee;
/**
 * populate the form fields with an employee's current data. 
 * also fetches role and salary info from their respective daos.
 * @param employee the employee objected being edited
 */
public void setEmployeeToEdit(Employee employee) {
    
    this.selectedEmployee = employee;
    //populate form fields from employee object
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

    //fetch and set user role from userdao
    UserDAO userDAO = new UserDAO();
    String role = userDAO.getRoleByEmployeeId(employee.getEmployeeId());
    roleField.setValue(role);
    
    //fetch salary info from salaryinfodao
    SalaryInfoDAO salaryDAO = new SalaryInfoDAO();
    SalaryInfo salary = salaryDAO.fetchSalaryInfoByEmployeeId(employee.getEmployeeId());

    //if salary info exists, populate the salary-related fields
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

/**
 * handles the update button click
 * validates all fields, updates the employee object
 * saves changes to employee, user role, and salary info tables
 */
 @FXML
private void onUpdateEmployeeClick() {
    if (selectedEmployee == null) {
        statusLabel.setText("No employee loaded.");
        return;
    }
    
    try {
        //grab all fields values
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

        //validations for each field
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

        //parse and validate wage
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
        
        //parse and validate dependents
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
        
        //update the selected employee object
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
        
        //push changes to db
        EmployeeDAO dao = new EmployeeDAO();
        boolean success = dao.updateEmployee(selectedEmployee);
        
        //update role in userdao
        String selectedRole = roleField.getValue();
        UserDAO userDAO = new UserDAO();
        boolean roleUpdated = userDAO.updateRole(selectedEmployee.getEmployeeId(), selectedRole);
        
        //update salary info in salaryinfodao
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
        
        //determine outcome message
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

/**
 * navigates back to the admin dashboard screen
 * @throws IOException if fxml cannot be loaded
 */
    @FXML
    private void onBackClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/manage_employees.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = new Scene(root, 800, 700);
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        stage.setScene(scene);
    }
}
