package com.mycompany.payrollsystem.controllers;

import com.mycompany.payrollsystem.dao.TimeEntryDAO;
import com.mycompany.payrollsystem.dao.SalaryInfoDAO;
import com.mycompany.payrollsystem.models.TimeEntry;
import com.mycompany.payrollsystem.models.SalaryInfo;
import com.mycompany.payrollsystem.utils.InputValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.List;

public class TimeEntryController {

    @FXML private TextField inputDate;
    @FXML private TextField inputHours;
    @FXML private CheckBox checkboxPTO;
    @FXML private Label statusLabel;

    @FXML private TableView<TimeEntry> entryTable;
    @FXML private TableColumn<TimeEntry, String> colDate;
    @FXML private TableColumn<TimeEntry, Double> colHours;
    @FXML private TableColumn<TimeEntry, Boolean> colPTO;
    @FXML private TableColumn<TimeEntry, Boolean> colLocked;

    @FXML private Button backButton;

    private String currentEmployeeId;
    private boolean isSalary;

    public void setEmployeeId(String id) {
        this.currentEmployeeId = id;

        // detect pay type
        SalaryInfoDAO sdao = new SalaryInfoDAO();
        SalaryInfo si = sdao.fetchSalaryInfoByEmployeeId(id);

        // verbose, safe checks
        isSalary = false;
        if (si != null) {
            String payType = si.getPayType();
            if (payType != null && payType.equalsIgnoreCase("Salary")) {
                isSalary = true;
            }
        }

        // enable inputs now that we have context
        inputDate.setDisable(false);   // always allow date
        checkboxPTO.setDisable(false);

        if (isSalary) {
            checkboxPTO.setSelected(true);
            // for salaried: hours only when PTO is checked
            inputHours.setDisable(!checkboxPTO.isSelected());
            if (statusLabel != null) {
                statusLabel.setText("Salaried employee. Only PTO entries allowed.");
            }
        } else {
            // hourly: hours always enabled
            inputHours.setDisable(false);
            checkboxPTO.setSelected(false);
            if (statusLabel != null) statusLabel.setText("");
        }

        refreshEntryTable();
    }

    @FXML
    public void initialize() {
        // bind columns
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colHours.setCellValueFactory(new PropertyValueFactory<>("hoursWorked"));
        colPTO.setCellValueFactory(new PropertyValueFactory<>("pto"));
        colLocked.setCellValueFactory(new PropertyValueFactory<>("locked"));

        // defaults
        inputDate.setText("");
        inputHours.setText("");
        checkboxPTO.setSelected(false);
        if (statusLabel != null) statusLabel.setText("");

        // lock inputs until employee id is provided
        inputDate.setDisable(true);
        inputHours.setDisable(true);
        checkboxPTO.setDisable(true);

        // dynamic toggle: if salaried, hours enabled only when PTO is checked
        checkboxPTO.selectedProperty().addListener((obs, was, isNow) -> {
            if (isSalary) {
                inputHours.setDisable(!isNow);
            }
        });
    }

    private void refreshEntryTable() {
        if (currentEmployeeId == null || currentEmployeeId.isEmpty()) {
            return;
        }
        TimeEntryDAO dao = new TimeEntryDAO();
        List<TimeEntry> entries = dao.fetchTimeEntriesByEmployeeId(currentEmployeeId);
        ObservableList<TimeEntry> data = FXCollections.observableArrayList(entries);
        entryTable.setItems(data);
    }

    @FXML
    private void onSubmitTimeClick() {
        String date = inputDate.getText().trim();
        String hourText = inputHours.getText().trim();
        boolean isPto = checkboxPTO.isSelected();

        if (!InputValidator.isValidDate(date)) {
            statusLabel.setText("Invalid date format (expected yyyy-MM-dd).");
            return;
        }

        double hours;
        try {
            hours = Double.parseDouble(hourText);
            if (!InputValidator.isValidHoursWorked(hours)) {
                statusLabel.setText("Hours must be between 0 and 24.");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid number format for hours.");
            return;
        }

        // salaried employees may only log PTO hours
        if (isSalary && !isPto) {
            statusLabel.setText("Salaried employees can only log PTO hours.");
            return;
        }

        TimeEntryDAO dao = new TimeEntryDAO();
        boolean success = dao.insertTimeEntry(currentEmployeeId, date, hours, isPto);

        if (success) {
            statusLabel.setText("Time saved.");
            refreshEntryTable();
        } else {
            if (dao.isEntryLocked(currentEmployeeId, date)) {
                statusLabel.setText("Cannot modify. This payroll period is locked.");
            } else {
                statusLabel.setText("Failed to save.");
            }
        }
    }

    @FXML
    private void onBackClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/employee_dashboard.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
