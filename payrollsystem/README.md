# Payroll System – SDEV268 Final Project

## Overview
The **Payroll System** is a JavaFX application using SQLite for data persistence. It supports two user roles:  
- **Admin (HR)**: Manage employees, run payroll, generate reports  
- **Employee**: Log hours, submit PTO, view payroll history  

This is a **Maven** project that runs in NetBeans or any IDE with Maven support.

---

## Requirements
- **Java**: Version 17 or higher  
- **JavaFX**: Configured via Maven dependencies  
- **Maven**: Required for builds and dependency management  
- **SQLite JDBC Driver**: Included as a dependency  
- **Database**: `payroll.db` located at the project root, preloaded with test data

---

## Installation & Setup
1. Clone the repository:
https://github.com/rgwin1/sdev268fp.git

2. Open the project in NetBeans (or any IDE with Maven support) and allow it to resolve dependencies.
3. Build the project:

4. Make sure `payroll.db` is present in the project root.
5. Run the application:
- **In IDE**: Use the Run button.
- **Command Line**:
  ```
  mvn clean compile exec:java -Dexec.mainClass="com.mycompany.payrollsystem.PayrollSystem"
  ```

---

## Testing Documentation
- **Initial Testing Log**: blank template outlining test plans  
- **Completed Testing Log**: filled out with actual outcomes + screenshots  
Both are located in the `/docs` folder.

---

## Author
**Ryan Gwin**  

Folder PATH listing
Volume serial number is 9CC8-CF3E
C:.
\---payrollsystem
    |   nb-configuration.xml
    |   nbactions.xml
    |   payroll.db
    |   pom.xml
    |   
    +---docs
    |   |   README.md
    |   |   RyanGwinFPDocumentation.docx
    |   |   user_guide.md
    |   |   
    |   +---test cases
    |   |   |   CompletedTestLog.xlsx
    |   |   |   InitialTestLog.xlsx
    |   |   |   
    |   |   \---screenshots
    |   |       |   tc-001.png
    |   |       |   TC-002.png
    |   |       |   TC-005.png
    |   |       |   TC-006.png
    |   |       |   TC-007.png
    |   |       |   TC-008.png
    |   |       |   TC-009.png
    |   |       |   TC-010.png
    |   |       |   TC-012.png
    |   |       |   TC-014.png
    |   |       |   TC-015.png
    |   |       |   TC-016.png
    |   |       |   TC-017.png
    |   |       |   TC-023.png
    |   |       |   
    |   |       +---TC-003
    |   |       |       TC-003A.png
    |   |       |       TC-003B.png
    |   |       |       
    |   |       +---TC-004
    |   |       |       TC-004A.png
    |   |       |       TC-004B.png
    |   |       |       
    |   |       +---TC-011
    |   |       |       TC-011A.png
    |   |       |       TC-011B.png
    |   |       |       
    |   |       +---TC-013
    |   |       |       TC-013A.png
    |   |       |       TC-013B.png
    |   |       |       
    |   |       +---TC-018
    |   |       |       TC-018A.png
    |   |       |       TC-018B.png
    |   |       |       
    |   |       +---TC-019
    |   |       |       TC-019A.png
    |   |       |       TC-019B.png
    |   |       |       
    |   |       +---TC-020
    |   |       |       TC-020A.png
    |   |       |       TC-020B.png
    |   |       |       TC-020C.png
    |   |       |       TC-020D.png
    |   |       |       
    |   |       +---TC-021
    |   |       |       TC-021A.png
    |   |       |       TC-021B.png
    |   |       |       
    |   |       \---TC-022
    |   |               TC-022.png
    |   |               TC-022Employees.png
    |   |               TC-022Payroll.png
    |   |               TC-022SalaryInfo.png
    |   |               TC-022TimeEntries.png
    |   |               TC-022Users.png
    |   |               
    |   \---uml
    |           uml-usecase.png
    |           
    +---src
    |   +---main
    |   |   +---java
    |   |   |   \---com
    |   |   |       \---mycompany
    |   |   |           \---payrollsystem
    |   |   |               |   Config.java
    |   |   |               |   PayrollSystem.java
    |   |   |               |   
    |   |   |               +---controllers
    |   |   |               |       AddEmployeeController.java
    |   |   |               |       AdminDashboardController.java
    |   |   |               |       AdminReportsController.java
    |   |   |               |       EditEmployeeController.java
    |   |   |               |       EmployeeDashboardController.java
    |   |   |               |       EmployeePayrollHistoryController.java
    |   |   |               |       EmployeeProfileController.java
    |   |   |               |       LoginController.java
    |   |   |               |       ManageEmployeesController.java
    |   |   |               |       PayrollPageController.java
    |   |   |               |       TimeEntryController.java
    |   |   |               |       ViewEmployeeController.java
    |   |   |               |       
    |   |   |               +---dao
    |   |   |               |       EmployeeDAO.java
    |   |   |               |       PayrollDAO.java
    |   |   |               |       SalaryInfoDAO.java
    |   |   |               |       TimeEntryDAO.java
    |   |   |               |       UserDAO.java
    |   |   |               |       
    |   |   |               +---models
    |   |   |               |       Employee.java
    |   |   |               |       PayrollRecord.java
    |   |   |               |       PayrollRow.java
    |   |   |               |       SalaryInfo.java
    |   |   |               |       TimeEntry.java
    |   |   |               |       
    |   |   |               +---reports
    |   |   |               |       HRReportGenerator.java
    |   |   |               |       
    |   |   |               \---utils
    |   |   |                       DatabaseManager.java
    |   |   |                       HashUtil.java
    |   |   |                       InputValidator.java
    |   |   |                       RecursiveUtils.java
    |   |   |                       Session.java
    |   |   |                       
    |   |   \---resources
    |   |           add_employee.fxml
    |   |           admin_dashboard.fxml
    |   |           admin_reports.fxml
    |   |           app.css
    |   |           edit_employee.fxml
    |   |           employee_dashboard.fxml
    |   |           employee_payroll_history.fxml
    |   |           employee_profile.fxml
    |   |           employee_timesheet.fxml
    |   |           login.fxml
    |   |           manage_employees.fxml
    |   |           payroll_page.fxml
    |   |           view_employee.fxml
