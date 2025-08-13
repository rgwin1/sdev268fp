package com.mycompany.payrollsystem.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;

import com.mycompany.payrollsystem.models.PayrollRow;
import com.mycompany.payrollsystem.dao.PayrollDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.List;

public class AdminReportsController {

    @FXML private TableView<PayrollRow> reportTable;

    @FXML private TableColumn<PayrollRow, String> colEmployeeId;
    @FXML private TableColumn<PayrollRow, String> colPayPeriod;
    @FXML private TableColumn<PayrollRow, Double> colHoursWorked;
    @FXML private TableColumn<PayrollRow, Double> colWageAtTime;
    @FXML private TableColumn<PayrollRow, Double> colGrossPay;
    @FXML private TableColumn<PayrollRow, Double> colMedicalDeduction;
    @FXML private TableColumn<PayrollRow, Double> colDependentStipend;
    @FXML private TableColumn<PayrollRow, Double> colStateTax;
    @FXML private TableColumn<PayrollRow, Double> colFederalTax;
    @FXML private TableColumn<PayrollRow, Double> colSocialSecurity;
    @FXML private TableColumn<PayrollRow, Double> colMedicare;
    @FXML private TableColumn<PayrollRow, Double> colNetPay;

    @FXML
    public void initialize() {
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colPayPeriod.setCellValueFactory(new PropertyValueFactory<>("payPeriod"));
        colHoursWorked.setCellValueFactory(new PropertyValueFactory<>("hoursWorked"));
        colWageAtTime.setCellValueFactory(new PropertyValueFactory<>("wageAtTime"));
        colGrossPay.setCellValueFactory(new PropertyValueFactory<>("grossPay"));
        colMedicalDeduction.setCellValueFactory(new PropertyValueFactory<>("medicalDeduction"));
        colDependentStipend.setCellValueFactory(new PropertyValueFactory<>("dependentStipend"));
        colStateTax.setCellValueFactory(new PropertyValueFactory<>("stateTax"));
        colFederalTax.setCellValueFactory(new PropertyValueFactory<>("federalTax"));
        colSocialSecurity.setCellValueFactory(new PropertyValueFactory<>("socialSecurity"));
        colMedicare.setCellValueFactory(new PropertyValueFactory<>("medicare"));
        colNetPay.setCellValueFactory(new PropertyValueFactory<>("netPay"));

        loadPayrollData();
    }

    private void loadPayrollData() {
        PayrollDAO payrollDAO = new PayrollDAO();
        List<PayrollRow> payrollList = payrollDAO.fetchAllPayrollRows();
        ObservableList<PayrollRow> observableList = FXCollections.observableArrayList(payrollList);
        reportTable.setItems(observableList);
    }

    @FXML
    private void onBackClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) reportTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
