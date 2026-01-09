package com.example.expensetrackerapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.expensetrackerapp.R;
import com.example.expensetrackerapp.auth.AuthManager;
import com.example.expensetrackerapp.auth.LoginActivity;
import com.example.expensetrackerapp.data.repository.ExpenseRepository;
import com.example.expensetrackerapp.data.repository.UserRepository;
import com.example.expensetrackerapp.databinding.FragmentProfileBinding;
import com.example.expensetrackerapp.utils.Constants;
import com.example.expensetrackerapp.utils.CurrencyUtils;
import com.example.expensetrackerapp.utils.PreferenceManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Profile Fragment for user settings and account management.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private AuthManager authManager;
    private UserRepository userRepository;
    private PreferenceManager preferenceManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authManager = AuthManager.getInstance();
        userRepository = UserRepository.getInstance(requireContext());
        preferenceManager = PreferenceManager.getInstance(requireContext());

        setupUI();
        setupClickListeners();
        observeProfile();
    }

    private void setupUI() {
        // Set user info
        if (authManager.isGuest()) {
            binding.tvUserName.setText("Guest User");
            binding.tvUserEmail.setText("Not logged in");
            binding.cardGuestBanner.setVisibility(View.VISIBLE);
            binding.tvLogoutLabel.setText("Sign In");
            binding.btnDeleteAccount.setVisibility(View.GONE);
            binding.dividerDeleteAccount.setVisibility(View.GONE);
        } else {
            binding.tvUserName.setText(authManager.getCurrentUser().getDisplayName());
            binding.tvUserEmail.setText(authManager.getCurrentUserEmail());
            binding.cardGuestBanner.setVisibility(View.GONE);
            binding.tvLogoutLabel.setText(R.string.logout);
            binding.btnDeleteAccount.setVisibility(View.VISIBLE);
            binding.dividerDeleteAccount.setVisibility(View.VISIBLE);
        }

        // Set current theme selection
        int themeMode = preferenceManager.getThemeMode();
        switch (themeMode) {
            case Constants.THEME_LIGHT:
                binding.tvThemeValue.setText(R.string.light_mode);
                break;
            case Constants.THEME_DARK:
                binding.tvThemeValue.setText(R.string.dark_mode);
                break;
            default:
                binding.tvThemeValue.setText(R.string.system_default);
        }

        // Set current currency
        String currency = preferenceManager.getCurrency();
        binding.tvCurrencyValue.setText(currency);
    }

    private void setupClickListeners() {
        // Edit Profile
        binding.btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Edit Profile coming soon", Toast.LENGTH_SHORT).show();
        });

        // Currency setting
        binding.cardCurrency.setOnClickListener(v -> showCurrencyDialog());

        // Theme setting
        binding.cardTheme.setOnClickListener(v -> showThemeDialog());

        // Export data
        binding.cardExport.setOnClickListener(v -> Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show());

        // App lock
        binding.cardAppLock.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show();
        });

        // Logout
        binding.btnLogout.setOnClickListener(v -> {
            if (authManager.isGuest()) {
                startActivity(new Intent(requireContext(), LoginActivity.class));
                requireActivity().finish();
            } else {
                showLogoutDialog();
            }
        });

        // Delete account
        binding.btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        // Login from guest banner
        binding.btnGuestLogin.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
    }

    private void observeProfile() {
        userRepository.getProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                if (profile.getName() != null && !profile.getName().isEmpty()) {
                    binding.tvUserName.setText(profile.getName());
                }
            }
        });
    }

    private void showBudgetDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_budget, null);
        TextInputEditText etBudget = dialogView.findViewById(R.id.etBudget);

        new MaterialAlertDialogBuilder(requireContext(), R.style.Theme_App_Dialog)
                .setTitle(R.string.monthly_budget)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String budgetStr = etBudget.getText().toString().trim();
                    if (!budgetStr.isEmpty()) {
                        try {
                            double budget = Double.parseDouble(budgetStr);
                            userRepository.updateMonthlyBudget(budget, null);
                            Toast.makeText(requireContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showCurrencyDialog() {
        String[] currencies = CurrencyUtils.getCurrencyDisplayNames();
        String[] currencyCodes = CurrencyUtils.getSupportedCurrencies();

        new MaterialAlertDialogBuilder(requireContext(), R.style.Theme_App_Dialog)
                .setTitle(R.string.currency)
                .setItems(currencies, (dialog, which) -> {
                    String selectedCurrency = currencyCodes[which];
                    preferenceManager.setCurrency(selectedCurrency);
                    binding.tvCurrencyValue.setText(selectedCurrency);
                    Toast.makeText(requireContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showThemeDialog() {
        String[] themes = {
                getString(R.string.system_default),
                getString(R.string.light_mode),
                getString(R.string.dark_mode)
        };

        int currentSelection = preferenceManager.getThemeMode();

        new MaterialAlertDialogBuilder(requireContext(), R.style.Theme_App_Dialog)
                .setTitle(R.string.theme)
                .setSingleChoiceItems(themes, currentSelection, (dialog, which) -> {
                    preferenceManager.setThemeMode(which);
                    binding.tvThemeValue.setText(themes[which]);
                    dialog.dismiss();
                })
                .show();
    }

    private void showExportDialog() {
        String[] options = {
                getString(R.string.export_csv),
                getString(R.string.export_pdf)
        };

        new MaterialAlertDialogBuilder(requireContext(), R.style.Theme_App_Dialog)
                .setTitle(R.string.export_data)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Toast.makeText(requireContext(), "Exporting CSV...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Exporting PDF...", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext(), R.style.Theme_App_Dialog)
                .setTitle(R.string.confirm_logout_title)
                .setMessage(R.string.confirm_logout_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    authManager.signOut();
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                    requireActivity().finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void showDeleteAccountDialog() {
        new MaterialAlertDialogBuilder(requireContext(), R.style.Theme_App_Dialog)
                .setTitle(R.string.confirm_delete_account_title)
                .setMessage(R.string.confirm_delete_account_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    // Delete account
                    authManager.deleteAccount().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(requireContext(), LoginActivity.class));
                            requireActivity().finish();
                        } else {
                            Toast.makeText(requireContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
