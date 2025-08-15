package com.mycompany.payrollsystem.controllers;

import com.mycompany.payrollsystem.dao.UserDAO;
import com.mycompany.payrollsystem.dao.EmployeeDAO;
import com.mycompany.payrollsystem.models.Employee;
import com.mycompany.payrollsystem.utils.Session;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;

public class LoginController {

    @FXML private TextField inputUsername;
    @FXML private PasswordField inputPassword;
    @FXML private RadioButton selectEmployee;
    @FXML private RadioButton selectAdmin;
    @FXML private ToggleGroup radioUserType;
    @FXML private Label textError;

    @FXML
    private void initialize() {
        radioUserType = new ToggleGroup();
        selectEmployee.setToggleGroup(radioUserType);
        selectAdmin.setToggleGroup(radioUserType);
    }

    @FXML
    private void submitLogin() throws Exception {
        String user = inputUsername.getText().trim();
        String pass = inputPassword.getText().trim();
        String type = null;

        if (selectEmployee.isSelected()) {
            type = "Employee";
        } else if (selectAdmin.isSelected()) {
            type = "Admin";
        }

        if (user.isEmpty() || pass.isEmpty() || type == null) {
            textError.setText("Please fill in all the fields.");
            return;
        }

        if (type.equals("Admin")) {
    UserDAO dao = new UserDAO();
    boolean valid = dao.validateUser(user, pass, "admin");

    if (valid) {
        FXMLLoader fx = new FXMLLoader(getClass().getResource("/admin_dashboard.fxml"));
        Parent page = fx.load();
        Stage stage = (Stage) inputUsername.getScene().getWindow();
        stage.setScene(new Scene(page));
    } else {
        textError.setText("Invalid admin credentials.");
    }
    return;
}


        if (type.equals("Employee")) {
            UserDAO dao = new UserDAO();
            boolean valid = dao.validateUser(user, pass, "employee");

            if (valid) {
                // fetch employee info
                EmployeeDAO empDAO = new EmployeeDAO();
                Employee emp = empDAO.getEmployeeById(user);
                Session.loggedInEmployee = emp;

                FXMLLoader fx = new FXMLLoader(getClass().getResource("/employee_dashboard.fxml"));
                Parent page = fx.load();
                Stage stage = (Stage) inputUsername.getScene().getWindow();
                stage.setScene(new Scene(page));
            } else {
                textError.setText("Invalid credentials.");
            }
        }
    }

    @FXML
    private void onExitClick() {
        System.exit(0);
    }
}
