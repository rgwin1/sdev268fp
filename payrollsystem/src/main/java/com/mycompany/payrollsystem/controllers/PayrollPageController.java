package com.mycompany.payrollsystem.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.mycompany.payrollsystem.models.PayrollRow;
import com.mycompany.payrollsystem.models.TimeEntry;
import com.mycompany.payrollsystem.models.SalaryInfo;
import com.mycompany.payrollsystem.models.PayrollRecord;
import com.mycompany.payrollsystem.dao.EmployeeDAO;
import com.mycompany.payrollsystem.dao.TimeEntryDAO;
import com.mycompany.payrollsystem.dao.SalaryInfoDAO;
import com.mycompany.payrollsystem.dao.PayrollDAO;
import com.mycompany.payrollsystem.models.Employee;
import com.mycompany.payrollsystem.reports.HRReportGenerator;
import java.util.List;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;

public class PayrollPageController {

    @FXML private TableView<PayrollRow> payrollTable;
    @FXML private TableColumn<PayrollRow, String> colEmployeeId;
    @FXML private TableColumn<PayrollRow, String> colName;
    @FXML private TableColumn<PayrollRow, String> colPayType;
    @FXML private TableColumn<PayrollRow, Double> colGross;
    @FXML private TableColumn<PayrollRow, Double> colDeductions;
    @FXML private TableColumn<PayrollRow, Double> colNet;

    @FXML private Button calculatePayrollButton;
    @FXML private Button generateHRReportButton;
    @FXML private Button exportReportButton;
    @FXML private Button backButton;

    private ObservableList<PayrollRow> payrollData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        payrollTable.setItems(payrollData);
    }

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

    private LocalDate[] currentPayPeriod(LocalDate ref) {
        LocalDate end = ref.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate start = end.minusDays(6);
        return new LocalDate[] { start, end };
    }

    @FXML
    private void onCalculatePayrollClick() throws SQLException {
        payrollTable.getItems().clear();

        EmployeeDAO employeeDAO = new EmployeeDAO();
        TimeEntryDAO timeEntryDAO = new TimeEntryDAO();
        SalaryInfoDAO salaryDAO = new SalaryInfoDAO();
        PayrollDAO payrollDAO = new PayrollDAO();

        List<Employee> allEmployees = employeeDAO.getAllEmployees();
        LocalDate[] period = currentPayPeriod(LocalDate.now());
        LocalDate startDate = period[0];
        LocalDate endDate = period[1];
        String payDate = endDate.plusDays(7).toString();
        String payPeriod = startDate + " to " + endDate;

        for (Employee emp : allEmployees) {
            String id = emp.getEmployeeId();
            String name = emp.getFirstName() + " " + emp.getLastName();
            String payType = emp.getPayType();

            List<TimeEntry> entries = timeEntryDAO.fetchTimeEntriesByEmployeeId(id);

            double totalHours = 0;
            double ptoHours = 0;
            double saturdayHours = 0;

            for (TimeEntry entry : entries) {
                if (!entry.isLocked()) {
                    LocalDate entryDate = LocalDate.parse(entry.getDate());
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

            SalaryInfo salary = salaryDAO.fetchSalaryInfoByEmployeeId(id);
            if (salary == null) continue;

            double wage = salary.getWage();
            double grossPay;
            double overtimeHours = 0;
            double hoursWorkedForRow;

            if (payType.equalsIgnoreCase("Salary")) {
                int weekdays = countWeekdays(startDate, endDate);
                double assumedPaidHours = weekdays * 8.0;
                double hoursWorked = assumedPaidHours;
                overtimeHours = 0.0;
                grossPay = hoursWorked * wage;

                double medicalDeduction = salary.getMedicalCoverage().equalsIgnoreCase("Family") ? 100 : 50;
                double dependentStipend = 45 * salary.getNumDependents();
                double taxableIncome = grossPay - medicalDeduction - dependentStipend;

                double stateTax = taxableIncome * 0.0315;
                double federalTax = taxableIncome * 0.0765;
                double socialSecurity = taxableIncome * 0.062;
                double medicare = taxableIncome * 0.0145;

                double totalDeductions = medicalDeduction + stateTax + federalTax + socialSecurity + medicare;
                double netPay = grossPay - totalDeductions;

                hoursWorkedForRow = hoursWorked;

                PayrollRow row = new PayrollRow(id, payPeriod, hoursWorkedForRow, wage, grossPay, medicalDeduction, dependentStipend, stateTax, federalTax, socialSecurity, medicare, netPay);
                payrollTable.getItems().add(row);

                PayrollRecord record = new PayrollRecord(
                    id, startDate.toString(), endDate.toString(), payDate,
                    hoursWorkedForRow, overtimeHours, wage,
                    grossPay, totalDeductions, netPay, false
                );
                payrollDAO.insertPayrollRecord(record);

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

                PayrollRow row = new PayrollRow(id, payPeriod, hoursWorkedForRow, wage, grossPay, medicalDeduction, dependentStipend, stateTax, federalTax, socialSecurity, medicare, netPay);
                payrollTable.getItems().add(row);

                PayrollRecord record = new PayrollRecord(
                    id, startDate.toString(), endDate.toString(), payDate,
                    hoursWorkedForRow, overtimeHours, wage,
                    grossPay, totalDeductions, netPay, false
                );
                payrollDAO.insertPayrollRecord(record);
            }
        }

        TimeEntryDAO dao = new TimeEntryDAO();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String start = startDate.format(formatter);
        String end = endDate.format(formatter);

        for (PayrollRow row : payrollTable.getItems()) {
            dao.lockEntries(row.getEmployeeId(), start, end);
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Entries Locked");
        alert.setContentText("All time entries for this payroll period are now locked.");
        alert.showAndWait();
    }

    @FXML
    private void onExportReportClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Payroll Report");

        String payPeriodForName = payrollTable.getItems().isEmpty()
            ? ""
            : payrollTable.getItems().get(0).getPayPeriod()
                  .replace(" ", "_")
                  .replace("to", "-");

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("payroll_report_" + payPeriodForName + ".csv");

        File file = fileChooser.showSaveDialog(payrollTable.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {

                if (!payrollTable.getItems().isEmpty()) {
                    writer.println("Pay Period: " + payrollTable.getItems().get(0).getPayPeriod());
                }

                writer.println("Employee ID,Pay Period,Hours Worked,Wage,Gross Pay,Medical Deduction,Dependent Stipend,State Tax,Federal Tax,Social Security,Medicare,Net Pay");

                for (PayrollRow row : payrollTable.getItems()) {
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

    @FXML
    private void onBackClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onGenerateHRReportClick() {
        if (payrollTable.getItems().isEmpty()) {
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
