# User Guide – Payroll System

This guide explains how to use the Payroll System from both Admin and Employees.

---

## 1. Login Credentials
- **Admin**
  - User ID: `HR0001`
  - Password: `password123`

- **Employees**
  - IDs: `E001` to `E012`
  - Password format: `fnamelnameYYYYMMDD`
    - Example: John Smith, DOB 
	Test Salary Employee Login: E001
					  password: johnsmith19850314
	
	Test Hourly Employee Login: E004
					  password: sarahbrown19820118
					  
	Test Terminated Employee Login: E002
						  password: emilyjohnson19900722

---

## 2. Admin Tasks
After login, Admin sees:
- **Manage Employees**
- **Payroll**
- **Records**
- **Logout**

### Manage Employees
- **Add**: Blank form to add a new employee  
- **Edit**: Pre-filled form for the selected employee  
- **View**: Read-only view  
- **Delete**: Remove the selected employee  
- **Back**: Return to Admin menu

### Payroll
- **Calculate Payroll**: Run payroll calculations  
- **Generate HR Report**: View summary for approval  
- **Export Report**: Save payroll output  
- **Lock Payroll Period**: Prevent further edits to entries  
- **Back**: Return to Admin menu

### Records
Displays all payroll data for employees in the current pay period.

---

## 3. Employee Tasks
After login, employees see:
- **Timesheet**
- **Payroll History**
- **Employee Profile**

### Timesheet
- **Hourly Employees**: Enter daily hours (overtime auto-calculated)  
- **Salary Employees**: Enter only PTO (hours auto-handled)  
- **Note**: Cannot add/edit entries once Admin locks the payroll

### Payroll History
View recent or past payroll information, including gross, deductions, and net pay.

### Employee Profile
View personal job and wage information (read-only).

---

## 4. Payroll Rules
- **Hourly Employees**:
  - Overtime is 1.5× pay for hours over 8/day or any Saturday
- **Salary Employees**:
  - Regular hours pre-credited; only log PTO
- **Deductions**:
  - Medical: $50 (single) / $100 (family)
  - Dependents: $45 each
  - Taxes: State 3.15%, Federal 7.65%, Social Security 6.2%, Medicare 1.45%

---

## 5. Error Feedback
- **Invalid login**: “Incorrect ID or Password”  
- **Locked pay period**: “Entries cannot be modified after payroll has been locked.”  
- **Invalid inputs**: Prompts for correction (e.g., date formats, missing fields)

---

## 6. Workflow Summary
- **Admin**:
  1. Manage employee records: Add, Edit, View, Delete
  2. Calculate payroll and Lock payroll period
  3. Generate report and export


- **Employee**:
  1. Timesheet entry (hours or PTO)
  2. View payroll history
  3. Check profile

---

## 7. Troubleshooting & Tips
- Unable to run app? Check Java and JavaFX configurations.
- Missing payroll.db? Ensure it’s located at project root.
- Payroll totals seem off? Check pay type, medical, dependents, overtime.

---

## 8. Structure & Navigation
- Start with **Manage Employees** or **Timesheet** based on user role  


