package com.mycompany.payrollsystem.controllers;

import com.mycompany.payrollsystem.Config;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import java.io.IOException;

/**
 * controller for the admin dashboard screen.
 * handles navigation to login, manage employees, payroll, and reports.
 * 
 */
public class AdminDashboardController {

    //dashboard buttons
    @FXML private Button buttonLogout;
    @FXML private Button payroll;
    @FXML private Button manageEmployees;
    @FXML private Button reports; 
    @FXML private Label labelStatusVersion; 

    @FXML
    public void initialize() {
        if (labelStatusVersion != null) {
            labelStatusVersion.setText(Config.APP_NAME + " v" + Config.APP_VERSION);
        }
    }

    @FXML
    private void onAbout() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("About");
        a.setHeaderText(Config.APP_NAME + " v" + Config.APP_VERSION);
        a.setContentText(
            "Developer: Your Name\n" +
            "JavaFX Payroll System for SDEV268\n" +
            "All deductions are pretax per requirements."
        );
        a.showAndWait();
    }
    
    /**
     * logs out and returns to the login screen
     * @throws IOException if the login fxml fails to load
     */
    @FXML
    private void onLogoutClick() throws IOException {
        //load login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) buttonLogout.getScene().getWindow();
        
        //apply scene and css
        Scene scene = new Scene(root, 800, 700);
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        stage.setScene(scene);
}
        /**
         * opens the manage employees screen
         * @throws IOException if the fxml fails to load
         */
        @FXML
        private void onManageEmployeesClick() throws IOException {
            //load manage employees screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manage_employees.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) buttonLogout.getScene().getWindow();
            //apply scene and css
            Scene scene = new Scene(root, 800, 700);
            scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
            stage.setScene(scene);
    }
        
        /**
         * opens the payroll page screen
         * @throws IOException if the fxml fails to load
         */
        @FXML
        private void onPayrollClick() throws IOException {
            //load payroll screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/payroll_page.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) buttonLogout.getScene().getWindow();
            
            //apply scene and css
            Scene scene = new Scene(root, 800, 700);
            scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
            stage.setScene(scene);
    }
        /**
         * opens the admin reports screen
         * 
         * @throws IOException if fxml fails to load
         */
        @FXML
        private void onReportsClick() throws IOException {
            //load reports screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_reports.fxml"));
            Parent root = loader.load();
            
            //apply scene and css
            Stage stage = (Stage) buttonLogout.getScene().getWindow();
            Scene scene = new Scene(root, 800, 700);
            scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
            stage.setScene(scene);
    }
    

}
    

