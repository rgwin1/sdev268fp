package com.mycompany.payrollsystem.utils;

import com.mycompany.payrollsystem.models.Employee;

/**
 * session utility for tracking the currently logged-in employee.
 * 
 * this class holds a static reference to the employee object
 * representing the user currently authenticated in the system.
 * controllers and other classes can use this to determine
 * permissions, roles, and access context.
 */
public class Session {

    //static reference to the employee that is currently logged in
    public static Employee loggedInEmployee;
}
