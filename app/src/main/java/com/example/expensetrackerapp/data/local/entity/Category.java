package com.example.expensetrackerapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Entity representing expense categories.
 * Pre-populated with default categories.
 */
@Entity(tableName = "categories")
public class Category {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String icon; // Icon resource name or emoji
    private String color; // Hex color code
    private boolean isDefault; // Whether this is a default category
    private String userId; // For custom user categories

    // Constructors
    public Category() {
    }

    @Ignore
    public Category(String name, String icon, String color, boolean isDefault) {
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.isDefault = isDefault;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
