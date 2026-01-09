package com.example.expensetrackerapp.utils;

/**
 * Application-wide constants.
 */
public class Constants {

    // User types
    public static final String USER_GUEST = "guest";

    // Transaction types
    public static final String TYPE_EXPENSE = "expense";
    public static final String TYPE_INCOME = "income";

    // Shared Preferences keys
    public static final String PREFS_NAME = "expense_tracker_prefs";
    public static final String KEY_THEME_MODE = "theme_mode";
    public static final String KEY_CURRENCY = "currency";
    public static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    public static final String KEY_REMINDER_TIME = "reminder_time";
    public static final String KEY_APP_LOCK_ENABLED = "app_lock_enabled";
    public static final String KEY_FIRST_LAUNCH = "first_launch";
    public static final String KEY_GUEST_DATA_EXISTS = "guest_data_exists";

    // Theme modes
    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    // Currencies
    public static final String CURRENCY_BDT = "BDT";
    public static final String CURRENCY_USD = "USD";
    public static final String CURRENCY_INR = "INR";
    public static final String CURRENCY_EUR = "EUR";
    public static final String CURRENCY_GBP = "GBP";

    // Currency symbols
    public static String getCurrencySymbol(String currency) {
        switch (currency) {
            case CURRENCY_USD:
                return "$";
            case CURRENCY_INR:
                return "₹";
            case CURRENCY_EUR:
                return "€";
            case CURRENCY_GBP:
                return "£";
            case CURRENCY_BDT:
            default:
                return "৳";
        }
    }

    // Firestore collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_EXPENSES = "expenses";
    public static final String COLLECTION_INCOME = "income";
    public static final String COLLECTION_PROFILE = "profile";

    // Categories
    public static final String CATEGORY_FOOD = "Food";
    public static final String CATEGORY_TRANSPORT = "Transport";
    public static final String CATEGORY_SHOPPING = "Shopping";
    public static final String CATEGORY_BILLS = "Bills";
    public static final String CATEGORY_ENTERTAINMENT = "Entertainment";
    public static final String CATEGORY_HEALTHCARE = "Healthcare";
    public static final String CATEGORY_EDUCATION = "Education";
    public static final String CATEGORY_SALARY = "Salary";
    public static final String CATEGORY_INVESTMENT = "Investment";
    public static final String CATEGORY_OTHERS = "Others";
    
    // Income Categories
    public static final String CATEGORY_BUSINESS = "Business";
    public static final String CATEGORY_FREELANCE = "Freelance";
    public static final String CATEGORY_GIFT = "Gift";
    public static final String CATEGORY_RENTAL = "Rental";
    public static final String CATEGORY_REFUND = "Refund";

    // Date filter types
    public static final int FILTER_ALL = 0;
    public static final int FILTER_TODAY = 1;
    public static final int FILTER_WEEK = 2;
    public static final int FILTER_MONTH = 3;
    public static final int FILTER_YEAR = 4;

    // Budget warning thresholds
    public static final double BUDGET_WARNING_THRESHOLD = 0.8; // 80%
    public static final double BUDGET_EXCEEDED_THRESHOLD = 1.0; // 100%

    // WorkManager tags
    public static final String WORK_TAG_REMINDER = "expense_reminder";
    public static final String WORK_TAG_BUDGET_CHECK = "budget_check";

    // Intent extras
    public static final String EXTRA_EXPENSE_ID = "expense_id";
    public static final String EXTRA_IS_EDIT = "is_edit";
    public static final String EXTRA_TRANSACTION_TYPE = "transaction_type";
}
