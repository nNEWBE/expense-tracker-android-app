package com.example.expensetrackerapp.utils;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Utility class for currency formatting.
 */
public class CurrencyUtils {

    /**
     * Format amount with currency symbol.
     */
    public static String formatAmount(double amount, String currency) {
        String symbol = Constants.getCurrencySymbol(currency);
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return symbol + " " + formatter.format(amount);
    }

    /**
     * Format amount without currency symbol.
     */
    public static String formatAmountWithoutSymbol(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(amount);
    }

    /**
     * Format amount with sign (+ or -).
     */
    public static String formatAmountWithSign(double amount, String currency, boolean isExpense) {
        String symbol = Constants.getCurrencySymbol(currency);
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        String sign = isExpense ? "-" : "+";
        return sign + symbol + " " + formatter.format(Math.abs(amount));
    }

    /**
     * Parse amount string to double.
     */
    public static double parseAmount(String amountString) {
        try {
            // Remove any currency symbols and whitespace
            String cleaned = amountString.replaceAll("[^\\d.,]", "").trim();
            // Replace comma with dot for parsing
            cleaned = cleaned.replace(",", ".");
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Get currency name from code.
     */
    public static String getCurrencyName(String currencyCode) {
        switch (currencyCode) {
            case Constants.CURRENCY_BDT:
                return "Bangladeshi Taka";
            case Constants.CURRENCY_USD:
                return "US Dollar";
            case Constants.CURRENCY_INR:
                return "Indian Rupee";
            case Constants.CURRENCY_EUR:
                return "Euro";
            case Constants.CURRENCY_GBP:
                return "British Pound";
            default:
                return currencyCode;
        }
    }

    /**
     * Get all supported currencies.
     */
    public static String[] getSupportedCurrencies() {
        return new String[] {
                Constants.CURRENCY_BDT,
                Constants.CURRENCY_USD,
                Constants.CURRENCY_INR,
                Constants.CURRENCY_EUR,
                Constants.CURRENCY_GBP
        };
    }

    /**
     * Get currency display names for dropdown.
     */
    public static String[] getCurrencyDisplayNames() {
        return new String[] {
                "৳ BDT - Bangladeshi Taka",
                "$ USD - US Dollar",
                "₹ INR - Indian Rupee",
                "€ EUR - Euro",
                "£ GBP - British Pound"
        };
    }
}
