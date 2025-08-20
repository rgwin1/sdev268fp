package com.mycompany.payrollsystem.utils;

import java.util.List;

public class RecursiveUtils {

    /**
     * Recursively calculates overtime hours across a list of daily hours.
     * Any hours above 8 in a day are counted as overtime.
     */
    public static double calculateOvertime(List<Double> dailyHours, int index) {
        if (index >= dailyHours.size()) {
            return 0; // base case
        }

        double hours = dailyHours.get(index);
        double overtimeToday = hours > 8 ? hours - 8 : 0;

        // recursive step: add today's overtime to the rest
        return overtimeToday + calculateOvertime(dailyHours, index + 1);
    }
}
