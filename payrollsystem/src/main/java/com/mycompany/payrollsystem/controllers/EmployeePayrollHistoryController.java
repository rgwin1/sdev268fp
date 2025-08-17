package com.mycompany.payrollsystem.controllers;

import com.mycompany.payrollsystem.dao.PayrollDAO;
import com.mycompany.payrollsystem.models.PayrollRecord;
import com.mycompany.payrollsystem.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import java.io.IOException;
import java.util.List;

/**
 * controller for the employee payroll history screen.
 * loads all payroll records for the logged-in employee
 * and displays them in a table view.
 */
public class EmployeePayrollHistoryController {
    @FXML private TableView<PayrollRecord> payrollTable;
    @FXML private Button backButton;

     /**
     * initializes the screen by fetching payroll records
     * for the logged-in employee and populating the table.
     */
    @FXML
    private void initialize() {
        PayrollDAO dao = new PayrollDAO();
        List<PayrollRecord> records = dao.fetchPayrollByEmployeeId(Session.loggedInEmployee.getEmployeeId());
        payrollTable.getItems().setAll(records);
    }
    /**
     * handles the back button, navigates to the employee dashboard
     * @throws IOException if bad fxml load
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
