package com.mycompany.payrollsystem.utils;

import org.apache.commons.validator.routines.EmailValidator;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

/**
 * utility class for validating user input fields such as
 * names, emails, dates, wages, and other payroll-related values.
 */
public class InputValidator {

    /**
     * checks that a string is non-null and not empty.
     *
     * @param input the string to check
     * @return true if input is not null and not blank
     */
    public static boolean isNonEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }

    /**
     * checks that a string contains only alphabetic characters.
     *
     * @param input the string to check
     * @return true if input is alphabetic only
     */
    public static boolean isAlphabetic(String input) {
        return input != null && input.matches("[a-zA-Z]+");
    }

    /**
     * validates that a string is a properly formatted email address.
     *
     * @param email the email string to check
     * @return true if valid email format
     */
    public static boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    /**
     * checks that gender is either "Male" or "Female".
     *
     * @param gender the gender string to check
     * @return true if gender matches allowed values
     */
    public static boolean isValidGender(String gender) {
        return gender != null && (
            gender.equalsIgnoreCase("Male") ||
            gender.equalsIgnoreCase("Female")
        );
    }

    /**
     * validates that a state code is exactly 2 uppercase letters.
     *
     * @param state the state string to check
     * @return true if state matches format
     */
    public static boolean isValidState(String state) {
        return state != null && state.matches("^[A-Z]{2}$");
    }

    /**
     * validates that a zip code is 5 digits.
     *
     * @param zip the zip code string to check
     * @return true if zip code format is valid
     */
    public static boolean isValidZip(String zip) {
        return zip != null && zip.matches("^\\d{5}$");
    }

    /**
     * validates that a date of birth is valid and at least 18 years ago.
     *
     * @param dob date of birth string in YYYY-MM-DD format
     * @return true if valid and person is 18+
     */
    public static boolean isValidDateOfBirth(String dob) {
        try {
            LocalDate birthDate = LocalDate.parse(dob);
            return Period.between(birthDate, LocalDate.now()).getYears() >= 18;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * checks that wage is not below the minimum threshold (7.25).
     *
     * @param wage the wage amount
     * @return true if wage >= 7.25
     */
    public static boolean isValidWage(double wage) {
        return wage >= 7.25;
    }

    /**
     * validates pay type as either "Hourly" or "Salary".
     *
     * @param type the pay type string
     * @return true if type is valid
     */
    public static boolean isValidPayType(String type) {
        return type != null && (
            type.equalsIgnoreCase("Hourly") ||
            type.equalsIgnoreCase("Salary")
        );
    }

    /**
     * validates medical coverage as "Single" or "Family".
     *
     * @param value medical coverage string
     * @return true if valid option
     */
    public static boolean isValidMedicalCoverage(String value) {
        return value != null && (
            value.equalsIgnoreCase("Single") ||
            value.equalsIgnoreCase("Family")
        );
    }

    /**
     * validates that number of dependents is non-negative.
     *
     * @param count number of dependents
     * @return true if count >= 0
     */
    public static boolean isValidDependents(int count) {
        return count >= 0;
    }

    /**
     * validates that hours worked is between 0 and 24.
     *
     * @param hours number of hours worked
     * @return true if within valid range
     */
    public static boolean isValidHoursWorked(double hours) {
        return hours >= 0 && hours <= 24;
    }

    /**
     * validates that a string is a properly formatted date.
     *
     * @param dateStr the date string in YYYY-MM-DD format
     * @return true if parseable into a LocalDate
     */
    public static boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
