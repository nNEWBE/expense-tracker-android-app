package com.example.expensetrackerapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expensetrackerapp.data.local.entity.Expense;

import java.util.List;

/**
 * Data Access Object for Expense entity.
 * Provides methods to interact with expense data in Room database.
 */
@Dao
public interface ExpenseDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Expense expense);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Expense> expenses);

    // Update operations
    @Update
    void update(Expense expense);

    // Delete operations
    @Delete
    void delete(Expense expense);

    @Query("DELETE FROM expenses WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM expenses WHERE userId = :userId")
    void deleteAllByUser(String userId);

    @Query("DELETE FROM expenses")
    void deleteAll();

    // Query operations - Get all
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<Expense>> getAllExpenses(String userId);

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    List<Expense> getAllExpensesSync(String userId);

    // Query by type (expense or income)
    @Query("SELECT * FROM expenses WHERE userId = :userId AND type = :type ORDER BY date DESC")
    LiveData<List<Expense>> getByType(String userId, String type);

    @Query("SELECT * FROM expenses WHERE userId = :userId AND type = :type ORDER BY date DESC")
    List<Expense> getByTypeSync(String userId, String type);

    // Query by date range
    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<Expense>> getByDateRange(String userId, long startDate, long endDate);

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    List<Expense> getByDateRangeSync(String userId, long startDate, long endDate);

    // Query by category
    @Query("SELECT * FROM expenses WHERE userId = :userId AND category = :category ORDER BY date DESC")
    LiveData<List<Expense>> getByCategory(String userId, String category);

    @Query("SELECT * FROM expenses WHERE userId = :userId AND category = :category AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<Expense>> getByCategoryAndDateRange(String userId, String category, long startDate, long endDate);

    // Sum operations for totals
    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE userId = :userId AND type = 'expense' AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalExpensesByDateRange(String userId, long startDate, long endDate);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE userId = :userId AND type = 'income' AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalIncomeByDateRange(String userId, long startDate, long endDate);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE userId = :userId AND type = 'expense' AND date BETWEEN :startDate AND :endDate")
    double getTotalExpensesByDateRangeSync(String userId, long startDate, long endDate);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE userId = :userId AND type = 'income' AND date BETWEEN :startDate AND :endDate")
    double getTotalIncomeByDateRangeSync(String userId, long startDate, long endDate);

    // Category-wise totals
    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE userId = :userId AND type = 'expense' AND category = :category AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalByCategory(String userId, String category, long startDate, long endDate);

    // Get unsynced expenses for cloud sync
    @Query("SELECT * FROM expenses WHERE userId = :userId AND synced = 0")
    List<Expense> getUnsyncedExpenses(String userId);

    // Mark as synced
    @Query("UPDATE expenses SET synced = 1, firestoreId = :firestoreId WHERE id = :id")
    void markAsSynced(long id, String firestoreId);

    // Search operations
    @Query("SELECT * FROM expenses WHERE userId = :userId AND notes LIKE '%' || :query || '%' ORDER BY date DESC")
    LiveData<List<Expense>> searchByNotes(String userId, String query);

    @Query("SELECT * FROM expenses WHERE userId = :userId AND amount >= :minAmount AND amount <= :maxAmount ORDER BY date DESC")
    LiveData<List<Expense>> searchByAmountRange(String userId, double minAmount, double maxAmount);

    // Get single expense by ID
    @Query("SELECT * FROM expenses WHERE id = :id")
    LiveData<Expense> getExpenseById(long id);

    @Query("SELECT * FROM expenses WHERE id = :id")
    Expense getExpenseByIdSync(long id);

    // Count operations
    @Query("SELECT COUNT(*) FROM expenses WHERE userId = :userId")
    LiveData<Integer> getExpenseCount(String userId);

    @Query("SELECT COUNT(*) FROM expenses WHERE userId = :userId AND type = :type")
    LiveData<Integer> getCountByType(String userId, String type);
}
