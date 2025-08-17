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

/**
 * controller for the login screen.
 * handles login validation for both admin and employee accounts,
 * then routes the user to the correct dashboard.
 */
public class LoginController {

    //login form fields
    @FXML private TextField inputUsername;
    @FXML private PasswordField inputPassword;

    //radio buttons to select user type
    @FXML private RadioButton selectEmployee;
    @FXML private RadioButton selectAdmin;
    @FXML private ToggleGroup radioUserType;

    //label to show error messages
    @FXML private Label textError;

    /**
     * sets up the toggle group so only one user type
     * (employee or admin) can be selected.
     */
    @FXML
    private void initialize() {
        radioUserType = new ToggleGroup();
        selectEmployee.setToggleGroup(radioUserType);
        selectAdmin.setToggleGroup(radioUserType);
    }

    /**
     * handles login submission, validates credentials,
     * and loads the correct dashboard.
     *
     * @throws Exception if loading fxml fails
     */
    @FXML
    private void submitLogin() throws Exception {
        String user = inputUsername.getText().trim();
        String pass = inputPassword.getText().trim();
        String type = null;

        //determine user type from radio button
        if (selectEmployee.isSelected()) {
            type = "Employee";
        } else if (selectAdmin.isSelected()) {
            type = "Admin";
        }

        //basic validation
        if (user.isEmpty() || pass.isEmpty() || type == null) {
            textError.setText("Please fill in all the fields.");
            return;
        }

        //if admin selected, validate admin credentials
        if (type.equals("Admin")) {
            UserDAO dao = new UserDAO();
            boolean valid = dao.validateUser(user, pass, "admin");

            if (valid) {
                FXMLLoader fx = new FXMLLoader(getClass().getResource("/admin_dashboard.fxml"));
                Parent root = fx.load();
                Stage stage = (Stage) inputUsername.getScene().getWindow();
                Scene scene = new Scene(root, 800, 700);
                scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
                stage.setScene(scene);
            } else {
                textError.setText("Invalid admin credentials.");
            }
            return;
        }

        //if employee selected, validate employee credentials
        if (type.equals("Employee")) {
            UserDAO dao = new UserDAO();
            boolean valid = dao.validateUser(user, pass, "employee");

            if (valid) {
                //fetch employee info for session
                EmployeeDAO empDAO = new EmployeeDAO();
                Employee emp = empDAO.getEmployeeById(user);
                Session.loggedInEmployee = emp;

                //load employee dashboard
                FXMLLoader fx = new FXMLLoader(getClass().getResource("/employee_dashboard.fxml"));
                Parent page = fx.load();
                Stage stage = (Stage) inputUsername.getScene().getWindow();
                stage.setScene(new Scene(page));
            } else {
                textError.setText("Invalid credentials.");
            }
        }
    }

    /**
     * exits the application when exit button is clicked.
     */
    @FXML
    private void onExitClick() {
        System.exit(0);
    }
}
