package com.example.expensetrackerapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expensetrackerapp.data.local.entity.UserProfile;

/**
 * Data Access Object for UserProfile entity.
 * Provides methods to interact with user profile data in Room database.
 */
@Dao
public interface UserProfileDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserProfile profile);

    // Update operations
    @Update
    void update(UserProfile profile);

    // Delete operations
    @Delete
    void delete(UserProfile profile);

    @Query("DELETE FROM user_profile WHERE userId = :userId")
    void deleteByUserId(String userId);

    // Query operations
    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    LiveData<UserProfile> getProfile(String userId);

    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    UserProfile getProfileSync(String userId);

    @Query("SELECT * FROM user_profile WHERE isGuest = 1 LIMIT 1")
    UserProfile getGuestProfileSync();

    @Query("SELECT * FROM user_profile WHERE isGuest = 1 LIMIT 1")
    LiveData<UserProfile> getGuestProfile();

    // Budget operations
    @Query("UPDATE user_profile SET monthlyBudget = :budget, updatedAt = :timestamp WHERE userId = :userId")
    void updateMonthlyBudget(String userId, double budget, long timestamp);

    @Query("SELECT monthlyBudget FROM user_profile WHERE userId = :userId")
    LiveData<Double> getMonthlyBudget(String userId);

    @Query("SELECT monthlyBudget FROM user_profile WHERE userId = :userId")
    double getMonthlyBudgetSync(String userId);

    // Currency operations
    @Query("UPDATE user_profile SET currency = :currency, updatedAt = :timestamp WHERE userId = :userId")
    void updateCurrency(String userId, String currency, long timestamp);

    @Query("SELECT currency FROM user_profile WHERE userId = :userId")
    String getCurrencySync(String userId);

    // Profile photo operations
    @Query("UPDATE user_profile SET photoUri = :photoUri, updatedAt = :timestamp WHERE userId = :userId")
    void updatePhotoUri(String userId, String photoUri, long timestamp);

    // Name operations
    @Query("UPDATE user_profile SET name = :name, updatedAt = :timestamp WHERE userId = :userId")
    void updateName(String userId, String name, long timestamp);

    // Check if profile exists
    @Query("SELECT COUNT(*) FROM user_profile WHERE userId = :userId")
    int profileExists(String userId);
}
