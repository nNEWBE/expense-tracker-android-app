package com.example.expensetrackerapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetrackerapp.MainActivity;
import com.example.expensetrackerapp.R;
import com.example.expensetrackerapp.data.local.AppDatabase;
import com.example.expensetrackerapp.data.local.entity.UserProfile;
import com.example.expensetrackerapp.databinding.ActivityRegisterBinding;
import com.example.expensetrackerapp.utils.Constants;
import com.google.android.material.snackbar.Snackbar;

/**
 * Registration Activity for new users.
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = AuthManager.getInstance();

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener(v -> onBackPressed());

        // Register button
        binding.btnRegister.setOnClickListener(v -> attemptRegister());

        // Login link
        binding.tvLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void attemptRegister() {
        // Get input values
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(name, email, password, confirmPassword)) {
            return;
        }

        // Show loading
        showLoading(true);

        // Register with Firebase
        authManager.signUp(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Create user profile in Firestore
                        String userId = authManager.getCurrentUserId();
                        authManager.createUserProfile(userId, email, name, new AuthManager.OnProfileCreatedListener() {
                            @Override
                            public void onSuccess() {
                                // Also create local profile
                                createLocalProfile(userId, email, name);

                                showLoading(false);
                                showSuccess("Account created successfully!");
                                navigateToMain();
                            }

                            @Override
                            public void onFailure(String error) {
                                showLoading(false);
                                showError("Profile creation failed: " + error);
                            }
                        });
                    } else {
                        showLoading(false);
                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed. Please try again.";
                        showError(error);
                    }
                });
    }

    private void createLocalProfile(String userId, String email, String name) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            UserProfile profile = new UserProfile();
            profile.setUserId(userId);
            profile.setEmail(email);
            profile.setName(name);
            profile.setGuest(false);
            profile.setCurrency(Constants.CURRENCY_BDT);
            profile.setMonthlyBudget(0);

            db.userProfileDao().insert(profile);
        });
    }

    private boolean validateInputs(String name, String email, String password, String confirmPassword) {
        boolean valid = true;

        // Validate name
        if (TextUtils.isEmpty(name)) {
            binding.tilName.setError("Name is required");
            valid = false;
        } else if (name.length() < 2) {
            binding.tilName.setError("Name must be at least 2 characters");
            valid = false;
        } else {
            binding.tilName.setError(null);
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Email is required");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Enter a valid email");
            valid = false;
        } else {
            binding.tilEmail.setError(null);
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Password is required");
            valid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            valid = false;
        } else {
            binding.tilPassword.setError(null);
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError("Please confirm your password");
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            valid = false;
        } else {
            binding.tilConfirmPassword.setError(null);
        }

        return valid;
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!show);
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.error))
                .show();
    }

    private void showSuccess(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.success))
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
