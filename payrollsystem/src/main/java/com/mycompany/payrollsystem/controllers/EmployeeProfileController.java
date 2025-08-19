package com.mycompany.payrollsystem.controllers;

import com.mycompany.payrollsystem.models.Employee;
import com.mycompany.payrollsystem.models.SalaryInfo;
import com.mycompany.payrollsystem.dao.SalaryInfoDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import java.io.IOException;

/**
 * controller for the employee profile screen.
 * displays basic employee details and salary information
 * for the currently logged-in employee.
 */
public class EmployeeProfileController {

    //profile fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField middleNameField;
    @FXML private TextField dobField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField jobTitleField;
    @FXML private TextField wageField;
    @FXML private Button backButton;
    
    //logged-in employee object
    private Employee loggedInEmployee;
    
     /**
     * sets the logged-in employee and fills in profile details.
     * also fetches salary info from the database if available.
     *
     * @param employee the employee who is logged in
     */
    public void setLoggedInEmployee(Employee employee) {
        this.loggedInEmployee = employee;
        //populate fields from employee object
        firstNameField.setText(employee.getFirstName());
        lastNameField.setText(employee.getLastName());
        middleNameField.setText(employee.getMiddleName());
        dobField.setText(employee.getDob());
        phoneField.setText(employee.getPhone());
        emailField.setText(employee.getEmail());
        //fetch salary info from dao
         SalaryInfoDAO salaryDao = new SalaryInfoDAO();
        SalaryInfo salary = salaryDao.fetchSalaryInfoByEmployeeId(employee.getEmployeeId());

        if (salary != null) {
            jobTitleField.setText(salary.getJobTitle());
            wageField.setText(String.format("%.2f", salary.getWage()));
        } else {
            //fallback if no salary info exists
            jobTitleField.setText("N/A");
            wageField.setText("N/A");
        }
    }
   /**
     * handles back button click, navigates to the employee dashboard.
     *
     * @throws IOException if the fxml cannot be loaded
     */
    @FXML
    private void onBackClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/employee_dashboard.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = new Scene(root, 800, 700);
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        stage.setScene(scene);
    }
}
