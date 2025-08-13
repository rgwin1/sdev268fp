package com.mycompany.payrollsystem.controllers;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import java.io.IOException;

public class AdminDashboardController {
    // dashboard buttons

    @FXML private Button buttonLogout;
    @FXML private Button payroll;
    @FXML private Button manageEmployees;
    @FXML private Button reports;


    
    @FXML
    private void onLogoutClick() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
    Parent root = loader.load();
    Stage stage = (Stage) buttonLogout.getScene().getWindow();
    stage.setScene(new Scene(root));
}
    @FXML
    private void onManageEmployeesClick() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/manage_employees.fxml"));
    Parent root = loader.load();
    Stage stage = (Stage) buttonLogout.getScene().getWindow();
    stage.setScene(new Scene(root));
}
    @FXML
    private void onPayrollClick() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/payroll_page.fxml"));
    Parent root = loader.load();
    Stage stage = (Stage) buttonLogout.getScene().getWindow();
    stage.setScene(new Scene(root));
}
    @FXML
    private void onReportsClick() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_reports.fxml"));
    Parent root = loader.load();
    Stage stage = (Stage) buttonLogout.getScene().getWindow();
    stage.setScene(new Scene(root));
}
    

}
    

