package com.example.expensetrackerapp.utils;

import android.content.Context;
import android.os.Environment;

import com.example.expensetrackerapp.data.local.entity.Expense;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for exporting expense data to CSV format.
 */
public class ExportUtils {

    private static final String TAG = "ExportUtils";

    public interface ExportCallback {
        void onSuccess(String filePath);

        void onFailure(String error);
    }

    /**
     * Export expenses to a CSV file.
     */
    public static void exportToCSV(Context context, List<Expense> expenses, String currency, ExportCallback callback) {
        try {
            // Create file name with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "expenses_" + timestamp + ".csv";

            // Get external files directory
            File exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (exportDir == null) {
                callback.onFailure("Storage not available");
                return;
            }

            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, fileName);
            FileWriter writer = new FileWriter(file);

            // Write CSV header
            writer.append("Date,Category,Type,Amount,Notes\n");

            // Write expense data
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            for (Expense expense : expenses) {
                String date = dateFormat.format(new Date(expense.getDate()));
                String category = escapeCSV(expense.getCategory());
                String type = expense.getType();
                String amount = String.format(Locale.US, "%.2f", expense.getAmount());
                String notes = escapeCSV(expense.getNotes() != null ? expense.getNotes() : "");

                writer.append(String.format("%s,%s,%s,%s,%s\n", date, category, type, amount, notes));
            }

            writer.flush();
            writer.close();

            callback.onSuccess(file.getAbsolutePath());

        } catch (IOException e) {
            callback.onFailure("Export failed: " + e.getMessage());
        }
    }

    /**
     * Export expenses to a simple text report.
     */
    public static void exportToTextReport(Context context, List<Expense> expenses, String currency,
            double totalIncome, double totalExpense, ExportCallback callback) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "expense_report_" + timestamp + ".txt";

            File exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (exportDir == null) {
                callback.onFailure("Storage not available");
                return;
            }

            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, fileName);
            FileWriter writer = new FileWriter(file);

            // Write report header
            writer.append("=================================\n");
            writer.append("       EXPENSE TRACKER REPORT    \n");
            writer.append("=================================\n\n");

            writer.append(String.format("Generated: %s\n\n",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())));

            // Summary
            writer.append("SUMMARY\n");
            writer.append("---------------------------------\n");
            writer.append(String.format("Total Income:  %s %s\n", currency, formatAmount(totalIncome)));
            writer.append(String.format("Total Expense: %s %s\n", currency, formatAmount(totalExpense)));
            writer.append(
                    String.format("Balance:       %s %s\n\n", currency, formatAmount(totalIncome - totalExpense)));

            // Transaction list
            writer.append("TRANSACTIONS\n");
            writer.append("---------------------------------\n");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            for (Expense expense : expenses) {
                String date = dateFormat.format(new Date(expense.getDate()));
                String sign = Constants.TYPE_EXPENSE.equals(expense.getType()) ? "-" : "+";
                writer.append(String.format("%s | %s%s %s | %s",
                        date, sign, currency, formatAmount(expense.getAmount()), expense.getCategory()));
                if (expense.getNotes() != null && !expense.getNotes().isEmpty()) {
                    writer.append(" | " + expense.getNotes());
                }
                writer.append("\n");
            }

            writer.append("\n=================================\n");
            writer.append("       END OF REPORT             \n");
            writer.append("=================================\n");

            writer.flush();
            writer.close();

            callback.onSuccess(file.getAbsolutePath());

        } catch (IOException e) {
            callback.onFailure("Export failed: " + e.getMessage());
        }
    }

    /**
     * Escape special characters for CSV format.
     */
    private static String escapeCSV(String value) {
        if (value == null)
            return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    /**
     * Format amount to 2 decimal places.
     */
    private static String formatAmount(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }
}
