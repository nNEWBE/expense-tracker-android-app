package com.example.expensetrackerapp.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetrackerapp.R;
import com.example.expensetrackerapp.databinding.ActivityForgotPasswordBinding;
import com.google.android.material.snackbar.Snackbar;

/**
 * Forgot Password Activity for password reset via email.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = AuthManager.getInstance();

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener(v -> onBackPressed());

        // Reset button
        binding.btnReset.setOnClickListener(v -> attemptResetPassword());

        // Back to login link
        binding.tvBackToLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        // Success screen back to login button
        binding.btnBackToLoginSuccess.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void attemptResetPassword() {
        String email = binding.etEmail.getText().toString().trim();

        // Validate email
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Email is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Enter a valid email");
            return;
        }

        binding.tilEmail.setError(null);

        // Show loading
        showLoading(true);

        // Send password reset email
        authManager.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        // Show success state
                        binding.layoutInput.setVisibility(View.GONE);
                        binding.layoutSuccess.setVisibility(View.VISIBLE);
                        binding.tvEmailSentTo.setText("We've sent a password reset link to:\n" + email);
                    } else {
                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Failed to send reset email. Please try again.";
                        showError(error);
                    }
                });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnReset.setEnabled(!show);
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.error))
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
