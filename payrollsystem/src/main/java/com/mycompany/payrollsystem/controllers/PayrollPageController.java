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
import javafx.beans.property.ReadOnlyStringWrapper;
import java.util.List;
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
 * controller for the payroll summary screen.
 * builds summary rows for the current pay period and supports export + hr report.
 */
public class PayrollPageController {

    @FXML private TableView<PayrollRow> payrollTable;

    @FXML private TableColumn<PayrollRow, String> colEmployeeId;
    @FXML private TableColumn<PayrollRow, String> colName;
    @FXML private TableColumn<PayrollRow, String> colPayType;

    //number columns 
    @FXML private TableColumn<PayrollRow, Number> colGross;
    @FXML private TableColumn<PayrollRow, Number> colDeductions;
    @FXML private TableColumn<PayrollRow, Number> colNet;

    @FXML private Button calculatePayrollButton;
    @FXML private Button generateHRReportButton;
    @FXML private Button exportReportButton;
    @FXML private Button backButton;

    //table data
    private final ObservableList<PayrollRow> payrollData = FXCollections.observableArrayList();

    //daos
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final TimeEntryDAO timeEntryDAO = new TimeEntryDAO();
    private final SalaryInfoDAO salaryDAO = new SalaryInfoDAO();
    private final PayrollDAO payrollDAO = new PayrollDAO();

