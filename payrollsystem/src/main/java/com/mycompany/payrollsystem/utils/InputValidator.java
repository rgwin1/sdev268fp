package com.mycompany.payrollsystem.utils;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

public class InputValidator {

    public static boolean isNonEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }

    public static boolean isAlphabetic(String input) {
        return input != null && input.matches("[a-zA-Z]+");
    }



public static boolean isValidEmail(String email) {
    return EmailValidator.getInstance().isValid(email);
}

    public static boolean isValidGender(String gender) {
        return gender != null && (
                gender.equalsIgnoreCase("Male") ||
                gender.equalsIgnoreCase("Female")
        );
    }

    public static boolean isValidState(String state) {
        return state != null && state.matches("^[A-Z]{2}$");
    }

    public static boolean isValidZip(String zip) {
        return zip != null && zip.matches("^\\d{5}$");
    }

    public static boolean isValidDateOfBirth(String dob) {
        try {
            LocalDate birthDate = LocalDate.parse(dob);
            return Period.between(birthDate, LocalDate.now()).getYears() >= 18;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidWage(double wage) {
        return wage >= 7.25;
    }

    public static boolean isValidPayType(String type) {
        return type != null && (
                type.equalsIgnoreCase("Hourly") ||
                type.equalsIgnoreCase("Salary")
        );
    }

    public static boolean isValidMedicalCoverage(String value) {
        return value != null && (
                value.equalsIgnoreCase("Single") ||
                value.equalsIgnoreCase("Family")
        );
    }

    public static boolean isValidDependents(int count) {
        return count >= 0;
    }

    public static boolean isValidHoursWorked(double hours) {
        return hours >= 0 && hours <= 24;
    }

    public static boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
