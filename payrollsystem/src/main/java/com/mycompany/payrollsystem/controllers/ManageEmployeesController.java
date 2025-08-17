package com.mycompany.payrollsystem.controllers;

import com.mycompany.payrollsystem.dao.EmployeeDAO;
import com.mycompany.payrollsystem.models.Employee;
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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;

/**
 * controller for managing employees in the admin dashboard.
 * handles adding, editing, deleting, and viewing employees.
 */
public class ManageEmployeesController implements Initializable {

    @FXML private Button buttonAddEmployee;
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colEmployeeid;
    @FXML private TableColumn<Employee, String> colLastName;
    @FXML private TableColumn<Employee, String> colFirstName;
    @FXML private Label statusLabel;
    @FXML private Button backButton; 
    @FXML private Button buttonViewEmployee;

    /**
     * opens the add employee page
     */
    @FXML
    private void onAddEmployeeClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_employee.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) buttonAddEmployee.getScene().getWindow();
        Scene scene = new Scene(root, 800, 700);
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        stage.setScene(scene);
    }

    /**
     * opens the edit employee page for the selected employee
     */
    @FXML
    private void onEditEmployeeClick() throws IOException {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            System.out.println("no employee selected.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_employee.fxml"));
        Parent root = loader.load();

        //pass selected employee to edit controller
        EditEmployeeController controller = loader.getController();
        controller.setEmployeeToEdit(selected);

        Stage stage = (Stage) employeeTable.getScene().getWindow();
        Scene scene = new Scene(root, 800, 700);
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        stage.setScene(scene);
    }

    /**
     * deletes the selected employee from all tables after confirmation
     */
    @FXML
    private void onDeleteEmployeeClick() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("no employee selected.");
            return;
        }
        
        //simple confirm dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete employee " + selected.getEmployeeId() + " from all tables?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Confirm Delete");

        ButtonType result = confirm.showAndWait().orElse(ButtonType.CANCEL);
        if (result != ButtonType.OK) {
            return; //user canceled
        }
        
        EmployeeDAO dao = new EmployeeDAO();
        boolean deleted = dao.deleteEmployeeEverywhere(selected.getEmployeeId());
        
        statusLabel.setText(deleted ? "employee deleted from all tables." : "delete failed.");
        refreshEmployeeTable();
    }

    /**
     * goes back to the admin dashboard
     */
    @FXML
    private void onBackClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_dashboard.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = new Scene(root, 800, 700);
        stage.setScene(scene);
    }

    /**
     * refreshes the employee table with latest data
     */
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
            System.err.println("error refreshing employee table: " + e.getMessage());
        }
    }

    /**
     * initializes the controller by refreshing the table
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshEmployeeTable();
    }

    /**
     * opens the view employee page for the selected employee
     */
    @FXML
    private void openViewEmployeePage() throws IOException {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Select an employee first.").showAndWait();
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view_employee.fxml"));
        Parent root = loader.load();
        ViewEmployeeController controller = loader.getController();
        controller.setSearchText(selected.getEmployeeId());
        controller.onViewEmployeeClick();
        Stage stage = (Stage) buttonViewEmployee.getScene().getWindow();
        Scene scene = new Scene(root, 800, 700);
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        stage.setScene(scene);
    }
}
