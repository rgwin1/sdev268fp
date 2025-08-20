package com.mycompany.payrollsystem.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.mycompany.payrollsystem.models.PayrollRow;
import com.mycompany.payrollsystem.models.TimeEntry;
import com.mycompany.payrollsystem.models.SalaryInfo;
import com.mycompany.payrollsystem.models.PayrollRecord;
import com.mycompany.payrollsystem.models.Employee;
import com.mycompany.payrollsystem.dao.EmployeeDAO;
import com.mycompany.payrollsystem.dao.TimeEntryDAO;
import com.mycompany.payrollsystem.dao.SalaryInfoDAO;
import com.mycompany.payrollsystem.dao.PayrollDAO;
import com.mycompany.payrollsystem.reports.HRReportGenerator;
import com.mycompany.payrollsystem.utils.Session;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.application.Platform;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;

/**
 * Controller for the Admin Payroll page.
 * Responsibilities:
 *  - Calculate payroll for the current pay period and persist results
 *  - Lock time entries for the period after calculation
 *  - Display previously calculated payroll rows
 *  - Export CSV and generate the HR sign-off report
 *
 * Notes on business rules used here:
 *  - Pay period is Monday through Sunday (7 days ending on the most recent Sunday)
 *  - Salaried employees are paid 8 hours per weekday
 *  - Hourly overtime: time-and-a-half for hours above 40 in the week and for Saturday work
 *  - Deductions are pretax: medical and dependent stipend reduce taxable income
 *  - Taxes: state 3.15%, federal 7.65%, Social Security 6.2%, Medicare 1.45%
 */
public class PayrollPageController {

    @FXML private TableView<PayrollRow> payrollTable;
    @FXML private TableColumn<PayrollRow, String> colEmployeeId;
    @FXML private TableColumn<PayrollRow, String> colName;
    @FXML private TableColumn<PayrollRow, String> colPayType;
    @FXML private TableColumn<PayrollRow, Number> colGross;
    @FXML private TableColumn<PayrollRow, Number> colDeductions;
    @FXML private TableColumn<PayrollRow, Number> colNet;
    @FXML private Button calculatePayrollButton;
    @FXML private Button generateHRReportButton;
    @FXML private Button exportReportButton;
    @FXML private Button backButton;

    private final ObservableList<PayrollRow> payrollData = FXCollections.observableArrayList();

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final TimeEntryDAO timeEntryDAO = new TimeEntryDAO();
    private final SalaryInfoDAO salaryDAO = new SalaryInfoDAO();
    private final PayrollDAO payrollDAO = new PayrollDAO();

    //simple per-load caches to avoid DB calls on every cell repaint
    private final Map<String, String> nameCache   = new HashMap<>();
    private final Map<String, String> payTypeCache = new HashMap<>();
    