    /**
     * binds columns to model and computed values, wires table items
     */
    @FXML
    public void initialize() {
        //bind direct fields from model
        colEmployeeId.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getEmployeeId()));
        colGross.setCellValueFactory(cd -> cd.getValue().grossPayProperty());
        colNet.setCellValueFactory(cd -> cd.getValue().netPayProperty());

        //compute name from employee table
        colName.setCellValueFactory(cd -> {
            
            String id = cd.getValue().getEmployeeId();
            Employee emp = employeeDAO.getEmployeeById(id);
            String name = (emp != null) ? emp.getFirstName() + " " + emp.getLastName() : "";
            return new ReadOnlyStringWrapper(name);
        });

        //compute pay type from salary info
        colPayType.setCellValueFactory(cd -> {
            
            String id = cd.getValue().getEmployeeId();
            SalaryInfo si = salaryDAO.fetchSalaryInfoByEmployeeId(id);
            String payType = (si != null) ? si.getPayType() : "";
            return new ReadOnlyStringWrapper(payType);
        });

        //deductions = medical + taxes - stipend
        colDeductions.setCellValueFactory(cd ->
                cd.getValue().medicalDeductionProperty()
                        .add(cd.getValue().stateTaxProperty())
                        .add(cd.getValue().federalTaxProperty())
                        .add(cd.getValue().socialSecurityProperty())
                        .add(cd.getValue().medicareProperty())
                        .subtract(cd.getValue().dependentStipendProperty())
        );

        //bind items
        payrollTable.setItems(payrollData);

        //refresh on focus return
        payrollTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((o, oldWin, newWin) -> {
                    if (newWin != null) {
                        newWin.focusedProperty().addListener((oo, was, isNow) -> {
                            if (isNow) payrollTable.refresh(); 
                        });
                    }
                });
            }
        });
    }

    /**
     * count weekdays between start and end inclusive
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
     * returns [start, end] where end is prev-or-same sunday and start is 6 days earlier
     */
    private LocalDate[] currentPayPeriod(LocalDate ref) {
        LocalDate end = ref.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate start = end.minusDays(6);
        return new LocalDate[] { start, end };
    }

    /**
     * calculate payroll for the current period, persist records, lock entries, populate table
     */
    @FXML
    private void onCalculatePayrollClick() throws SQLException {
        //clear old rows
        payrollData.clear();

        //period
        LocalDate[] period = currentPayPeriod(LocalDate.now());
        LocalDate startDate = period[0];
        LocalDate endDate = period[1];
        String payDate = endDate.plusDays(7).toString();
        String payPeriod = startDate + " to " + endDate;

        //employees
        List<Employee> allEmployees = employeeDAO.getAllEmployees();
        for (Employee emp : allEmployees) {
            String id = emp.getEmployeeId();
            String payType = emp.getPayType();

            //entries in period and not locked
            List<TimeEntry> entries = timeEntryDAO.fetchTimeEntriesByEmployeeId(id);

            double totalHours = 0;
            double ptoHours = 0;
            double saturdayHours = 0;

            for (TimeEntry entry : entries) {
                if (!entry.isLocked()) {
                    LocalDate entryDate = LocalDate.parse(entry.getDate());
                    if (!entryDate.isBefore(startDate) && !entryDate.isAfter(endDate)) {
                        if (entry.isPto()) {
                            ptoHours += entry.getHoursWorked();
                        } else {
                            totalHours += entry.getHoursWorked();
                            if (entryDate.getDayOfWeek().getValue() == 6) {
                                saturdayHours += entry.getHoursWorked();
                            }
                        }
                    }
                }
            }

            SalaryInfo salary = salaryDAO.fetchSalaryInfoByEmployeeId(id);
            if (salary == null) continue;

            double wage = salary.getWage();
            double grossPay;
            double overtimeHours;
            double hoursWorkedForRow;

            if (payType.equalsIgnoreCase("Salary")) {
                //salary: 8h per weekday
                int weekdays = countWeekdays(startDate, endDate);
                double assumedPaidHours = weekdays * 8.0;
                double hoursWorked = assumedPaidHours;
                overtimeHours = 0.0;
                grossPay = hoursWorked * wage;

                //pretax
                double medicalDeduction = salary.getMedicalCoverage().equalsIgnoreCase("Family") ? 100 : 50;
                double dependentStipend = 45 * salary.getNumDependents();
                double taxableIncome = grossPay - medicalDeduction - dependentStipend;

                //taxes
                double stateTax = taxableIncome * 0.0315;
                double federalTax = taxableIncome * 0.0765;
                double socialSecurity = taxableIncome * 0.062;
                double medicare = taxableIncome * 0.0145;

                double totalDeductions = medicalDeduction + stateTax + federalTax + socialSecurity + medicare;
                double netPay = grossPay - totalDeductions;

                hoursWorkedForRow = hoursWorked;

                //row
                PayrollRow row = new PayrollRow(
                        id, payPeriod, hoursWorkedForRow, wage,
                        grossPay, medicalDeduction, dependentStipend,
                        stateTax, federalTax, socialSecurity, medicare, netPay
                );
                payrollData.add(row);

                //record
                PayrollRecord record = new PayrollRecord(
                        id, startDate.toString(), endDate.toString(), payDate,
                        hoursWorkedForRow, 0.0, wage,
                        grossPay, totalDeductions, netPay, false
                );
                payrollDAO.insertPayrollRecord(record);

            } else {
                //hourly with overtime and saturday
                double weekdayHours = totalHours - saturdayHours;
                double standardOvertime = Math.max(0, weekdayHours + saturdayHours - 40);
                double regularHours = Math.max(0, weekdayHours - standardOvertime);

                overtimeHours = standardOvertime + saturdayHours;
                grossPay = (regularHours * wage) + (overtimeHours * wage * 1.5) + (ptoHours * wage);

                //pretax
                double medicalDeduction = salary.getMedicalCoverage().equalsIgnoreCase("Family") ? 100 : 50;
                double dependentStipend = 45 * salary.getNumDependents();
                double taxableIncome = grossPay - medicalDeduction - dependentStipend;

                //taxes
                double stateTax = taxableIncome * 0.0315;
                double federalTax = taxableIncome * 0.0765;
                double socialSecurity = taxableIncome * 0.062;
                double medicare = taxableIncome * 0.0145;

                double totalDeductions = medicalDeduction + stateTax + federalTax + socialSecurity + medicare;
                double netPay = grossPay - totalDeductions;

                hoursWorkedForRow = totalHours + ptoHours;

                //row
                PayrollRow row = new PayrollRow(
                        id, payPeriod, hoursWorkedForRow, wage,
                        grossPay, medicalDeduction, dependentStipend,
                        stateTax, federalTax, socialSecurity, medicare, netPay
                );
                payrollData.add(row);

                //record
                PayrollRecord record = new PayrollRecord(
                        id, startDate.toString(), endDate.toString(), payDate,
                        hoursWorkedForRow, overtimeHours, wage,
                        grossPay, totalDeductions, netPay, false
                );
                payrollDAO.insertPayrollRecord(record);
            }
        }

        //lock entries for the period
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String start = startDate.format(formatter);
        String end = endDate.format(formatter);
        for (PayrollRow row : payrollData) {
            timeEntryDAO.lockEntries(row.getEmployeeId(), start, end);
        }

        //alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Entries Locked");
        alert.setContentText("All time entries for this payroll period are now locked.");
        alert.showAndWait();

        //refresh
        payrollTable.refresh();
    }

    /**
     * export the table to csv with a period header
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

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText("Payroll report exported successfully.");
                alert.setContentText("Saved to:\n" + file.getAbsolutePath());
                alert.showAndWait();

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Failed");
                alert.setHeaderText("Failed to export report.");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    /**
     * back to admin dashboard
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
            e.printStackTrace(); //this is a comment
        }
    }

    /**
     * generate hr report for the current period
     */
    @FXML
    private void onGenerateHRReportClick() {
        if (payrollData.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Data");
            alert.setHeaderText("No payroll rows found for this period");
            alert.setContentText("Calculate payroll first, then generate the HR report.");
            alert.showAndWait();
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
                LocalDate endDate = period[1];
                String payPeriod = startDate + " to " + endDate;

                HRReportGenerator generator = new HRReportGenerator();
                var data = generator.getPayrollData(startDate, endDate);
                generator.generateReport(data, file.getAbsolutePath(), payPeriod);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Report Generated");
                alert.setHeaderText("HR Report Generated Successfully");
                alert.setContentText("Saved to: " + file.getAbsolutePath());
                alert.showAndWait();

            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to generate HR report");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }
}
