package com.mycompany.payrollsystem.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import java.io.IOException;
import com.mycompany.payrollsystem.utils.Session;
import com.mycompany.payrollsystem.dao.EmployeeDAO;
import com.mycompany.payrollsystem.models.Employee;

public class EmployeeDashboardController {

    @FXML private Button viewProfileButton;
    @FXML private Button viewTimesheetButton;
    @FXML private Button viewPayrollButton;
    @FXML private Button logoutButton;

    
@FXML
private void onViewProfileClick() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/employee_profile.fxml"));
    Parent root = loader.load();

    EmployeeProfileController controller = loader.getController();
    controller.setLoggedInEmployee(Session.loggedInEmployee);

    Stage stage = (Stage) viewProfileButton.getScene().getWindow();
    stage.setScene(new Scene(root));
}


 @FXML
private void onTimesheetClick() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/employee_timesheet.fxml"));
    Parent root = loader.load();

    TimeEntryController controller = loader.getController();
    controller.setEmployeeId(Session.loggedInEmployee.getEmployeeId());

    Stage stage = (Stage) viewTimesheetButton.getScene().getWindow();
    stage.setScene(new Scene(root));
}


    @FXML
    private void onPayrollClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/employee_payroll_history.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) viewPayrollButton.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void onLogoutClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.setScene(new Scene(root));
    }
    
    
}
