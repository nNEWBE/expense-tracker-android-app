package com.example.expensetrackerapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Manages SharedPreferences for the app.
 */
public class PreferenceManager {

    private final SharedPreferences prefs;
    private static PreferenceManager instance;

    private PreferenceManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferenceManager(context);
        }
        return instance;
    }

    // Theme management
    public void setThemeMode(int mode) {
        prefs.edit().putInt(Constants.KEY_THEME_MODE, mode).apply();
        applyTheme(mode);
    }

    public int getThemeMode() {
        return prefs.getInt(Constants.KEY_THEME_MODE, Constants.THEME_SYSTEM);
    }

    public void applyTheme(int mode) {
        switch (mode) {
            case Constants.THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case Constants.THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case Constants.THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    // Currency management
    public void setCurrency(String currency) {
        prefs.edit().putString(Constants.KEY_CURRENCY, currency).apply();
    }

    public String getCurrency() {
        return prefs.getString(Constants.KEY_CURRENCY, Constants.CURRENCY_BDT);
    }

    public String getCurrencySymbol() {
        return Constants.getCurrencySymbol(getCurrency());
    }

    // Reminder settings
    public void setReminderEnabled(boolean enabled) {
        prefs.edit().putBoolean(Constants.KEY_REMINDER_ENABLED, enabled).apply();
    }

    public boolean isReminderEnabled() {
        return prefs.getBoolean(Constants.KEY_REMINDER_ENABLED, false);
    }

    public void setReminderTime(String time) {
        prefs.edit().putString(Constants.KEY_REMINDER_TIME, time).apply();
    }

    public String getReminderTime() {
        return prefs.getString(Constants.KEY_REMINDER_TIME, "20:00");
    }

    // App lock settings
    public void setAppLockEnabled(boolean enabled) {
        prefs.edit().putBoolean(Constants.KEY_APP_LOCK_ENABLED, enabled).apply();
    }

    public boolean isAppLockEnabled() {
        return prefs.getBoolean(Constants.KEY_APP_LOCK_ENABLED, false);
    }

    // First launch check
    public void setFirstLaunch(boolean isFirst) {
        prefs.edit().putBoolean(Constants.KEY_FIRST_LAUNCH, isFirst).apply();
    }

    public boolean isFirstLaunch() {
        return prefs.getBoolean(Constants.KEY_FIRST_LAUNCH, true);
    }

    // Guest data tracking
    public void setGuestDataExists(boolean exists) {
        prefs.edit().putBoolean(Constants.KEY_GUEST_DATA_EXISTS, exists).apply();
    }

    public boolean hasGuestData() {
        return prefs.getBoolean(Constants.KEY_GUEST_DATA_EXISTS, false);
    }

    // Clear all preferences
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
