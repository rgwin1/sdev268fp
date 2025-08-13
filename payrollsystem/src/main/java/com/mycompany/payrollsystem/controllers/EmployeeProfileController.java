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

public class EmployeeProfileController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField dobField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField jobTitleField;
    @FXML private TextField wageField;
    @FXML private Button backButton;

    private Employee loggedInEmployee;

    public void setLoggedInEmployee(Employee employee) {
        this.loggedInEmployee = employee;

        firstNameField.setText(employee.getFirstName());
        lastNameField.setText(employee.getLastName());
        dobField.setText(employee.getDob());
        phoneField.setText(employee.getPhone());
        emailField.setText(employee.getEmail());
        
         SalaryInfoDAO salaryDao = new SalaryInfoDAO();
        SalaryInfo salary = salaryDao.fetchSalaryInfoByEmployeeId(employee.getEmployeeId());

        if (salary != null) {
            jobTitleField.setText(salary.getJobTitle());
            wageField.setText(String.format("%.2f", salary.getWage()));
        } else {
            jobTitleField.setText("N/A");
            wageField.setText("N/A");
        }
    }

    @FXML
    private void onBackClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/employee_dashboard.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
