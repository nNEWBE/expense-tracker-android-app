package com.example.expensetrackerapp.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.expensetrackerapp.auth.AuthManager;
import com.example.expensetrackerapp.data.local.AppDatabase;
import com.example.expensetrackerapp.data.local.dao.UserProfileDao;
import com.example.expensetrackerapp.data.local.entity.UserProfile;
import com.example.expensetrackerapp.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository for UserProfile data.
 * Handles local Room database and Firestore cloud sync.
 */
public class UserRepository {

    private final UserProfileDao userProfileDao;
    private final FirebaseFirestore firestore;
    private final AuthManager authManager;
    private static UserRepository instance;

    private UserRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userProfileDao = db.userProfileDao();
        firestore = FirebaseFirestore.getInstance();
        authManager = AuthManager.getInstance();
    }

    public static synchronized UserRepository getInstance(Context context) {
        if (instance == null) {
            instance = new UserRepository(context);
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
     * Get current user profile.
     */
    public LiveData<UserProfile> getProfile() {
        return userProfileDao.getProfile(getCurrentUserId());
    }

    /**
     * Get profile synchronously.
     */
    public UserProfile getProfileSync() {
        return userProfileDao.getProfileSync(getCurrentUserId());
    }

    /**
     * Create or update profile.
     */
    public void saveProfile(UserProfile profile, OnOperationCompleteListener listener) {
        profile.setUpdatedAt(System.currentTimeMillis());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            int exists = userProfileDao.profileExists(profile.getUserId());

            if (exists > 0) {
                userProfileDao.update(profile);
            } else {
                userProfileDao.insert(profile);
            }

            // Sync to cloud if logged in
            if (!authManager.isGuest()) {
                syncProfileToCloud(profile, listener);
            } else {
                if (listener != null) {
                    listener.onSuccess();
                }
            }
        });
    }

    /**
     * Update monthly budget.
     */
    public void updateMonthlyBudget(double budget, OnOperationCompleteListener listener) {
        String userId = getCurrentUserId();
        long timestamp = System.currentTimeMillis();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            userProfileDao.updateMonthlyBudget(userId, budget, timestamp);

            // Sync to cloud if logged in
            if (!authManager.isGuest()) {
                updateBudgetInCloud(budget, listener);
            } else {
                if (listener != null) {
                    listener.onSuccess();
                }
            }
        });
    }

    /**
     * Get monthly budget.
     */
    public LiveData<Double> getMonthlyBudget() {
        return userProfileDao.getMonthlyBudget(getCurrentUserId());
    }

    /**
     * Update currency preference.
     */
    public void updateCurrency(String currency, OnOperationCompleteListener listener) {
        String userId = getCurrentUserId();
        long timestamp = System.currentTimeMillis();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            userProfileDao.updateCurrency(userId, currency, timestamp);

            if (!authManager.isGuest()) {
                updateCurrencyInCloud(currency, listener);
            } else {
                if (listener != null) {
                    listener.onSuccess();
                }
            }
        });
    }

    /**
     * Update profile name.
     */
    public void updateName(String name, OnOperationCompleteListener listener) {
        String userId = getCurrentUserId();
        long timestamp = System.currentTimeMillis();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            userProfileDao.updateName(userId, name, timestamp);

            if (!authManager.isGuest()) {
                updateNameInCloud(name, listener);
            } else {
                if (listener != null) {
                    listener.onSuccess();
                }
            }
        });
    }

    /**
     * Update profile photo URI.
     */
    public void updatePhotoUri(String photoUri, OnOperationCompleteListener listener) {
        String userId = getCurrentUserId();
        long timestamp = System.currentTimeMillis();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            userProfileDao.updatePhotoUri(userId, photoUri, timestamp);

            if (!authManager.isGuest()) {
                updatePhotoInCloud(photoUri, listener);
            } else {
                if (listener != null) {
                    listener.onSuccess();
                }
            }
        });
    }

    /**
     * Delete user profile.
     */
    public void deleteProfile() {
        String userId = getCurrentUserId();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            userProfileDao.deleteByUserId(userId);
        });
    }

    // Cloud sync methods

    private void syncProfileToCloud(UserProfile profile, OnOperationCompleteListener listener) {
        String userId = getCurrentUserId();

        Map<String, Object> data = profileToMap(profile);

        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_PROFILE)
                .document("info")
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

    private void updateBudgetInCloud(double budget, OnOperationCompleteListener listener) {
        String userId = getCurrentUserId();

        Map<String, Object> updates = new HashMap<>();
        updates.put("monthlyBudget", budget);
        updates.put("updatedAt", System.currentTimeMillis());

        updateProfileField(userId, updates, listener);
    }

    private void updateCurrencyInCloud(String currency, OnOperationCompleteListener listener) {
        String userId = getCurrentUserId();

        Map<String, Object> updates = new HashMap<>();
        updates.put("currency", currency);
        updates.put("updatedAt", System.currentTimeMillis());

        updateProfileField(userId, updates, listener);
    }

    private void updateNameInCloud(String name, OnOperationCompleteListener listener) {
        String userId = getCurrentUserId();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("updatedAt", System.currentTimeMillis());

        updateProfileField(userId, updates, listener);
    }

    private void updatePhotoInCloud(String photoUri, OnOperationCompleteListener listener) {
        String userId = getCurrentUserId();

        Map<String, Object> updates = new HashMap<>();
        updates.put("photoUri", photoUri);
        updates.put("updatedAt", System.currentTimeMillis());

        updateProfileField(userId, updates, listener);
    }

    private void updateProfileField(String userId, Map<String, Object> updates, OnOperationCompleteListener listener) {
        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_PROFILE)
                .document("info")
                .update(updates)
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
     * Convert UserProfile to Map for Firestore.
     */
    private Map<String, Object> profileToMap(UserProfile profile) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", profile.getName());
        data.put("email", profile.getEmail());
        data.put("photoUri", profile.getPhotoUri());
        data.put("monthlyBudget", profile.getMonthlyBudget());
        data.put("currency", profile.getCurrency());
        data.put("userId", profile.getUserId());
        data.put("isGuest", profile.isGuest());
        data.put("createdAt", profile.getCreatedAt());
        data.put("updatedAt", profile.getUpdatedAt());
        return data;
    }

    /**
     * Callback interface for operations.
     */
    public interface OnOperationCompleteListener {
        void onSuccess();

        void onFailure(String error);
    }
}
