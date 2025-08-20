package com.mycompany.payrollsystem.controllers;

import com.mycompany.payrollsystem.dao.TimeEntryDAO;
import com.mycompany.payrollsystem.dao.SalaryInfoDAO;
import com.mycompany.payrollsystem.models.TimeEntry;
import com.mycompany.payrollsystem.models.SalaryInfo;
import com.mycompany.payrollsystem.utils.InputValidator;
import com.mycompany.payrollsystem.utils.RecursiveUtils;
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
import java.sql.SQLException;

/**
 * controller for handling employee time entry input and display
 * manages PTO vs hourly logic, saves entries to db, and refreshes table
 */
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

    /**
     * sets employee id context and adjusts inputs based on pay type
     */
    public void setEmployeeId(String employeeid) {
        this.currentEmployeeId = employeeid;

        //detect pay type
        SalaryInfoDAO sdao = new SalaryInfoDAO();
        SalaryInfo si = sdao.fetchSalaryInfoByEmployeeId(employeeid);

        //safe check with defaults
        isSalary = false;
        if (si != null) {
            String payType = si.getPayType();
            if (payType != null && payType.equalsIgnoreCase("Salary")) {
                isSalary = true;
            }
        }

        //enable inputs now that we have context
        inputDate.setDisable(false);   //always allow date
        checkboxPTO.setDisable(false);

        if (isSalary) {
            checkboxPTO.setSelected(true);
            //for salaried: hours only when PTO is checked
            inputHours.setDisable(!checkboxPTO.isSelected());
            if (statusLabel != null) {
                statusLabel.setText("Salaried employee. Only PTO entries allowed.");
            }
        } else {
            //hourly: hours always enabled
            inputHours.setDisable(false);
            checkboxPTO.setSelected(false);
            if (statusLabel != null) statusLabel.setText("");
        }

        refreshEntryTable();
    }
    
    @FXML
    public void initialize() {
        //bind table columns to model
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colHours.setCellValueFactory(new PropertyValueFactory<>("hoursWorked"));
        colPTO.setCellValueFactory(new PropertyValueFactory<>("pto"));
        colLocked.setCellValueFactory(new PropertyValueFactory<>("locked"));

        //defaults for new form
        inputDate.setText("");
        inputHours.setText("");
        checkboxPTO.setSelected(false);
        if (statusLabel != null) statusLabel.setText("");

        //lock inputs until employee id is set
        inputDate.setDisable(true);
        inputHours.setDisable(true);
        checkboxPTO.setDisable(true);

        //dynamic toggle: salaried employees can only log hours if PTO box checked
        checkboxPTO.selectedProperty().addListener((obs, was, isNow) -> {
            if (isSalary) {
                inputHours.setDisable(!isNow);
            }
        });
        entryTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
    if (sel == null) {
        inputDate.clear();
        inputHours.clear();
        checkboxPTO.setSelected(false);
        statusLabel.setText("");
        return;
    }
    // mirror selected row into fields for edit/delete
    inputDate.setText(sel.getDate());
    inputHours.setText(Double.toString(sel.getHoursWorked()));
    checkboxPTO.setSelected(sel.isPto());

    // for salaried employees, keep PTO only
    if (isSalary) {
        checkboxPTO.setDisable(false);
        inputHours.setDisable(!checkboxPTO.isSelected());
        statusLabel.setText("Salaried employee. Only PTO entries allowed.");
    } else {
        inputHours.setDisable(false);
        checkboxPTO.setDisable(false);
        statusLabel.setText("");
    }
});

    }

    //reloads table data from dao
    private void refreshEntryTable() {
        if (currentEmployeeId == null || currentEmployeeId.isEmpty()) {
            return;
        }
        TimeEntryDAO dao = new TimeEntryDAO();
        List<TimeEntry> entries = dao.fetchTimeEntriesByEmployeeId(currentEmployeeId);
        ObservableList<TimeEntry> data = FXCollections.observableArrayList(entries);
        entryTable.setItems(data);
        

    }

    /**
     * validates input and saves time entry to db
     * applies business rules for salary vs hourly
     */
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

        //salaried employees may only log PTO
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
public void onEditEntryClick() throws SQLException {
    TimeEntry selected = entryTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
        statusLabel.setText("Select a row to edit.");
        return;
    }
    //prevent editing locked rows
    if (selected.isLocked()) {
        statusLabel.setText("Locked entries cannot be edited.");
        return;
    }

    String originalDate = selected.getDate();   
    String newDate = inputDate.getText().trim();   
    String hourText = inputHours.getText().trim();
    boolean newIsPto = checkboxPTO.isSelected();

    //validate inputs
    if (!InputValidator.isValidDate(newDate)) {
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
    if (isSalary && !newIsPto) {
        statusLabel.setText("Salaried employees can only log PTO hours.");
        return;
    }

    TimeEntryDAO dao = new TimeEntryDAO();
    if (dao.isEntryLocked(currentEmployeeId, originalDate)) {
        statusLabel.setText("Cannot edit. This payroll period is locked.");
        return;
    }
    
    TimeEntry editedEntry = new TimeEntry(currentEmployeeId, newDate, hours, newIsPto, false);

    boolean entryEdit = dao.updateTimeEntry(editedEntry, originalDate);
    if (entryEdit) {
        statusLabel.setText("Entry updated.");
        refreshEntryTable();
        // keep selection in sync if date changed
        entryTable.getItems().stream()
                .filter(entry -> entry.getDate().equals(newDate))
                .findFirst()
                .ifPresent(entry -> entryTable.getSelectionModel().select(entry));
    } else {
        statusLabel.setText("Update failed (row may be locked or not found).");
    }
}
/**
 * allows employee to delete a time entry
 * @throws SQLException 
 */
@FXML
private void onDeleteEntryClick() throws SQLException {
    TimeEntry selected = entryTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
        statusLabel.setText("Select a row to delete.");
        return;
    }
    if (selected.isLocked()) {
        statusLabel.setText("Locked entries cannot be deleted.");
        return;
    }

    // confirm with the user
    Alert confirm = new Alert(
            Alert.AlertType.CONFIRMATION,
            "Delete entry on " + selected.getDate() + "?",
            ButtonType.YES, ButtonType.NO
    );
    confirm.setHeaderText("Confirm Deletion");
    var result = confirm.showAndWait();
    if (result.isEmpty() || result.get() != ButtonType.YES) return;

    TimeEntryDAO dao = new TimeEntryDAO();

    // optional extra guard: check if that date/period got locked meanwhile
    if (dao.isEntryLocked(currentEmployeeId, selected.getDate())) {
        statusLabel.setText("Cannot delete. This payroll period is locked.");
        return;
    }

    boolean deleted = dao.deleteTimeEntry(currentEmployeeId, selected.getDate());
    if (deleted) {
        statusLabel.setText("Entry deleted.");
        refreshEntryTable();
        // clear input fields
        inputDate.clear();
        inputHours.clear();
        checkboxPTO.setSelected(false);
    } else {
        statusLabel.setText("Delete failed (row may be locked or not found).");
    }
}


    /**
     * navigates back to employee dashboard
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
