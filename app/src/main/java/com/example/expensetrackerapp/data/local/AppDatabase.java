package com.example.expensetrackerapp.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.expensetrackerapp.data.local.dao.CategoryDao;
import com.example.expensetrackerapp.data.local.dao.ExpenseDao;
import com.example.expensetrackerapp.data.local.dao.UserProfileDao;
import com.example.expensetrackerapp.data.local.entity.Category;
import com.example.expensetrackerapp.data.local.entity.Expense;
import com.example.expensetrackerapp.data.local.entity.UserProfile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Room Database for the Expense Tracker App.
 * Includes Expense, UserProfile, and Category entities.
 */
@Database(entities = { Expense.class, UserProfile.class, Category.class }, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // DAOs
    public abstract ExpenseDao expenseDao();

    public abstract UserProfileDao userProfileDao();

    public abstract CategoryDao categoryDao();

    // Singleton instance
    private static volatile AppDatabase INSTANCE;

    // Database write executor
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Get database instance with lazy initialization.
     */
    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "expense_tracker_database")
                            .addCallback(prepopulateCallback)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback to prepopulate database with default categories.
     */
    private static final RoomDatabase.Callback prepopulateCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // Prepopulate with default categories
            databaseWriteExecutor.execute(() -> {
                CategoryDao categoryDao = INSTANCE.categoryDao();

                // Check if categories already exist
                if (categoryDao.getCategoryCount() == 0) {
                    // Default categories with icons (emoji) and colors
                    categoryDao.insert(new Category("Food", "🍕", "#FF6B6B", true));
                    categoryDao.insert(new Category("Transport", "🚗", "#4ECDC4", true));
                    categoryDao.insert(new Category("Shopping", "🛒", "#45B7D1", true));
                    categoryDao.insert(new Category("Bills", "💡", "#96CEB4", true));
                    categoryDao.insert(new Category("Entertainment", "🎬", "#DDA0DD", true));
                    categoryDao.insert(new Category("Healthcare", "🏥", "#98D8C8", true));
                    categoryDao.insert(new Category("Education", "📚", "#F7DC6F", true));
                    categoryDao.insert(new Category("Salary", "💰", "#52C41A", true));
                    categoryDao.insert(new Category("Investment", "📈", "#722ED1", true));
                    categoryDao.insert(new Category("Others", "📦", "#95A5A6", true));
                }
            });
        }
    };
}
