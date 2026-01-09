package com.example.expensetrackerapp.utils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for date operations.
 */
public class DateUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd MMM yyyy, hh:mm a",
            Locale.getDefault());
    private static final SimpleDateFormat MONTH_YEAR_FORMAT = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat DAY_MONTH_FORMAT = new SimpleDateFormat("dd MMM", Locale.getDefault());
    private static final SimpleDateFormat WEEK_DAY_FORMAT = new SimpleDateFormat("EEE", Locale.getDefault());

    /**
     * Format timestamp to readable date string.
     */
    public static String formatDate(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }

    /**
     * Format timestamp to readable date-time string.
     */
    public static String formatDateTime(long timestamp) {
        return DATE_TIME_FORMAT.format(new Date(timestamp));
    }

    /**
     * Format timestamp to month-year string.
     */
    public static String formatMonthYear(long timestamp) {
        return MONTH_YEAR_FORMAT.format(new Date(timestamp));
    }

    /**
     * Format timestamp to day-month string.
     */
    public static String formatDayMonth(long timestamp) {
        return DAY_MONTH_FORMAT.format(new Date(timestamp));
    }

    /**
     * Get weekday name from timestamp.
     */
    public static String getWeekDay(long timestamp) {
        return WEEK_DAY_FORMAT.format(new Date(timestamp));
    }

    /**
     * Get start of today in milliseconds.
     */
    public static long getStartOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Get end of today in milliseconds.
     */
    public static long getEndOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    /**
     * Get start of current week in milliseconds.
     */
    public static long getStartOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Get end of current week in milliseconds.
     */
    public static long getEndOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    /**
     * Get start of current month in milliseconds.
     */
    public static long getStartOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Get end of current month in milliseconds.
     */
    public static long getEndOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    /**
     * Get start of current year in milliseconds.
     */
    public static long getStartOfYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Get end of current year in milliseconds.
     */
    public static long getEndOfYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    /**
     * Get start and end based on filter type.
     */
    public static long[] getDateRangeForFilter(int filterType) {
        switch (filterType) {
            case Constants.FILTER_TODAY:
                return new long[] { getStartOfToday(), getEndOfToday() };
            case Constants.FILTER_WEEK:
                return new long[] { getStartOfWeek(), getEndOfWeek() };
            case Constants.FILTER_MONTH:
                return new long[] { getStartOfMonth(), getEndOfMonth() };
            case Constants.FILTER_YEAR:
                return new long[] { getStartOfYear(), getEndOfYear() };
            case Constants.FILTER_ALL:
            default:
                return new long[] { 0, Long.MAX_VALUE };
        }
    }

    /**
     * Check if timestamp is today.
     */
    public static boolean isToday(long timestamp) {
        long startOfToday = getStartOfToday();
        long endOfToday = getEndOfToday();
        return timestamp >= startOfToday && timestamp <= endOfToday;
    }

    /**
     * Get relative date string (Today, Yesterday, or date).
     */
    public static String getRelativeDateString(long timestamp) {
        if (isToday(timestamp)) {
            return "Today";
        }

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        yesterday.set(Calendar.MILLISECOND, 0);

        Calendar endOfYesterday = Calendar.getInstance();
        endOfYesterday.add(Calendar.DAY_OF_YEAR, -1);
        endOfYesterday.set(Calendar.HOUR_OF_DAY, 23);
        endOfYesterday.set(Calendar.MINUTE, 59);
        endOfYesterday.set(Calendar.SECOND, 59);
        endOfYesterday.set(Calendar.MILLISECOND, 999);

        if (timestamp >= yesterday.getTimeInMillis() && timestamp <= endOfYesterday.getTimeInMillis()) {
            return "Yesterday";
        }

        return formatDate(timestamp);
    }
}