        /**
     * Wire up table columns, attach caches, and perform an initial load
     * after the scene is ready. listens for window focus to refresh
     * from the database when returning to this screen.
     */
    @FXML
    public void initialize() {
        //direct fields from model
        colEmployeeId.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getEmployeeId()));
        colGross.setCellValueFactory(cd -> cd.getValue().grossPayProperty());
        colNet.setCellValueFactory(cd -> cd.getValue().netPayProperty());

        //name/pay type via small caches
        colName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
                nameCache.computeIfAbsent(cd.getValue().getEmployeeId(), this::lookupName)
        ));
        colPayType.setCellValueFactory(cd -> new ReadOnlyStringWrapper(
                payTypeCache.computeIfAbsent(cd.getValue().getEmployeeId(), this::lookupPayType)
        ));

        //deductions = medical + state + federal + ss + medicare - dependent stipend
        colDeductions.setCellValueFactory(cd ->
            cd.getValue().medicalDeductionProperty()
              .add(cd.getValue().stateTaxProperty())
              .add(cd.getValue().federalTaxProperty())
              .add(cd.getValue().socialSecurityProperty())
              .add(cd.getValue().medicareProperty())
              .subtract(cd.getValue().dependentStipendProperty())
        );

        payrollTable.setItems(payrollData);

        //initial load after scene is ready (pull from persisted payroll for last period)
        Platform.runLater(this::refreshFromSession);

        //when returning focus to this window, reload from database (not just refresh UI)
        payrollTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((o, oldWin, newWin) -> {
                    if (newWin != null) {
                        newWin.focusedProperty().addListener((oo, was, isNow) -> {
                            if (isNow) refreshFromSession();
                        });
                    }
                });
            }
        });
    }
  /**
     * Resolve employee full name for table display.
     * @param empId employee id
     * @return "First Last" or empty string if not found
     */
    private String lookupName(String empId) {
        Employee employee = employeeDAO.getEmployeeById(empId);
        return (employee == null) ? "" : (employee.getFirstName() + " " + employee.getLastName());
    }
        /**
     * Resolve pay type for table display.
     * @param empId employee id
     * @return pay type string or empty string if not found
     */
    private String lookupPayType(String empId) {
        SalaryInfo salaryInfo = salaryDAO.fetchSalaryInfoByEmployeeId(empId);
        return (salaryInfo == null) ? "" : salaryInfo.getPayType();
    }
     /**
     * Count weekdays between two dates inclusive.
     * Used to compute salaried hours in the period.
     */
    private int countWeekdays(LocalDate start, LocalDate end) {
        int days = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            switch (d.getDayOfWeek()) {
                case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> days++;
                default -> {}
            }
        }
        return days;
    }
 /**
     * Compute the current pay period for a reference date.
     * The period is Monday through Sunday, ending on the most recent Sunday.
     * @param ref reference date
     * @return array with [start, end] dates
     */
    private LocalDate[] currentPayPeriod(LocalDate ref) {
        LocalDate end = ref.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate start = end.minusDays(6);
        return new LocalDate[] { start, end };
    }
    /**
     * Calculate payroll for the current pay period and persist results.
     * Business rules:
     *  - Skip terminated employees
     *  - Lock all time entries for the period after insert
     *  - Salaried: 8 hours per weekday at wage
     *  - Hourly: overtime includes Saturday hours and hours above 40 at 1.5x
     *
     * @throws SQLException if DAO operations fail
     */
    @FXML
    private void onCalculatePayrollClick() throws SQLException {
        payrollData.clear();

        LocalDate[] period = currentPayPeriod(LocalDate.now());
        LocalDate startDate = period[0];
        LocalDate endDate   = period[1];
        String payDate = endDate.plusDays(7).toString();
        String payPeriod = startDate + " to " + endDate;

        //remember this period so we can auto-reload on return
        Session.lastPeriodStart = startDate;
        Session.lastPeriodEnd   = endDate;

        List<Employee> allEmployees = employeeDAO.getAllEmployees();
        for (Employee employee : allEmployees) {
            if (employee.getStatus() != null && employee.getStatus().equalsIgnoreCase("Terminated")) continue;

            String id = employee.getEmployeeId();
            String payType = employee.getPayType();

            //get entries in period (skip locked)
            List<TimeEntry> entries = timeEntryDAO.fetchTimeEntriesByEmployeeId(id);

            double totalHours = 0, ptoHours = 0, saturdayHours = 0;
            for (TimeEntry entry : entries) {
                if (entry.isLocked()) continue;
                LocalDate entryDate = LocalDate.parse(entry.getDate());
                if (entryDate.isBefore(startDate) || entryDate.isAfter(endDate)) continue;

                if (entry.isPto()) {
                    ptoHours += entry.getHoursWorked();
                } else {
                    totalHours += entry.getHoursWorked();
                    if (entryDate.getDayOfWeek().getValue() == 6) {
                        saturdayHours += entry.getHoursWorked();
                    }
                }
            }

            SalaryInfo salary = salaryDAO.fetchSalaryInfoByEmployeeId(id);
            if (salary == null) continue;

            double wage = salary.getWage();
            double grossPay, overtimeHours, hoursWorkedForRow;

            if ("Salary".equalsIgnoreCase(payType)) {
                int weekdays = countWeekdays(startDate, endDate);
                hoursWorkedForRow = weekdays * 8.0;
                overtimeHours = 0.0;
                grossPay = hoursWorkedForRow * wage;

                double medicalDeduction = salary.getMedicalCoverage().equalsIgnoreCase("Family") ? 100 : 50;
                double dependentStipend = 45 * salary.getNumDependents();
                double taxableIncome = grossPay - medicalDeduction - dependentStipend;

                double stateTax = taxableIncome * 0.0315;
                double federalTax = taxableIncome * 0.0765;
                double socialSecurity = taxableIncome * 0.062;
                double medicare = taxableIncome * 0.0145;

                double totalDeductions = medicalDeduction + stateTax + federalTax + socialSecurity + medicare;
                double netPay = grossPay - totalDeductions;

                payrollData.add(new PayrollRow(
                        id, payPeriod, hoursWorkedForRow, wage,
                        grossPay, medicalDeduction, dependentStipend,
                        stateTax, federalTax, socialSecurity, medicare, netPay
                ));

                //prevent duplicates if rerunning same period
                payrollDAO.deletePayrollForPeriod(id, startDate.toString(), endDate.toString());

                payrollDAO.insertPayrollRecord(new PayrollRecord(
                        id, startDate.toString(), endDate.toString(), payDate,
                        hoursWorkedForRow, 0.0, wage,
                        grossPay, totalDeductions, netPay, false
                ));

            } else {
                double weekdayHours = totalHours - saturdayHours;
                double standardOvertime = Math.max(0, weekdayHours + saturdayHours - 40);
                double regularHours = Math.max(0, weekdayHours - standardOvertime);

                overtimeHours = standardOvertime + saturdayHours;
                grossPay = (regularHours * wage) + (overtimeHours * wage * 1.5) + (ptoHours * wage);

                double medicalDeduction = salary.getMedicalCoverage().equalsIgnoreCase("Family") ? 100 : 50;
                double dependentStipend = 45 * salary.getNumDependents();
                double taxableIncome = grossPay - medicalDeduction - dependentStipend;

                double stateTax = taxableIncome * 0.0315;
                double federalTax = taxableIncome * 0.0765;
                double socialSecurity = taxableIncome * 0.062;
                double medicare = taxableIncome * 0.0145;

                double totalDeductions = medicalDeduction + stateTax + federalTax + socialSecurity + medicare;
                double netPay = grossPay - totalDeductions;

                hoursWorkedForRow = totalHours + ptoHours;

                payrollData.add(new PayrollRow(
                        id, payPeriod, hoursWorkedForRow, wage,
                        grossPay, medicalDeduction, dependentStipend,
                        stateTax, federalTax, socialSecurity, medicare, netPay
                ));

                //prevent duplicates if rerunning same period
                payrollDAO.deletePayrollForPeriod(id, startDate.toString(), endDate.toString());

                payrollDAO.insertPayrollRecord(new PayrollRecord(
                        id, startDate.toString(), endDate.toString(), payDate,
                        hoursWorkedForRow, overtimeHours, wage,
                        grossPay, totalDeductions, netPay, false
                ));
            }
        }

        //lock entries for the period
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String start = startDate.format(formatter);
        String end   = endDate.format(formatter);
        for (PayrollRow row : payrollData) {
            timeEntryDAO.lockEntries(row.getEmployeeId(), start, end);
        }

        new Alert(Alert.AlertType.INFORMATION) {{
            setTitle("Success");
            setHeaderText("Entries Locked");
            setContentText("All time entries for this payroll period are now locked.");
        }}.showAndWait();

        payrollTable.refresh();
    }


  /**
     * Load the previously used period if available, otherwise
     * compute the current period, then load rows from the DB.
     */
    private void refreshFromSession() {
        LocalDate start, end;
        if (Session.lastPeriodStart != null &&
            Session.lastPeriodEnd   != null) {
            start = Session.lastPeriodStart;
            end   = Session.lastPeriodEnd;
        } else {
            var p = currentPayPeriod(LocalDate.now());
            start = p[0]; end = p[1];
        }
        loadFromDb(start, end);
    }
    /**
     * Load persisted payroll rows for a given period, recompute display-only
     * deduction columns from current salary info, and refresh the table.
     * @param start period start date
     * @param end period end date
     */
    private void loadFromDb(LocalDate start, LocalDate end) {
        //clear caches for a clean rebuild
        nameCache.clear();
        payTypeCache.clear();

        List<PayrollRecord> records = payrollDAO.fetchPayrollByPeriod(start.toString(), end.toString());

        payrollData.clear();
        for (PayrollRecord record : records) {
            //recompute medical/dependents/taxes from salary_info for display (matches your table columns)
            SalaryInfo salaryInfo = salaryDAO.fetchSalaryInfoByEmployeeId(record.getEmployeeId());
            double medical = (salaryInfo != null && "Family".equalsIgnoreCase(salaryInfo.getMedicalCoverage())) ? 100 : 50;
            double depend  = (salaryInfo != null ? 45 * salaryInfo.getNumDependents() : 0);

            double taxable = record.getGrossPay() - medical - depend;
            double state   = taxable * 0.0315;
            double socialsecurity      = taxable * 0.062;
            double med     = taxable * 0.0145;
            double federal = taxable * 0.0765;

            payrollData.add(new PayrollRow(
                record.getEmployeeId(),
                record.getPayPeriodStart() + " to " + record.getPayPeriodEnd(),
                record.getHoursWorked(),
                record.getWageAtTime(),
                record.getGrossPay(),
                medical,
                depend,
                state,
                federal,
                socialsecurity,
                med,
                record.getNetPay()
            ));
        }
        payrollTable.refresh();
    }

        /**
     * Export the visible payroll table to CSV.
     * File name includes the pay period when available.
     */
    @FXML
    private void onExportReportClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Payroll Report");

        String payPeriodForName = payrollData.isEmpty()
                ? ""
                : payrollData.get(0).getPayPeriod()
                    .replace(" ", "_")
                    .replace("to", "-");

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("payroll_report_" + payPeriodForName + ".csv");

        File file = fileChooser.showSaveDialog(payrollTable.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                if (!payrollData.isEmpty()) {
                    writer.println("Pay Period: " + payrollData.get(0).getPayPeriod());
                }

                writer.println("Employee ID,Pay Period,Hours Worked,Wage,Gross Pay,Medical Deduction,Dependent Stipend,State Tax,Federal Tax,Social Security,Medicare,Net Pay");

                for (PayrollRow row : payrollData) {
                    writer.printf("%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                            row.getEmployeeId(),
                            row.getPayPeriod(),
                            row.getHoursWorked(),
                            row.getWageAtTime(),
                            row.getGrossPay(),
                            row.getMedicalDeduction(),
                            row.getDependentStipend(),
                            row.getStateTax(),
                            row.getFederalTax(),
                            row.getSocialSecurity(),
                            row.getMedicare(),
                            row.getNetPay()
                    );
                }

                new Alert(Alert.AlertType.INFORMATION) {{
                    setTitle("Export Successful");
                    setHeaderText("Payroll report exported successfully.");
                    setContentText("Saved to:\n" + file.getAbsolutePath());
                }}.showAndWait();

            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR) {{
                    setTitle("Export Failed");
                    setHeaderText("Failed to export report.");
                    setContentText(e.getMessage());
                }}.showAndWait();
            }
        }
    }
    /**
     * takes user back to admin_dashboard.fxml
     */
    @FXML
    private void onBackClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 800, 700);
            scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Generate the HR sign-off report for the current pay period.
     * Requires that payroll rows are present for the period.
     * Shows a save dialog and writes a plain text report.
     */
    @FXML
    private void onGenerateHRReportClick() {
        if (payrollData.isEmpty()) {
            new Alert(Alert.AlertType.WARNING) {{
                setTitle("No Data");
                setHeaderText("No payroll rows found for this period");
                setContentText("Calculate payroll first, then generate the HR report.");
            }}.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save HR Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("hr_report.txt");

        File file = fileChooser.showSaveDialog(payrollTable.getScene().getWindow());
        if (file != null) {
            try {
                LocalDate[] period = currentPayPeriod(LocalDate.now());
                LocalDate startDate = period[0];
                LocalDate endDate   = period[1];
                String payPeriod = startDate + " to " + endDate;

                HRReportGenerator generator = new HRReportGenerator();
                List<HRReportGenerator.Row> data = generator.getPayrollData(startDate, endDate);
                generator.generateReport(data, file.getAbsolutePath(), payPeriod);

                new Alert(Alert.AlertType.INFORMATION) {{
                    setTitle("Report Generated");
                    setHeaderText("HR Report Generated Successfully");
                    setContentText("Saved to: " + file.getAbsolutePath());
                }}.showAndWait();

            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR) {{
                    setTitle("Error");
                    setHeaderText("Failed to generate HR report");
                    setContentText(e.getMessage());
                }}.showAndWait();
            }
        }
    }
}
