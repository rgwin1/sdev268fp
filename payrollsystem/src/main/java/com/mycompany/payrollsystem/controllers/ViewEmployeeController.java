package com.mycompany.payrollsystem.controllers;

import com.mycompany.payrollsystem.dao.EmployeeDAO;
import com.mycompany.payrollsystem.dao.SalaryInfoDAO;
import com.mycompany.payrollsystem.dao.TimeEntryDAO;
import com.mycompany.payrollsystem.models.Employee;
import com.mycompany.payrollsystem.models.SalaryInfo;
import com.mycompany.payrollsystem.models.TimeEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class ViewEmployeeController {

    @FXML private TextField searchField;
    @FXML private Button backButton;

    @FXML private Label lblEmployeeId;
    @FXML private Label lblName;
    @FXML private Label lblStatus;
    @FXML private Label lblGender;
    @FXML private Label lblDob;
    @FXML private Label lblPhone;
    @FXML private Label lblEmail;
    @FXML private Label lblAddress;

    @FXML private Label lblPayType;
    @FXML private Label lblWage;
    @FXML private Label lblDepartment;
    @FXML private Label lblJobTitle;
    @FXML private Label lblHireDate;
    @FXML private Label lblMedical;
    @FXML private Label lblDependents;

    @FXML private TableView<TimeEntry> timeTable;
    @FXML private TableColumn<TimeEntry, String> colDate;
    @FXML private TableColumn<TimeEntry, Double> colHours;
    @FXML private TableColumn<TimeEntry, Boolean> colPTO;
    @FXML private TableColumn<TimeEntry, Boolean> colLocked;
    @FXML private Label lblPayPeriod;

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final SalaryInfoDAO salaryDAO = new SalaryInfoDAO();
    private final TimeEntryDAO timeDAO = new TimeEntryDAO();

    private String currentEmployeeId;

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colHours.setCellValueFactory(new PropertyValueFactory<>("hoursWorked"));
        colPTO.setCellValueFactory(new PropertyValueFactory<>("pto"));
        colLocked.setCellValueFactory(new PropertyValueFactory<>("locked"));
    }

    @FXML
    public void onViewEmployeeClick() {
        String q = (searchField.getText() == null) ? "" : searchField.getText().trim();
        if (q.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Enter an Employee ID (e.g., E001) or a name (First Last / Last, First).");
            return;
        }

        Employee e = null;


        String upper = q.toUpperCase();
        if (upper.matches("^E\\d{3}$")) {
            e = employeeDAO.getEmployeeById(upper);
        }


        if (e == null && q.contains(",")) {
            String[] parts = q.split(",", 2);
            String last = parts[0].trim();
            String first = parts.length > 1 ? parts[1].trim() : "";
            if (!first.isEmpty() && !last.isEmpty()) {
                e = employeeDAO.getEmployeeByName(first, last);
            }
        }

        if (e == null && q.contains(" ")) {
            String[] parts = q.trim().split("\\s+");
            if (parts.length >= 2) {
                String first = parts[0];
                String last  = parts[parts.length - 1];
                e = employeeDAO.getEmployeeByName(first, last);
            }
        }

        if (e == null) {
            showAlert(Alert.AlertType.WARNING, "Employee not found: " + q);
            return;
        }

        currentEmployeeId = e.getEmployeeId();

        lblEmployeeId.setText(e.getEmployeeId());
        String middle = e.getMiddleName() == null ? "" : (" " + e.getMiddleName());
        lblName.setText(e.getFirstName() + middle + " " + e.getLastName());
        lblStatus.setText(e.getStatus());
        lblGender.setText(e.getGender());
        lblDob.setText(e.getDob());
        lblPhone.setText(e.getPhone());
        lblEmail.setText(e.getEmail());

        String addr2 = (e.getAddressLine2() == null || e.getAddressLine2().isBlank()) ? "" : (", " + e.getAddressLine2());
        lblAddress.setText(e.getAddressLine1() + addr2 + ", " + e.getCity() + ", " + e.getState() + " " + e.getZip());

        SalaryInfo s = salaryDAO.fetchSalaryInfoByEmployeeId(currentEmployeeId);
        if (s != null) {
            lblPayType.setText(s.getPayType());
            lblWage.setText(String.format("%.2f", s.getWage()));
            lblDepartment.setText(s.getDepartment());
            lblJobTitle.setText(s.getJobTitle());
            lblHireDate.setText(s.getHireDate());
            lblMedical.setText(s.getMedicalCoverage());
            lblDependents.setText(String.valueOf(s.getNumDependents()));
        } else {
            lblPayType.setText("-");
            lblWage.setText("-");
            lblDepartment.setText("-");
            lblJobTitle.setText("-");
            lblHireDate.setText("-");
            lblMedical.setText("-");
            lblDependents.setText("-");
        }

        loadCurrentPeriodEntries();
    }


    @FXML
    private void onRefreshTimeEntries() {
        if (currentEmployeeId == null || currentEmployeeId.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Load an employee first.");
            return;
        }
        loadCurrentPeriodEntries();
    }

    private void loadCurrentPeriodEntries() {
        LocalDate[] period = currentPayPeriod(LocalDate.now());
        LocalDate start = period[0];
        LocalDate end = period[1];
        lblPayPeriod.setText(start + " to " + end);

        List<TimeEntry> all = timeDAO.fetchTimeEntriesByEmployeeId(currentEmployeeId);
        ObservableList<TimeEntry> filtered = FXCollections.observableArrayList();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (TimeEntry t : all) {
            LocalDate d = LocalDate.parse(t.getDate(), fmt);
            if ((!d.isBefore(start)) && (!d.isAfter(end))) {
                filtered.add(t);
            }
        }
        timeTable.setItems(filtered);
    }

    private LocalDate[] currentPayPeriod(LocalDate ref) {
        LocalDate end = ref.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate start = end.minusDays(6);
        return new LocalDate[]{ start, end };
    }

    @FXML
    private void onBackClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manage_employees.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Failed to go back: " + ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Info");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
    public void setSearchText(String text) {
    if (searchField != null) searchField.setText(text);
}

}
