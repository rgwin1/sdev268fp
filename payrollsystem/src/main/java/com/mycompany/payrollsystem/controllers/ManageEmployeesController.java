package com.mycompany.payrollsystem.controllers;

import com.mycompany.payrollsystem.dao.EmployeeDAO;
import com.mycompany.payrollsystem.models.Employee;
import com.mycompany.payrollsystem.controllers.ViewEmployeeController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;

public class ManageEmployeesController implements Initializable {

    @FXML private Button buttonAddEmployee;
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colEmployeeid;
    @FXML private TableColumn<Employee, String> colLastName;
    @FXML private TableColumn<Employee, String> colFirstName;
    @FXML private Label statusLabel;
    @FXML private Button backButton; 
    @FXML private Button buttonViewEmployee;

    @FXML
    private void onAddEmployeeClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_employee.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) buttonAddEmployee.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void onEditEmployeeClick() throws IOException {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            System.out.println("No employee selected.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_employee.fxml"));
        Parent root = loader.load();

        EditEmployeeController controller = loader.getController();
        controller.setEmployeeToEdit(selected);

        Stage stage = (Stage) employeeTable.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void onDeleteEmployeeClick() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            statusLabel.setText("No employee selected.");
        }
        
        EmployeeDAO dao = new EmployeeDAO();
        boolean deleted = dao.deleteEmployeeEverywhere(selected.getEmployeeId());
        
        statusLabel.setText(deleted ? "Employee deleted from all tables." : "Delete failed.");
        refreshEmployeeTable();
    }

    @FXML
    private void onBackClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_dashboard.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    public void refreshEmployeeTable() {
        try {
            employeeTable.getItems().clear();

            EmployeeDAO dao = new EmployeeDAO();
            List<Employee> employeeList = dao.getAllEmployees();

            colEmployeeid.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
            colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));

            employeeTable.getItems().addAll(employeeList);
        } catch (Exception e) {
            System.err.println("Error refreshing employee table: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshEmployeeTable();
    }
    @FXML
private void openViewEmployeePage() throws java.io.IOException {
    com.mycompany.payrollsystem.models.Employee selected =
            employeeTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
        new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING,
                "Select an employee first.").showAndWait();
        return;
    }

    javafx.fxml.FXMLLoader loader =
            new javafx.fxml.FXMLLoader(getClass().getResource("/view_employee.fxml"));
    javafx.scene.Parent root = loader.load();

    com.mycompany.payrollsystem.controllers.ViewEmployeeController controller = loader.getController();
    controller.setSearchText(selected.getEmployeeId());
    controller.onViewEmployeeClick();

    javafx.stage.Stage stage = (javafx.stage.Stage) buttonViewEmployee.getScene().getWindow();
    stage.setScene(new javafx.scene.Scene(root));
}
    
}
