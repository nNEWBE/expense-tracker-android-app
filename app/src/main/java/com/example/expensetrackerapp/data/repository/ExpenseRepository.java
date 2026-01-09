package com.example.expensetrackerapp.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.expensetrackerapp.auth.AuthManager;
import com.example.expensetrackerapp.data.local.AppDatabase;
import com.example.expensetrackerapp.data.local.dao.ExpenseDao;
import com.example.expensetrackerapp.data.local.entity.Expense;
import com.example.expensetrackerapp.utils.Constants;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for Expense data.
 * Handles local Room database and Firestore cloud sync.
 */
public class ExpenseRepository {

    private final ExpenseDao expenseDao;
    private final FirebaseFirestore firestore;
    private final AuthManager authManager;
    private static ExpenseRepository instance;

    private ExpenseRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        expenseDao = db.expenseDao();
        firestore = FirebaseFirestore.getInstance();
        authManager = AuthManager.getInstance();
    }

    public static synchronized ExpenseRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ExpenseRepository(context);
        }
        return instance;
    }

    /**
     * Get current user ID.
     */
    private String getCurrentUserId() {
        return authManager.getCurrentUserId();
    }

    /**
     * Insert expense (local and cloud if logged in).
     */
    public void insert(Expense expense, OnOperationCompleteListener listener) {
        String userId = getCurrentUserId();
        expense.setUserId(userId);
        expense.setUpdatedAt(System.currentTimeMillis());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Insert locally first
            long id = expenseDao.insert(expense);
            expense.setId(id);

            // Sync to cloud if logged in
            if (!authManager.isGuest()) {
                syncToCloud(expense, listener);
            } else {
                if (listener != null) {
                    listener.onSuccess();
                }
            }
        });
    }

    /**
     * Update expense.
     */
    public void update(Expense expense, OnOperationCompleteListener listener) {
        expense.setUpdatedAt(System.currentTimeMillis());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            expenseDao.update(expense);

            // Sync to cloud if logged in
            if (!authManager.isGuest() && expense.getFirestoreId() != null) {
                updateInCloud(expense, listener);
            } else {
                if (listener != null) {
                    listener.onSuccess();
                }
            }
        });
    }

    /**
     * Delete expense.
     */
    public void delete(Expense expense, OnOperationCompleteListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            expenseDao.delete(expense);

            // Delete from cloud if logged in
            if (!authManager.isGuest() && expense.getFirestoreId() != null) {
                deleteFromCloud(expense, listener);
            } else {
                if (listener != null) {
                    listener.onSuccess();
                }
            }
        });
    }

    /**
     * Get all expenses for current user.
     */
    public LiveData<List<Expense>> getAllExpenses() {
        return expenseDao.getAllExpenses(getCurrentUserId());
    }

    /**
     * Get expenses by type.
     */
    public LiveData<List<Expense>> getByType(String type) {
        return expenseDao.getByType(getCurrentUserId(), type);
    }

    /**
     * Get expenses by date range.
     */
    public LiveData<List<Expense>> getByDateRange(long startDate, long endDate) {
        return expenseDao.getByDateRange(getCurrentUserId(), startDate, endDate);
    }

    /**
     * Get expenses by category.
     */
    public LiveData<List<Expense>> getByCategory(String category) {
        return expenseDao.getByCategory(getCurrentUserId(), category);
    }

    /**
     * Get expenses by category and date range.
     */
    public LiveData<List<Expense>> getByCategoryAndDateRange(String category, long startDate, long endDate) {
        return expenseDao.getByCategoryAndDateRange(getCurrentUserId(), category, startDate, endDate);
    }

    /**
     * Get total expenses by date range.
     */
    public LiveData<Double> getTotalExpenses(long startDate, long endDate) {
        return expenseDao.getTotalExpensesByDateRange(getCurrentUserId(), startDate, endDate);
    }

    /**
     * Get total income by date range.
     */
    public LiveData<Double> getTotalIncome(long startDate, long endDate) {
        return expenseDao.getTotalIncomeByDateRange(getCurrentUserId(), startDate, endDate);
    }

    /**
     * Get total by category.
     */
    public LiveData<Double> getTotalByCategory(String category, long startDate, long endDate) {
        return expenseDao.getTotalByCategory(getCurrentUserId(), category, startDate, endDate);
    }

    /**
     * Search by notes.
     */
    public LiveData<List<Expense>> searchByNotes(String query) {
        return expenseDao.searchByNotes(getCurrentUserId(), query);
    }

    /**
     * Search by amount range.
     */
    public LiveData<List<Expense>> searchByAmountRange(double minAmount, double maxAmount) {
        return expenseDao.searchByAmountRange(getCurrentUserId(), minAmount, maxAmount);
    }

    /**
     * Get expense by ID.
     */
    public LiveData<Expense> getExpenseById(long id) {
        return expenseDao.getExpenseById(id);
    }

    /**
     * Get expense count.
     */
    public LiveData<Integer> getExpenseCount() {
        return expenseDao.getExpenseCount(getCurrentUserId());
    }

    /**
     * Sync expense to Firestore.
     */
    private void syncToCloud(Expense expense, OnOperationCompleteListener listener) {
        String userId = getCurrentUserId();

        Map<String, Object> data = expenseToMap(expense);

        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_EXPENSES)
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    // Update local record with Firestore ID
                    String firestoreId = documentReference.getId();
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        expenseDao.markAsSynced(expense.getId(), firestoreId);
                    });

                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Update expense in Firestore.
     */
    private void updateInCloud(Expense expense, OnOperationCompleteListener listener) {
        String userId = getCurrentUserId();

        Map<String, Object> data = expenseToMap(expense);

        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_EXPENSES)
                .document(expense.getFirestoreId())
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Delete expense from Firestore.
     */
    private void deleteFromCloud(Expense expense, OnOperationCompleteListener listener) {
        String userId = getCurrentUserId();

        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_EXPENSES)
                .document(expense.getFirestoreId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Sync all guest data to cloud after login.
     */
    public void syncGuestDataToCloud(OnSyncCompleteListener listener) {
        if (authManager.isGuest()) {
            if (listener != null) {
                listener.onFailure("User is not logged in");
            }
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Get all guest expenses
            List<Expense> guestExpenses = expenseDao.getAllExpensesSync(Constants.USER_GUEST);

            if (guestExpenses.isEmpty()) {
                if (listener != null) {
                    listener.onSuccess(0);
                }
                return;
            }

            String userId = getCurrentUserId();
            int[] syncedCount = { 0 };
            int totalCount = guestExpenses.size();

            for (Expense expense : guestExpenses) {
                // Update user ID
                expense.setUserId(userId);
                expense.setUpdatedAt(System.currentTimeMillis());

                Map<String, Object> data = expenseToMap(expense);

                firestore.collection(Constants.COLLECTION_USERS)
                        .document(userId)
                        .collection(Constants.COLLECTION_EXPENSES)
                        .add(data)
                        .addOnSuccessListener(documentReference -> {
                            // Update local record
                            AppDatabase.databaseWriteExecutor.execute(() -> {
                                expense.setFirestoreId(documentReference.getId());
                                expense.setSynced(true);
                                expenseDao.update(expense);

                                syncedCount[0]++;
                                if (syncedCount[0] == totalCount) {
                                    // Delete old guest records
                                    expenseDao.deleteAllByUser(Constants.USER_GUEST);

                                    if (listener != null) {
                                        listener.onSuccess(syncedCount[0]);
                                    }
                                }
                            });
                        })
                        .addOnFailureListener(e -> {
                            if (listener != null) {
                                listener.onFailure(e.getMessage());
                            }
                        });
            }
        });
    }

    /**
     * Convert Expense to Map for Firestore.
     */
    private Map<String, Object> expenseToMap(Expense expense) {
        Map<String, Object> data = new HashMap<>();
        data.put("amount", expense.getAmount());
        data.put("category", expense.getCategory());
        data.put("date", expense.getDate());
        data.put("notes", expense.getNotes());
        data.put("type", expense.getType());
        data.put("userId", expense.getUserId());
        data.put("createdAt", expense.getCreatedAt());
        data.put("updatedAt", expense.getUpdatedAt());
        return data;
    }

    /**
     * Callback interface for operations.
     */
    public interface OnOperationCompleteListener {
        void onSuccess();

        void onFailure(String error);
    }

    /**
     * Callback interface for sync operations.
     */
    public interface OnSyncCompleteListener {
        void onSuccess(int syncedCount);

        void onFailure(String error);
    }
}
