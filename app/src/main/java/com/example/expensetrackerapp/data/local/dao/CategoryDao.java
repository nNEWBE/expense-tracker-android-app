package com.example.expensetrackerapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expensetrackerapp.data.local.entity.Category;

import java.util.List;

/**
 * Data Access Object for Category entity.
 * Provides methods to interact with category data in Room database.
 */
@Dao
public interface CategoryDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Category category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Category> categories);

    // Update operations
    @Update
    void update(Category category);

    // Delete operations
    @Delete
    void delete(Category category);

    @Query("DELETE FROM categories WHERE id = :id")
    void deleteById(long id);

    // Query operations
    @Query("SELECT * FROM categories WHERE isDefault = 1 OR userId = :userId ORDER BY name ASC")
    LiveData<List<Category>> getAllCategories(String userId);

    @Query("SELECT * FROM categories WHERE isDefault = 1 OR userId = :userId ORDER BY name ASC")
    List<Category> getAllCategoriesSync(String userId);

    @Query("SELECT * FROM categories WHERE isDefault = 1 ORDER BY name ASC")
    List<Category> getDefaultCategoriesSync();

    @Query("SELECT * FROM categories WHERE id = :id")
    LiveData<Category> getCategoryById(long id);

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    Category getCategoryByNameSync(String name);

    // Count operations
    @Query("SELECT COUNT(*) FROM categories")
    int getCategoryCount();
}
