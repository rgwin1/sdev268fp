package com.mycompany.payrollsystem.controllers;

import com.mycompany.payrollsystem.dao.EmployeeDAO;
import com.mycompany.payrollsystem.dao.UserDAO;
import com.mycompany.payrollsystem.models.Employee;
import com.mycompany.payrollsystem.models.SalaryInfo;
import com.mycompany.payrollsystem.dao.SalaryInfoDAO;
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
 * controller for the add employee screen.
 * handles validation, saving employee data, creating user login,
 * and adding salary info to the database
 * 
 */

public class AddEmployeeController {
    //form fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField middleNameField;
    @FXML private ComboBox<String> statusField;
    @FXML private ComboBox<String> genderField;
    @FXML private ComboBox<String> payTypeField;
    @FXML private ComboBox<String> roleField;
    @FXML private TextField addressLine1Field;
    @FXML private TextField addressLine2Field;
    @FXML private TextField cityField;
    @FXML private TextField stateField;
    @FXML private TextField zipField;
    @FXML private TextField dobField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField jobTitleField;
    @FXML private TextField wageField;
    @FXML private Button saveButton;
    @FXML private Button backButton;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> coverageField;
    @FXML private TextField dependentsField;
    @FXML private TextField departmentField;
    @FXML private TextField hireDateField;

    /**
     * runs when save button is clicked.
     * validates input fields and saves employee, user, and salary info to db.
     */
    @FXML
    private void onSaveEmployeeClick() {
        //get field values
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String middleName = middleNameField.getText().trim();
        String dob = dobField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String jobTitle = jobTitleField.getText().trim();
        String wageText = wageField.getText().trim();
        String status = statusField.getValue();
        String gender = genderField.getValue();
        String payType = payTypeField.getValue();
        String addressLine1 = addressLine1Field.getText().trim();
        String addressLine2 = addressLine2Field.getText().trim();
        String city = cityField.getText().trim();
        String state = stateField.getText().trim();
        String zip = zipField.getText().trim();
        String role = roleField.getValue();
        String department = departmentField.getText().trim();
        String hireDate = hireDateField.getText().trim();
        String medicalCoverage = coverageField.getValue();

        //validation checks
        if (!InputValidator.isNonEmpty(firstName) || !InputValidator.isAlphabetic(firstName)) {
            statusLabel.setText("First name is required and must be alphabetic.");
            return;
        }
        if (!InputValidator.isNonEmpty(lastName) || !InputValidator.isAlphabetic(lastName)) {
            statusLabel.setText("Last name is required and must be alphabetic.");
            return;
        }
        if (!InputValidator.isValidDateOfBirth(dob)) {
            statusLabel.setText("Invalid date of birth or under 18.");
            return;
        }
        if (!InputValidator.isValidEmail(email)) {
            statusLabel.setText("Invalid email format.");
            return;
        }
        if (!InputValidator.isValidGender(gender)) {
            statusLabel.setText("Invalid gender selection.");
            return;
        }
        if (!InputValidator.isValidPayType(payType)) {
            statusLabel.setText("Pay type must be 'Hourly' or 'Salary'.");
            return;
        }
        if (!InputValidator.isValidState(state)) {
            statusLabel.setText("State must be 2 uppercase letters.");
            return;
        }
        if (!InputValidator.isValidZip(zip)) {
            statusLabel.setText("Zip code must be 5 digits.");
            return;
        }
        if (!InputValidator.isNonEmpty(department)) {
            statusLabel.setText("Department is required.");
            return;
        }
        if (!InputValidator.isNonEmpty(jobTitle)) {
            statusLabel.setText("Job title is required.");
            return;
        }
        if (!InputValidator.isValidDate(hireDate)) {
            statusLabel.setText("Invalid hire date format.");
            return;
        }
        if (!InputValidator.isValidMedicalCoverage(medicalCoverage)) {
            statusLabel.setText("Coverage must be Single or Family.");
            return;
        }

        double parsedWage;
        int numDependents;
        //parse wage
        try {
            parsedWage = Double.parseDouble(wageText);
            if (!InputValidator.isValidWage(parsedWage)) {
                statusLabel.setText("Wage must be at least $7.25.");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid wage format.");
            return;
        }
        
        //parse dependents
        try {
            numDependents = Integer.parseInt(dependentsField.getText().trim());
            if (!InputValidator.isValidDependents(numDependents)) {
                statusLabel.setText("Dependents must be 0 or greater.");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid dependents format.");
            return;
        }

        //continue if all valid
        try {
            EmployeeDAO employeeDAO = new EmployeeDAO();
            String employeeId = employeeDAO.getNextEmployeeId();

            Employee emp = new Employee(employeeId, firstName, lastName, middleName, dob, phone, email,
                    status, gender, payType, addressLine1, addressLine2, city, state, zip);

            boolean added = employeeDAO.addEmployee(emp);

            if (!added) {
                statusLabel.setText("Failed to save employee.");
                return;
            }
            //create default user login (email prefix + dob digits)
            String defaultPassword = email.split("@")[0] + dob.replaceAll("-", "");
            UserDAO userDAO = new UserDAO();
            boolean userCreated = userDAO.addUser(employeeId, defaultPassword, role);
            
            //create salary info record
            SalaryInfoDAO salaryDAO = new SalaryInfoDAO();
            SalaryInfo salary = new SalaryInfo(employeeId, department, jobTitle, hireDate, payType, parsedWage, medicalCoverage, numDependents);
            boolean salarySaved = salaryDAO.addSalaryInfo(salary);
            
            //show status depending on what saved
            if (!userCreated && !salarySaved) {
                statusLabel.setText("Employee saved, but user and salary info failed.");
            } else if (!userCreated) {
                statusLabel.setText("Employee and salary info saved, but user creation failed.");
            } else if (!salarySaved) {
                statusLabel.setText("Employee and user saved, but salary info failed.");
            } else {
                statusLabel.setText("Employee, user, and salary info saved.");
            }

    } catch (Exception e) {
        statusLabel.setText("Error: " + e.getMessage());
        e.printStackTrace();
    }
}
    /**
     * handles back button click
     * loads manage employees screen
     * @throws IOException if fxml fails to load
     */

    @FXML
    private void onBackClick() throws IOException {
        //load manage employees screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/manage_employees.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) backButton.getScene().getWindow();
        
        //set new scene
        Scene scene = new Scene(root, 800, 700);
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        stage.setScene(scene);
    }
}
