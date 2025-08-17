package com.mycompany.payrollsystem.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import java.io.IOException;
import com.mycompany.payrollsystem.utils.Session;

/**
 * controller for the employee dashboard screen
 * handles navigation for logged-in employees to their profile
 * timesheet, payroll history, and logout
 */
public class EmployeeDashboardController {
    //buttons defined in the fxml
    @FXML private Button viewProfileButton;
    @FXML private Button viewTimesheetButton;
    @FXML private Button viewPayrollButton;
    @FXML private Button logoutButton;

  /**
 * opens the employee profile screen and passes the logged-in employee.
 * @throws IOException if fxml can't be loaded
 */  
@FXML
private void onViewProfileClick() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/employee_profile.fxml"));
    Parent root = loader.load();
    
    //pass logged-in employee to the profile controller
    EmployeeProfileController controller = loader.getController();
    controller.setLoggedInEmployee(Session.loggedInEmployee);
    
    //switch scene to profile screen
    Stage stage = (Stage) viewProfileButton.getScene().getWindow();
    stage.setScene(new Scene(root));
}

/**
 * opens the employee timesheet screen and passes employee id.
 * @throws IOException if fxml cant be loaded
 */

 @FXML
private void onTimesheetClick() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/employee_timesheet.fxml"));
    Parent root = loader.load();
    
    //pass employeeid to timesheet controller
    TimeEntryController controller = loader.getController();
    controller.setEmployeeId(Session.loggedInEmployee.getEmployeeId());
   
    //switch scene to timesheen screen
    Stage stage = (Stage) viewTimesheetButton.getScene().getWindow();
    stage.setScene(new Scene(root));
}

    /**
     * opens the payroll history screen for logged-in employee
     * @throws IOException if fxml cant be loaded
     */
    @FXML
    private void onPayrollClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/employee_payroll_history.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) viewPayrollButton.getScene().getWindow();
        Scene scene = new Scene(root, 800, 700);
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        stage.setScene(scene);
    }
    /**
     * logs employee out and returns to login screen
     * @throws IOException if fxml cant be loaded
     */
    @FXML
    private void onLogoutClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        Scene scene = new Scene(root, 800, 700);
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        stage.setScene(scene);
    }
    
    
}
